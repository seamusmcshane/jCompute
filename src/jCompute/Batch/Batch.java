package jCompute.Batch;

import jCompute.Datastruct.List.Interface.StoredQueuePosition;
import jCompute.Datastruct.cache.DiskCache;
import jCompute.Gui.Component.Swing.JComputeProgressMonitor;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.ScenarioManager;
import jCompute.Scenario.ConfigurationInterpreter;
import jCompute.Stats.StatExporter;
import jCompute.util.FileUtil;
import jCompute.util.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Batch implements StoredQueuePosition
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(Batch.class);
	
	// Batch Attributes
	private int position;
	private int batchId;
	private String batchName;
	private BatchPriority priority;
	private String baseScenarioFileName;
	private String batchFileName;
	private ArrayList<String> parameters;
	private ArrayList<String> infoCache;
	private boolean needGenerated = true;
	
	// Set if this batch's items can be processed (stop/start)
	private boolean status = true;
	private String type;
	
	// Items Management
	private int itemsRequested = 0;
	private int itemsReturned = 0;
	private int batchItems = 0;
	
	// Number of repeats of each item (can be used for averages)
	private int itemSamples;
	
	// Maximum steps for simulations in this batch
	private int maxSteps = 0;
	
	// For human readable date/time info
	private String addedDateTime = "";
	
	// Log - total time calc
	private long startTime;
	private String startDateTime = "Not Started";
	private String endDateTime = "Not Finished";
	
	// Items processing times and eta calculation
	private long cpuTotalTimes;
	private long netTotalTimes;
	private long ioTotalTimes;
	private long lastCompletedItemTime;
	
	// Enable / Disable writing the generated statistic files to disk
	private boolean storeStats;
	
	// Write stats to a single archive or directories with sub archives
	private boolean statsMethodSingleArchive;
	private int singleArchiveCompressionLevel;
	
	// The export dir for stats
	private String batchStatsExportDir;
	private ZipOutputStream resultsZipOut;
	
	// Item log writer
	private PrintWriter itemLog;
	private boolean itemLogEnabled;
	
	// Used for combination and for saving axis names
	private String parameterName[];
	private String groupName[];
	
	// Simple Batch Log
	private boolean infoLogEnabled;
	
	// Our Queue of Items yet to be processed
	private LinkedList<BatchItem> queuedItems;
	
	// The active Items currently being processed.
	private ArrayList<BatchItem> activeItems;
	private int active;
	
	// Completed items count
	private int completed = 0;
	
	// The Batch Configuration Text
	private String batchConfigText;
	
	// The Configuration Processor
	private ConfigurationInterpreter batchConfigProcessor;
	
	// The base scenario
	private String basePath;
	
	// Base scenario text
	private String baseScenarioText;
	
	// Disk Cache for Items
	private DiskCache itemDiskCache;
	
	// To protect our shared variables/data structures
	private Semaphore batchLock = new Semaphore(1, false);
	
	public Batch(int batchId, BatchPriority priority)
	{
		this.batchId = batchId;
		this.priority = priority;
		
		// Processing Times
		cpuTotalTimes = 0;
		netTotalTimes = 0;
		ioTotalTimes = 0;
		
		addedDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		
		// Item management data structures
		queuedItems = new LinkedList<BatchItem>();
		activeItems = new ArrayList<BatchItem>();
		
		// Active Items
		active = 0;
	}
	
	public boolean loadConfig(String filePath)
	{
		boolean status = true;
		log.info("New Batch based on : " + filePath);
		this.batchFileName = getFileName(filePath);
		log.debug("File : " + batchFileName);
		
		batchName = batchFileName.replaceAll("\\s*\\b.batch\\b\\s*", "");
		
		basePath = getPath(filePath);
		log.debug("Base Path : " + basePath);
		
		batchConfigText = jCompute.util.Text.textFileToString(filePath);
		
		if(batchConfigText != null)
		{
			status = processBatchConfig(batchConfigText);
		}
		
		return status;
	}
	
	/**
	 * Processes and Validates the batch config text
	 * @param fileText
	 * @param genComboMonitor
	 * @return
	 * @throws IOException
	 */
	private boolean processBatchConfig(String fileText)
	{
		boolean status = true;
		
		batchLock.acquireUninterruptibly();
		
		log.info("Processing BatchFile");
		
		batchConfigProcessor = new ConfigurationInterpreter();
		
		batchConfigProcessor.loadConfig(batchConfigText);
		
		status = checkBatchFile();
		
		if(status)
		{
			ScenarioInf baseScenario = getBaseScenario(fileText);
			
			if(baseScenario != null)
			{
				maxSteps = baseScenario.getEndEventTriggerValue("StepCount");
				
				type = baseScenario.getScenarioType();
				log.debug(type);
				
				// Logs + Stats
				storeStats = batchConfigProcessor.getBooleanValue("Stats", "Store");
				statsMethodSingleArchive = batchConfigProcessor.getBooleanValue("Stats", "SingleArchive");
				singleArchiveCompressionLevel = batchConfigProcessor.getIntValue("Stats", "CompressionLevel");
				infoLogEnabled = batchConfigProcessor.getBooleanValue("Log", "InfoLog");
				itemLogEnabled = batchConfigProcessor.getBooleanValue("Log", "ItemLog");
				
				setBatchStatExportDir();
				
			}
			else
			{
				status = false;
			}
		}
		
		batchLock.release();
		
		return status;
	}
	
	public boolean itemsNeedGenerated()
	{
		return needGenerated;
	}
	
	public void generateItems(JComputeProgressMonitor genComboMonitor)
	{
		log.info("Created DiskCache for Batch " + batchId);
		
		// Create DiskCache
		itemDiskCache = new DiskCache(batchStatsExportDir, Deflater.BEST_SPEED);
		
		float progress = 0;
		
		if(genComboMonitor != null)
		{
			genComboMonitor.setProgress(0);
		}
		
		// Get a count of the parameter groups.
		int parameterGroups = batchConfigProcessor.getSubListSize("Parameters", "Parameter");
		log.debug("Number of Parameter Groups : " + parameterGroups);
		
		// Array to hold the parameter type (group/single)
		String ParameterType[] = new String[parameterGroups];
		
		// Array to hold the path to group/parameter
		String Path[] = new String[parameterGroups];
		
		// Array to hold the unique identifier for the group.
		groupName = new String[parameterGroups];
		
		// Array to hold the parameter name that will be changed.
		parameterName = new String[parameterGroups];
		
		// Initial values of each parameter
		int Intial[] = new int[parameterGroups];
		
		// Increment values of each parameter
		int Increment[] = new int[parameterGroups];
		
		// Combinations for each parameter
		int Combinations[] = new int[parameterGroups];
		
		// Value of the max combination for each parameter
		int IncrementMaxValue[] = new int[parameterGroups];
		
		// The value in the combination at which to increment the value of
		// the
		// parameter
		int IncrementMod[] = new int[parameterGroups];
		
		parameters = new ArrayList<String>();
		
		// Iterate over the detected parameters and populate the arrays
		String section = "";
		for(int p = 0; p < parameterGroups; p++)
		{
			// Generate the parameter path in the xml array (0),(1) etc
			log.debug("Parameter Group : " + p);
			section = "Parameters.Parameter(" + p + ")";
			
			// Get the type (group/single)
			ParameterType[p] = batchConfigProcessor.getStringValue(section, "Type");
			
			// Populate the path to this parameter.
			Path[p] = batchConfigProcessor.getStringValue(section, "Path");
			
			// Store the group name if this parameter changes one in a
			// group.
			groupName[p] = "";
			if(ParameterType[p].equalsIgnoreCase("Group"))
			{
				groupName[p] = batchConfigProcessor.getStringValue(section, "GroupName");
			}
			
			// Store the name of the paramter to change
			parameterName[p] = batchConfigProcessor.getStringValue(section, "ParameterName");
			
			// Intial value
			Intial[p] = batchConfigProcessor.getIntValue(section, "Intial");
			
			// Increment value
			Increment[p] = batchConfigProcessor.getIntValue(section, "Increment");
			
			// Combinations e.g 2 = initial value + 1 increment
			Combinations[p] = batchConfigProcessor.getIntValue(section, "Combinations");
			
			// Max value = Combinations-1 as initial is the first
			IncrementMaxValue[p] = Intial[p] + ((Combinations[p] - 1) * Increment[p]);
			
			parameters.add("");
			parameters.add("");
			parameters.add("ParameterType");
			parameters.add(ParameterType[p]);
			parameters.add("Path");
			parameters.add(Path[p]);
			parameters.add("GroupName");
			parameters.add(groupName[p]);
			parameters.add("Intial");
			parameters.add(String.valueOf(Intial[p]));
			parameters.add("Increment");
			parameters.add(String.valueOf(Increment[p]));
			parameters.add("Combinations");
			parameters.add(String.valueOf(Combinations[p]));
			parameters.add("IncrementMaxValue");
			parameters.add(String.valueOf(IncrementMaxValue[p]));
			
			// Logging
			log.info("ParameterType : " + ParameterType[p]);
			log.info("Path : " + Path[p]);
			log.info("GroupName : " + groupName[p]);
			log.info("ParameterName : " + parameterName[p]);
			log.info("Intial : " + Intial[p]);
			log.info("Increment : " + Increment[p]);
			log.info("Combinations : " + Combinations[p]);
			log.info("IncrementMaxValue : " + IncrementMaxValue[p]);
			
		}
		
		int currentValues[] = new int[parameterGroups];
		int combinations = 1;
		for(int p = 0; p < parameterGroups; p++)
		{
			// Set Initial Values used in generation
			currentValues[p] = Intial[p];
			
			// Calculate Total Combinations
			combinations *= Combinations[p];
			
			if(p > 0)
			{
				IncrementMod[p] = IncrementMod[p - 1] * Combinations[p];
			}
			else
			{
				// P[0] always increments
				IncrementMod[p] = 1;
			}
			log.info("Group " + p + " Increments @Combo%" + IncrementMod[p]);
			
		}
		log.info("Combinations " + combinations);
		
		// The temp scenario used to generate the xml.
		ConfigurationInterpreter temp;
		
		// Combination space coordinates X,Y,Z..
		ArrayList<Integer> comboCoordinates = new ArrayList<Integer>(parameterGroups);
		for(int p = 0; p < parameterGroups; p++)
		{
			// Initialise the coordinates (1 base)
			comboCoordinates.add(1);
		}
		
		// For progress monitoring during generation
		float progressInc = 100f / (float) combinations;
		
		// Create and populate Results Zip Aachive with Directories
		String zipFileName = batchStatsExportDir + File.separator + "results.zip";
		log.info("Zip Archive : " + zipFileName);
		
		if(storeStats)
		{
			if(statsMethodSingleArchive)
			{
				try
				{
					resultsZipOut = new ZipOutputStream(new FileOutputStream(zipFileName));
					
					if(singleArchiveCompressionLevel > 9)
					{
						singleArchiveCompressionLevel = 9;
					}
					
					if(singleArchiveCompressionLevel < 0)
					{
						singleArchiveCompressionLevel = 0;
					}
					
					resultsZipOut.setMethod(ZipOutputStream.DEFLATED);
					resultsZipOut.setLevel(singleArchiveCompressionLevel);
				}
				catch(FileNotFoundException e1)
				{
					log.error("Could not create create  " + zipFileName);
					
					e1.printStackTrace();
				}
			}
		}
		
		// Set the combination Values
		for(int combo = 1; combo < combinations + 1; combo++)
		{
			if(storeStats)
			{
				if(statsMethodSingleArchive)
				{
					// Create Sub Directories in Zip Archive or Directory
					try
					{
						// Create Item Directories
						resultsZipOut.putNextEntry(new ZipEntry(Integer.toString(combo) + "/"));
						resultsZipOut.closeEntry();
						
						for(int sid = 1; sid < itemSamples + 1; sid++)
						{
							// Create Sample Directories
							resultsZipOut.putNextEntry(new ZipEntry(Integer.toString(combo) + "/"
									+ Integer.toString(sid) + "/"));
							resultsZipOut.closeEntry();
						}
						
					}
					catch(IOException e)
					{
						log.error("Could not create create directory " + combo + " in " + zipFileName);
						
						e.printStackTrace();
					}
				}
				else
				{
					// Create the item export dir
					FileUtil.createDirIfNotExist(batchStatsExportDir + File.separator + combo);
					
					for(int sid = 1; sid < itemSamples + 1; sid++)
					{
						String fullExportPath = batchStatsExportDir + File.separator + combo + File.separator + sid;
						
						// Create the item sample full export path dir
						FileUtil.createDirIfNotExist(fullExportPath);
					}
				}
			}
			
			// Create a copy of the base scenario
			temp = new ConfigurationInterpreter();
			temp.loadConfig(baseScenarioText);
			
			StringBuilder itemName = new StringBuilder();
			
			// Start of log line + itemName
			itemName.append("Combo " + combo);
			
			// Change the value for each parameter group
			for(int p = 0; p < parameterGroups; p++)
			{
				
				// This is a group parameter
				if(ParameterType[p].equalsIgnoreCase("Group"))
				{
					// Log line middle
					itemName.append(" " + Path[p] + "." + groupName[p] + "." + parameterName[p] + " "
							+ currentValues[p]);
					
					int groups = temp.getSubListSize(Path[p]);
					
					// Find the correct group that matches the name
					for(int sg = 0; sg < groups; sg++)
					{
						String groupSection = Path[sg] + "(" + sg + ")";
						
						String searchGroupName = temp.getStringValue(groupSection, "Name");
						
						// Found Group
						if(searchGroupName.equalsIgnoreCase(groupName[p]))
						{
							// Combo / targetGroupName / Current GroupName
							// DebugLogger.output(c + " " + targetGroupName
							// +
							// " " + GroupName[p]);
							
							// The parameter we want
							// DebugLogger.output(ParameterName[p]);
							
							// String target = Path[p]+"."+ParameterName[p];
							// String target =
							// groupSection+"."+ParameterName[p];
							
							// Current Value in XML
							// DebugLogger.output("Current Value " +
							// temp.getIntValue(groupSection,ParameterName[p]));
							
							// Find the datatype to change
							String dtype = temp.findDataType(Path[p] + "." + parameterName[p]);
							
							// Currently only decimal and integer are used.
							if(dtype.equals("boolean"))
							{
								temp.changeValue(groupSection, parameterName[p], new Boolean(true));
							}
							else if(dtype.equals("string"))
							{
								temp.changeValue(groupSection, parameterName[p], " ");
							}
							else if(dtype.equals("decimal"))
							{
								temp.changeValue(groupSection, parameterName[p], currentValues[p]);
							}
							else if(dtype.equals("integer"))
							{
								temp.changeValue(groupSection, parameterName[p], currentValues[p]);
							}
							else
							{
								// This will not happen unless there is a
								// new
								// datatype added to the XML standards
								// schema
								// and we use it.
								log.error("Unknown XML DTYPE : " + dtype);
							}
							/*
							 * DebugLogger.output("New Value " +
							 * temp.getIntValue
							 * (groupSection,ParameterName[p]));
							 * DebugLogger.output("Target : " + target);
							 * DebugLogger.output("Value : " +
							 * temp.getValueToString(target,
							 * temp.findDataType(target)));
							 */
							
							// Group was found and value was changed now
							// exit
							// search
							break;
						}
						
					}
					
				}
				else
				{
					// Log line middle
					itemName.append(" " + Path[p] + "." + parameterName[p] + " " + currentValues[p]);
					
					// Fine the datatype for this parameter
					String dtype = temp.findDataType(Path[p] + "." + parameterName[p]);
					
					// Currently only decimal and integer are used.
					if(dtype.equals("boolean"))
					{
						temp.changeValue(Path[p], parameterName[p], new Boolean(true));
					}
					else if(dtype.equals("string"))
					{
						temp.changeValue(Path[p], parameterName[p], " ");
					}
					else if(dtype.equals("decimal"))
					{
						temp.changeValue(Path[p], parameterName[p], currentValues[p]);
					}
					else if(dtype.equals("integer"))
					{
						temp.changeValue(Path[p], parameterName[p], currentValues[p]);
					}
					else
					{
						// This will not happen unless there is a new
						// datatype
						// added to the XML standards schema.
						log.error("Unknown XML DTYPE : " + dtype);
					}
					
				}
				
			}
			// Log line end
			log.debug(itemName.toString());
			
			StringBuilder logLine = new StringBuilder();
			
			// DebugLogger.output(temp.getScenarioXMLText());
			logLine.append("ComboPos(");
			ArrayList<Integer> tempCoord = new ArrayList<Integer>();
			ArrayList<Integer> tempCoordValues = new ArrayList<Integer>();
			for(int p = 0; p < parameterGroups; p++)
			{
				logLine.append(String.valueOf(comboCoordinates.get(p)));
				if(p < (parameterGroups - 1))
				{
					logLine.append('x');
				}
				
				tempCoord.add(comboCoordinates.get(p));
				tempCoordValues.add(currentValues[p]);
			}
			logLine.append(")");
			log.info(logLine.toString());
			
			// Add the new Batch Item combo used for batch item id,
			// getScenarioXMLText is the new scenario xml configuration -
			// samples is the number of identical items to generate (used as
			// a
			// sample/average)
			addBatchItem(itemSamples, combo, itemName.toString(), temp.getScenarioXMLText(), tempCoord, tempCoordValues);
			
			// Increment the combinatorics values.
			for(int p = 0; p < parameterGroups; p++)
			{
				
				// Work out if the current c value is a increment for this
				// group.
				if(combo % (IncrementMod[p]) == 0)
				{
					currentValues[p] = (currentValues[p] + Increment[p]);
					
					// Increment after currentValues is greater than
					// IncrementMaxValue
					if(currentValues[p] > IncrementMaxValue[p])
					{
						// Reset to initial value
						currentValues[p] = Intial[p];
					}
					
					// P[0] increments 1 each time, wrap it by the roll over
					// value of its combinations number
					if(p == 0)
					{
						// Increment the coordinate by 1
						comboCoordinates.set(p, (comboCoordinates.get(p) % Combinations[p]) + 1);
					}
					else
					{
						// Increment the coordinate by 1
						comboCoordinates.set(p, comboCoordinates.get(p) + 1);
					}
					
				}
				
			}
			
			progress += progressInc;
			
			if(genComboMonitor != null)
			{
				genComboMonitor.setProgress(Math.min((int) progress, 100));
			}
		}
		
		// All the items need to get processed, but the ett is influenced by
		// the
		// order (randomise it in an attempt to reduce influence)
		Collections.shuffle(queuedItems);
		
		if(genComboMonitor != null)
		{
			genComboMonitor.setProgress(100);
		}
		
		needGenerated = false;
	}
	
	private boolean checkBatchFile()
	{
		boolean status = true;
		
		if(!batchConfigProcessor.getScenarioType().equalsIgnoreCase("Batch"))
		{
			status = false;
			log.error("Invalid Batch File");
		}
		
		return status;
	}
	
	private void startBatchLog(int numCordinates)
	{
		if(itemLogEnabled)
		{
			try
			{
				itemLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator
						+ "ItemLog.log", true)));
				
				/* Common Log Header */
				/*
				 * itemLog.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
				 * );
				 * itemLog.println(
				 * "<Log xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"schemas/batchItemLog.xsd\">"
				 * );
				 * itemLog.println("<Header>");
				 * itemLog.println("<Name>" + batchName + "</Name>");
				 * itemLog.println("<LogType>" + "BatchItems" + "</LogType>");
				 * itemLog.println("<SamplesPerItem>" + itemSamples +
				 * "</SamplesPerItem>");
				 * itemLog.println("<AxisLabels>");
				 * for(int c = 1; c < numCordinates + 1; c++)
				 * {
				 * itemLog.println("<AxisLabel>");
				 * itemLog.println("<ID>" + c + "</ID>");
				 * itemLog.println("<Name>" + groupName[c - 1] + parameterName[c
				 * - 1] + "</Name>");
				 * itemLog.println("</AxisLabel>");
				 * }
				 * itemLog.println("</AxisLabels>");
				 * itemLog.println("</Header>");
				 * itemLog.println("<Items>");
				 */
				
				itemLog.println("[+Header]");
				itemLog.println("Name=" + batchName);
				itemLog.println("LogType=BatchItems");
				itemLog.println("Samples=" + itemSamples);
				itemLog.println("[+AxisLabels]");
				for(int c = 1; c < numCordinates + 1; c++)
				{
					itemLog.println("id=" + c);
					itemLog.println("AxisName=" + groupName[c - 1] + parameterName[c - 1]);
				}
				
				itemLog.println("[-AxisLabels]");
				itemLog.println("[-Header]");
				itemLog.println("[+Items]");
				
			}
			catch(IOException e)
			{
				System.out.println("Could not created item log file");
			}
		}
	}
	
	private void setBatchStatExportDir()
	{
		String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		String time = new SimpleDateFormat("HHmm").format(Calendar.getInstance().getTime());
		
		log.debug(date + "+" + time);
		
		String section = "Stats";
		
		// Normally stats/
		String baseExportDir = batchConfigProcessor.getStringValue(section, "BatchStatsExportDir");
		
		// Create Stats Dir
		FileUtil.createDirIfNotExist(baseExportDir);
		
		// Group Batches of Stats
		String groupDirName = batchConfigProcessor.getStringValue(section, "BatchGroupDir");
		
		String subgroupDirName = batchConfigProcessor.getStringValue(section, "BatchSubGroupDirName");
		
		// Append Group name to export dir and create if needed
		if(groupDirName != null)
		{
			baseExportDir = baseExportDir + File.separator + groupDirName;
			
			FileUtil.createDirIfNotExist(baseExportDir);
			
			// Sub Groups
			if(subgroupDirName != null)
			{
				baseExportDir = baseExportDir + File.separator + subgroupDirName;
				
				FileUtil.createDirIfNotExist(baseExportDir);
			}
			
		}
		
		// How many times to run each batchItem.
		itemSamples = batchConfigProcessor.getIntValue("Config", "ItemSamples");
		
		int uniqueItems = batchItems / itemSamples;
		
		batchStatsExportDir = baseExportDir + File.separator + date + "@" + time + "[" + batchId + "][" + itemSamples
				+ "-" + uniqueItems + "-" + batchItems + "-" + maxSteps + "] " + batchName;
		
		FileUtil.createDirIfNotExist(batchStatsExportDir);
		
		log.debug("Batch Stats Export Dir : " + batchStatsExportDir);
	}
	
	public String getBatchStatsExportDir()
	{
		return batchStatsExportDir;
	}
	
	private ScenarioInf getBaseScenario(String fileText)
	{
		ScenarioInf scenario = null;
		
		baseScenarioFileName = batchConfigProcessor.getStringValue("Config", "BaseScenarioFileName");
		
		String baseScenaroFilePath = basePath + File.separator + baseScenarioFileName;
		
		log.debug("Base Scenario File : " + baseScenaroFilePath);
		
		baseScenarioText = jCompute.util.Text.textFileToString(baseScenaroFilePath);
		
		if(baseScenarioText != null)
		{
			scenario = ScenarioManager.getScenario(baseScenarioText);
		}
		
		return scenario;
	}
	
	public void returnItemToQueue(BatchItem item)
	{
		batchLock.acquireUninterruptibly();
		
		queuedItems.add(item);
		
		itemsReturned++;
		
		activeItems.remove(item);
		
		active = activeItems.size();
		
		batchLock.release();
		
	}
	
	public BatchItem getNext()
	{
		batchLock.acquireUninterruptibly();
		
		BatchItem temp = queuedItems.remove();
		
		activeItems.add(temp);
		
		active = activeItems.size();
		
		// Is this the first Item && Sample
		if(itemsRequested == 0)
		{
			startBatchLog(temp.getCoordinates().size());
			
			// For run time calc
			startTime = System.currentTimeMillis();
			startDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		}
		
		itemsRequested++;
		
		batchLock.release();
		
		return temp;
	}
	
	public void setItemNotActive(BatchItem item)
	{
		batchLock.acquireUninterruptibly();
		
		activeItems.remove(item);
		
		batchLock.release();
	}
	
	public void setItemComplete(BatchItem item, StatExporter exporter)
	{
		batchLock.acquireUninterruptibly();
		
		long ioStart = System.currentTimeMillis();
		long ioEnd;
		
		log.debug("Setting Completed Sim " + item.getSimId() + " Item " + item.getItemId());
		
		// activeItems.remove(item);
		
		// For estimated complete time calculation
		cpuTotalTimes += item.getCPUTime();
		netTotalTimes += item.getNetTime();
		
		if(itemLogEnabled)
		{
			/*
			 * itemLog.println("<Item>");
			 * itemLog.println("<IID>" + item.getItemId() + "</IID>");
			 * itemLog.println("<SID>" + item.getSampleId() + "</SID>");
			 * itemLog.println("<Coordinates>");
			 * ArrayList<Integer> coords = item.getCoordinates();
			 * ArrayList<Integer> coordsValues = item.getCoordinatesValues();
			 * for(int c = 0; c < coords.size(); c++)
			 * {
			 * itemLog.println("<Coordinate>");
			 * itemLog.println("<Pos>" + coords.get(c) + "</Pos>");
			 * itemLog.println("<Value>" + coordsValues.get(c) + "</Value>");
			 * itemLog.println("</Coordinate>");
			 * }
			 * itemLog.println("</Coordinates>");
			 * itemLog.println("<Hash>" + item.getItemHash() + "</Hash>");
			 * itemLog.println("<RunTime>" +
			 * jCompute.util.Text.longTimeToDHMS(item.getTotalTime()) +
			 * "</RunTime>");
			 * itemLog.println("<EndEvent>" + item.getEndEvent() +
			 * "</EndEvent>");
			 * itemLog.println("<StepCount>" + item.getStepCount() +
			 * "</StepCount>");
			 * itemLog.println("</Item>");
			 */
			
			itemLog.println("[+Item]");
			itemLog.println("IID=" + item.getItemId());
			itemLog.println("SID=" + item.getSampleId());
			ArrayList<Integer> coords = item.getCoordinates();
			ArrayList<Integer> coordsValues = item.getCoordinatesValues();
			for(int c = 0; c < coords.size(); c++)
			{
				itemLog.println("[+Coordinate]");
				itemLog.println("Pos=" + coords.get(c));
				itemLog.println("Value=" + coordsValues.get(c));
				itemLog.println("[-Coordinate]");
			}
			itemLog.println("Hash=" + item.getItemHash());
			itemLog.println("RunTime=" + item.getTotalTime());
			itemLog.println("EndEvent=" + item.getEndEvent());
			itemLog.println("StepCount=" + item.getStepCount());
			itemLog.println("[-Item]");
		}
		
		// Only Save configs if stats are enabled
		if(storeStats)
		{
			if(statsMethodSingleArchive)
			{
				// Export the stats
				exporter.exportAllStatsToZipDir(resultsZipOut, item.getItemId(), item.getSampleId());
			}
			else
			{
				String fullExportPath = batchStatsExportDir + File.separator + item.getItemId() + File.separator
						+ item.getSampleId();
				
				// Export the stats
				exporter.exportAllStatsToDir(fullExportPath);
			}
			
			// Only the first sample needs to save the item config (all
			// identical
			// samples)
			if(item.getSampleId() == 1)
			{
				if(statsMethodSingleArchive)
				{
					try
					{
						// FileName
						resultsZipOut.putNextEntry(new ZipEntry(item.getItemId() + "/" + "itemconfig-"
								+ item.getItemHash() + ".xml"));
						
						// Data
						resultsZipOut.write(itemDiskCache.getFile(item.getItemHash()));
						
						// Entry end
						resultsZipOut.closeEntry();
					}
					catch(IOException e1)
					{
						log.error("Unable to save item config " + item.getItemId() + " to results zip");
						e1.printStackTrace();
					}
				}
				else
				{
					// Save the Item Config
					try
					{
						// All Item samples use same config so overwrite.
						PrintWriter configFile = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir
								+ File.separator + item.getItemId() + File.separator + "itemconfig-"
								+ item.getItemHash() + ".xml", true)));
						
						configFile.write(new String(itemDiskCache.getFile(item.getItemHash()), "ISO-8859-1"));
						configFile.flush();
						configFile.close();
					}
					catch(IOException e)
					{
						System.out.println("Could not save item " + item.getItemId() + " config (Batch "
								+ item.getBatchId() + ")");
					}
				}
				
			}
		}
		
		completed++;
		
		if(completed == batchItems)
		{
			if(storeStats)
			{
				if(statsMethodSingleArchive)
				{
					try
					{
						resultsZipOut.close();
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			
			if(itemLogEnabled)
			{
				// Close Batch Log
				/*
				 * itemLog.println("</Items>");
				 * itemLog.println("</Log>");
				 */
				
				itemLog.println("[-Items]");
				
				itemLog.flush();
				itemLog.close();
			}
			
			endDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			
			// Write the info log
			if(infoLogEnabled)
			{
				try
				{
					// Close Info Log
					PrintWriter infoLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir
							+ File.separator + "InfoLog.xml", true)));
					infoLog.println("<Batch>");
					infoLog.println("<ID>" + batchId + "</ID>");
					infoLog.println("<ScenarioType>" + type + "</ScenarioType>");
					infoLog.println("<Description>" + batchName + "</Description>");
					infoLog.println("<BaseFile>" + baseScenarioFileName + "</BaseFile>");
					infoLog.println("<Start>" + startDateTime + "</Start>");
					infoLog.println("<Finished>" + endDateTime + "</Finished>");
					infoLog.println("<TotalTime>"
							+ jCompute.util.Text.longTimeToDHMS(System.currentTimeMillis() - startTime)
							+ "</TotalTime>");
					infoLog.println("<Batch>");
					infoLog.flush();
					infoLog.close();
				}
				catch(IOException e)
				{
					log.error("Error writing info log");
				}
			}
			
			log.info("Removing Batch " + batchId + " DiskCache");
			itemDiskCache.clear();
		}
		
		ioEnd = System.currentTimeMillis();
		
		ioTotalTimes += ioEnd - ioStart;
		
		lastCompletedItemTime = System.currentTimeMillis();
		
		batchLock.release();
	}
	
	public int getRemaining()
	{
		return queuedItems.size();
	}
	
	// Small wrapper around queue add and disk cache
	private void addBatchItem(int samples, int id, String name, String configText, ArrayList<Integer> coordinates,
			ArrayList<Integer> coordinatesValues)
	{
		byte[] configBytes = null;
		try
		{
			configBytes = configText.getBytes("ISO-8859-1");
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		String itemHash = itemDiskCache.addFile(configBytes);
		
		// SID/SampleId is 1 base/ 1=first sample
		for(int sid = 1; sid < samples + 1; sid++)
		{
			queuedItems.add(new BatchItem(sid, id, batchId, name, itemHash, coordinates, coordinatesValues));
		}
		
		batchItems = batchItems + (1 * samples);
	}
	
	public int getBatchId()
	{
		return batchId;
	}
	
	public void setBatchId(int batchId)
	{
		this.batchId = batchId;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getBaseScenarioFileName()
	{
		return baseScenarioFileName;
	}
	
	public void setBaseScenarioFile(String baseScenarioFile)
	{
		this.baseScenarioFileName = baseScenarioFile;
	}
	
	public int getBatchItems()
	{
		return batchItems;
	}
	
	public void setBatchItems(int batchItems)
	{
		this.batchItems = batchItems;
	}
	
	public int getProgress()
	{
		return (int) (((float) completed / (float) batchItems) * 100f);
	}
	
	public int getCompleted()
	{
		return completed;
	}
	
	public int getActiveItemsCount()
	{
		return activeItems.size();
	}
	
	public long getRunTime()
	{
		return lastCompletedItemTime - startTime;
	}
	
	public long getETT()
	{
		if(active > 0 && completed > 0)
		{
			return ((cpuTotalTimes + netTotalTimes + ioTotalTimes) / completed) * (batchItems - completed) / active;
		}
		
		return 0;
	}
	
	public String[] getBatchInfo()
	{
		ArrayList<String> info = new ArrayList<String>();
		
		if(!needGenerated)
		{
			// Cache the non changing values
			if(infoCache == null)
			{
				infoCache = new ArrayList<String>();
				
				infoCache.add("Id");
				infoCache.add(String.valueOf(batchId));
				infoCache.add("Name");
				infoCache.add(batchName);
				infoCache.add("Scenario Type");
				infoCache.add(type);
				
				infoCache.add("");
				infoCache.add("");
				infoCache.add("Unique Items");
				infoCache.add(String.valueOf(batchItems / itemSamples));
				infoCache.add("Sample per Item");
				infoCache.add(String.valueOf(itemSamples));
				infoCache.add("Total Items");
				infoCache.add(String.valueOf(batchItems));
				infoCache.add("Max Steps");
				infoCache.add(String.valueOf(maxSteps));
				
				// Add The parameters and free
				infoCache.addAll(parameters);
				parameters = null;
				
				infoCache.add("");
				infoCache.add("");
				infoCache.add("Batch File");
				infoCache.add(batchFileName);
				infoCache.add("Scenario");
				infoCache.add(baseScenarioFileName);
				infoCache.add("Export Directory");
				infoCache.add(batchStatsExportDir);
				
				infoCache.add("");
				infoCache.add("");
				infoCache.add("Stats Store");
				infoCache.add(storeStats == true ? "Enabled" : "Disabled");
				infoCache.add("Single Archive");
				infoCache.add(statsMethodSingleArchive == true ? "Enabled" : "Disabled");
				infoCache.add("Compression Level");
				infoCache.add(String.valueOf(singleArchiveCompressionLevel));
				infoCache.add("Info Log");
				infoCache.add(infoLogEnabled == true ? "Enabled" : "Disabled");
				infoCache.add("Item Log");
				infoCache.add(itemLogEnabled == true ? "Enabled" : "Disabled");
			}
		}
		
		info.add("Queue Position");
		info.add(String.valueOf(position));
		info.add("Status");
		info.add(status == true ? "Enabled" : "Disabled");
		info.add("Priority");
		info.add(priority.toString());
		
		if(!needGenerated)
		{
			// Add the cached values
			info.addAll(infoCache);
		}
		else
		{
			info.add("Id");
			info.add(String.valueOf(batchId));
			info.add("Name");
			info.add(batchName);
			info.add("Scenario Type");
			info.add(type);
			
			info.add("");
			info.add("");
			info.add("Batch File");
			info.add(batchFileName);
			info.add("Scenario");
			info.add(baseScenarioFileName);
			info.add("Export Directory");
			info.add(batchStatsExportDir);
			
			info.add("");
			info.add("");
			info.add("Stats Store");
			info.add(storeStats == true ? "Enabled" : "Disabled");
			info.add("Single Archive");
			info.add(statsMethodSingleArchive == true ? "Enabled" : "Disabled");
			info.add("Compression Level");
			info.add(String.valueOf(singleArchiveCompressionLevel));
			info.add("Info Log");
			info.add(infoLogEnabled == true ? "Enabled" : "Disabled");
			info.add("Item Log");
			info.add(itemLogEnabled == true ? "Enabled" : "Disabled");
		}
		
		info.add("");
		info.add("");
		info.add("Active Items");
		info.add(String.valueOf(getActiveItemsCount()));
		
		info.add("Items Completed");
		info.add(String.valueOf(completed));
		info.add("Items Requested");
		info.add(String.valueOf(itemsRequested));
		info.add("Items Returned");
		info.add(String.valueOf(itemsReturned));
		
		int div = 1;
		if(completed > 0)
		{
			div = completed;
		}
		
		info.add("");
		info.add("");
		info.add("Items Cpu Time");
		info.add(Text.longTimeToDHMSM(cpuTotalTimes));
		info.add("Items Cpu Avg");
		info.add(Text.longTimeToDHMSM(cpuTotalTimes / div));
		
		info.add("Items Net Time");
		info.add(Text.longTimeToDHMSM(netTotalTimes));
		info.add("Items Net Avg");
		info.add(Text.longTimeToDHMSM(netTotalTimes / div));
		
		info.add("Items IO Time");
		info.add(Text.longTimeToDHMSM(ioTotalTimes));
		info.add("Items IO Avg");
		info.add(Text.longTimeToDHMSM(ioTotalTimes / div));
		
		info.add("Items Total Time");
		info.add(Text.longTimeToDHMSM(cpuTotalTimes + netTotalTimes + ioTotalTimes));
		info.add("Items Avg Time");
		info.add(Text.longTimeToDHMSM((cpuTotalTimes + netTotalTimes + ioTotalTimes) / div));
		
		info.add("");
		info.add("");
		info.add("Added");
		info.add(addedDateTime);
		info.add("Started");
		info.add(startDateTime);
		info.add("Finished");
		info.add(endDateTime);
		info.add("Run Time");
		info.add(Text.longTimeToDHMS(getRunTime()));
		
		return info.toArray(new String[info.size()]);
	}
	
	public enum BatchPriority
	{
		HIGH("HIGH"), STANDARD("Standard");
		
		private final String name;
		
		private BatchPriority(String name)
		{
			this.name = name;
		}
		
		public String toString()
		{
			return name;
		}
	}
	
	public BatchPriority getPriority()
	{
		return priority;
	}
	
	public void setPriority(BatchPriority priority)
	{
		this.priority = priority;
	}
	
	public void setStatus(boolean status)
	{
		this.status = status;
	}
	
	public boolean getStatus()
	{
		return status;
	}
	
	public String getFinished()
	{
		return endDateTime;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public void setPosition(int position)
	{
		this.position = position;
	}
	
	public String getFileName()
	{
		return batchFileName;
	}
	
	private String getFileName(String filePath)
	{
		return Paths.get(filePath).getFileName().toString();
	}
	
	private String getPath(String filePath)
	{
		return Paths.get(filePath).getParent().toString();
	}
	
	public boolean isFinished()
	{
		if(!needGenerated)
		{
			return getCompleted() == getBatchItems();
		}
		
		return false;
		
	}
	
	public byte[] getItemConfig(String fileHash)
	{
		return itemDiskCache.getFile(fileHash);
	}
}
