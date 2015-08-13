package jCompute.Batch;

import jCompute.Datastruct.List.Interface.StoredQueuePosition;
import jCompute.Datastruct.cache.DiskCache;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.ScenarioManager;
import jCompute.Scenario.ConfigurationInterpreter;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;
import jCompute.util.FileUtil;
import jCompute.util.Text;

import java.io.BufferedOutputStream;
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
	
	// Item Generation
	private boolean needGenerated = true;
	private boolean generating = false;
	private float generationProgress = 0;
	
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
	private long ioTotalTimes;
	private long lastCompletedItemTime;
	
	// Enable / Disable writing the generated statistic files to disk
	private boolean storeStats;
	
	// Write stats to a single archive or directories with sub archives
	private final int BOS_DEFAULT_BUFFER_SIZE = 512;
	private int bosBufferSize;
	private boolean statsMethodSingleArchive;
	private int singleArchiveCompressionLevel;
	private final ExportFormat statExportFormat = ExportFormat.CSV;
	
	// The export dir for stats
	private String batchStatsExportDir;
	private ZipOutputStream resultsZipOut;
	
	// Item log writer
	private final int BW_BUFFER_SIZE = 1024 * 1000;
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
	
	// Batch Finished Status
	private boolean finished;
	
	// Get Batch Info Cache (Non Changing Data / All Final Info )
	private ArrayList<String> infoCache;
	
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
		ioTotalTimes = 0;
		
		addedDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		
		// Item management data structures
		queuedItems = new LinkedList<BatchItem>();
		activeItems = new ArrayList<BatchItem>();
		
		// Active Items
		active = 0;
		
		finished = false;
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
	 * @return boolean
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
				bosBufferSize = batchConfigProcessor.getIntValue("Stats", "BufferSize", BOS_DEFAULT_BUFFER_SIZE);
				
				log.info("Store Stats " + storeStats);
				log.info("Single Archive " + statsMethodSingleArchive);
				log.info("BufferSize " + bosBufferSize);
				log.info("Compression Level " + singleArchiveCompressionLevel);
				log.info("InfoLog " + infoLogEnabled);
				log.info("ItemLog " + itemLogEnabled);
				
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
	
	public void generateItems()
	{
		// Don't generate if generating
		if(generating)
		{
			return;
		}
		
		generating = true;
		
		// This avoids a GUI lockup during item generation
		Thread backgroundGenerate = new Thread(new Runnable()
		{
			public void run()
			{
				log.info("Generating Items for Batch " + batchId);
				
				log.info("Created an Item DiskCache for Batch " + batchId);
				// Create DiskCache
				itemDiskCache = new DiskCache(batchStatsExportDir, Deflater.BEST_SPEED);
				
				// Get a count of the parameter groups.
				int parameterGroups = batchConfigProcessor.getSubListSize("Parameters", "Parameter");
				
				// Array to hold the parameter type (group/single)
				String parameterType[] = new String[parameterGroups];
				
				// Array to hold the path to group/parameter
				String path[] = new String[parameterGroups];
				
				// Array to hold the unique identifier for the group.
				groupName = new String[parameterGroups];
				
				// Array to hold the parameter name that will be changed.
				parameterName = new String[parameterGroups];
				
				// Base values of each parameter
				double baseValue[] = new double[parameterGroups];
				
				// Increment values of each parameter
				double increment[] = new double[parameterGroups];
				
				// steps for each parameter
				int step[] = new int[parameterGroups];
				
				// Batch Info Parameters list
				parameters = new ArrayList<String>();
				
				// Iterate over the detected parameters and read values
				String section = "";
				for(int p = 0; p < parameterGroups; p++)
				{
					// Generate the parameter path in the xml array (0),(1) etc
					log.debug("Parameter Group : " + p);
					section = "Parameters.Parameter(" + p + ")";
					
					// Get the type (group/single)
					parameterType[p] = batchConfigProcessor.getStringValue(section, "Type");
					
					// Populate the path to this parameter.
					path[p] = batchConfigProcessor.getStringValue(section, "Path");
					
					// Store the group name if this parameter changes one in a
					// group.
					groupName[p] = "";
					if(parameterType[p].equalsIgnoreCase("Group"))
					{
						groupName[p] = batchConfigProcessor.getStringValue(section, "GroupName");
					}
					
					// Store the name of the paramter to change
					parameterName[p] = batchConfigProcessor.getStringValue(section, "ParameterName");
					
					// Base value
					baseValue[p] = batchConfigProcessor.getDoubleValue(section, "Intial");
					
					// Increment value
					increment[p] = batchConfigProcessor.getDoubleValue(section, "Increment");
					
					// Steps for each values
					step[p] = batchConfigProcessor.getIntValue(section, "Combinations");
				}
				
				// Calculate Total Combinations
				int combinations = 1;
				for(int s = 0; s < step.length; s++)
				{
					combinations *= step[s];
				}
				
				// When to increment values in the combos
				int incrementMods[] = new int[parameterGroups];
				int div = combinations;
				for(int p = 0; p < parameterGroups; p++)
				{
					div = div / step[p];
					// Increment depending on the step div
					incrementMods[p] = div;
					log.info("p " + p + " increments every " + incrementMods[p]);
				}
				
				// Roll over value of the max combination for each parameter
				double maxValue[] = new double[parameterGroups];
				for(int p = 0; p < parameterGroups; p++)
				{
					maxValue[p] = baseValue[p] + (increment[p] * (step[p] - 1));
				}
				
				// Init combo initial starting bases
				double value[] = new double[parameterGroups];
				for(int p = 0; p < parameterGroups; p++)
				{
					value[p] = baseValue[p];
				}
				
				// Create and populate Results Zip archive with Directories
				String zipFileName = batchStatsExportDir + File.separator + "results.zip";
				log.info("Zip Archive : " + zipFileName);
				
				if(storeStats)
				{
					if(statsMethodSingleArchive)
					{
						try
						{
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipFileName), bosBufferSize);
							
							resultsZipOut = new ZipOutputStream(bos);
							
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
				
				// Combo x,y,z... parameter spatial grid position
				int pos[] = new int[parameterGroups];
				
				// The temp scenario used to generate the xml.
				ConfigurationInterpreter temp;
				generationProgress = 0;
				
				for(int c = 0; c < combinations; c++)
				{
					if(storeStats)
					{
						if(statsMethodSingleArchive)
						{
							// Create Sub Directories in Zip Archive or
							// Directory
							try
							{
								// Create Item Directories
								resultsZipOut.putNextEntry(new ZipEntry(Integer.toString(c) + "/"));
								resultsZipOut.closeEntry();
								
								for(int sid = 1; sid < itemSamples + 1; sid++)
								{
									// Create Sample Directories
									resultsZipOut.putNextEntry(new ZipEntry(Integer.toString(c) + "/" + Integer.toString(sid) + "/"));
									resultsZipOut.closeEntry();
								}
								
							}
							catch(IOException e)
							{
								log.error("Could not create create directory " + c + " in " + zipFileName);
								
								e.printStackTrace();
							}
						}
						else
						{
							// Create the item export dir
							FileUtil.createDirIfNotExist(batchStatsExportDir + File.separator + c);
							
							for(int sid = 1; sid < itemSamples + 1; sid++)
							{
								String fullExportPath = batchStatsExportDir + File.separator + c + File.separator + sid;
								
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
					itemName.append("Combo " + c);
					
					StringBuilder comboPosString = new StringBuilder();
					
					// DebugLogger.output(temp.getScenarioXMLText());
					comboPosString.append("ComboPos(");
					ArrayList<Integer> tempCoord = new ArrayList<Integer>();
					ArrayList<Double> tempCoordValues = new ArrayList<Double>();
					
					for(int p = 0; p < parameterGroups; p++)
					{
						// Increment this parameter? (avoid increment the first
						// combo c>0)
						if((c) % incrementMods[p] == 0 && c > 0)
						{
							pos[p] = (pos[p] + 1) % step[p];
							
							value[p] = (value[p] + increment[p]);
							
							// Has a roll over occured
							if(value[p] > maxValue[p])
							{
								value[p] = baseValue[p];
							}
						}
						
						// This is a group parameter
						if(parameterType[p].equalsIgnoreCase("Group"))
						{
							// Log line middle
							itemName.append(" " + path[p] + "." + groupName[p] + "." + parameterName[p] + " " + value[p]);
							
							int groups = temp.getSubListSize(path[p]);
							
							// Find the correct group that matches the name
							for(int sg = 0; sg < groups; sg++)
							{
								String groupSection = path[sg] + "(" + sg + ")";
								
								String searchGroupName = temp.getStringValue(groupSection, "Name");
								
								// Found Group
								if(searchGroupName.equalsIgnoreCase(groupName[p]))
								{
									// Combo / targetGroupName / Current
									// GroupName
									// DebugLogger.output(c + " " +
									// targetGroupName
									// +
									// " " + GroupName[p]);
									
									// The parameter we want
									// DebugLogger.output(ParameterName[p]);
									
									// String target =
									// Path[p]+"."+ParameterName[p];
									// String target =
									// groupSection+"."+ParameterName[p];
									
									// Current Value in XML
									// DebugLogger.output("Current Value " +
									// temp.getIntValue(groupSection,ParameterName[p]));
									
									// Find the datatype to change
									String dtype = temp.findDataType(path[p] + "." + parameterName[p]);
									
									// Currently only decimal and integer are
									// supported.
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
										temp.changeValue(groupSection, parameterName[p], value[p]);
									}
									else if(dtype.equals("integer"))
									{
										// The configuration file wants Integer
										// values - Cast floats to ints
										temp.changeValue(groupSection, parameterName[p], (int) value[p]);
									}
									else
									{
										// This will not happen unless there is
										// a
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
							itemName.append(" " + path[p] + "." + parameterName[p] + " " + value[p]);
							
							// Fine the datatype for this parameter
							String dtype = temp.findDataType(path[p] + "." + parameterName[p]);
							
							// Currently only decimal and integer are used.
							if(dtype.equals("boolean"))
							{
								temp.changeValue(path[p], parameterName[p], new Boolean(true));
							}
							else if(dtype.equals("string"))
							{
								temp.changeValue(path[p], parameterName[p], " ");
							}
							else if(dtype.equals("decimal"))
							{
								temp.changeValue(path[p], parameterName[p], value[p]);
							}
							else if(dtype.equals("integer"))
							{
								temp.changeValue(path[p], parameterName[p], (int) value[p]);
							}
							else
							{
								// This will not happen unless there is a new
								// datatype
								// added to the XML standards schema.
								log.error("Unknown XML DTYPE : " + dtype);
							}
							
						}
						
						// Set the pos and val
						tempCoord.add(pos[p]);
						tempCoordValues.add(value[p]);
						
						comboPosString.append(String.valueOf(pos[p]));
						if(p < (parameterGroups - 1))
						{
							comboPosString.append('x');
						}
						
					}
					// Log line end
					comboPosString.append(")");
					log.debug(comboPosString.toString());
					log.debug(itemName.toString());
					
					addBatchItem(itemSamples, c, itemName.toString(), temp.getScenarioXMLText(), tempCoord, tempCoordValues);
					
					generationProgress = ((float) c / (float) combinations) * 100f;
					
					
					// Avoid div by zero on <10 combinations
					if(combinations > 10)
					{
						// Every 10%
						if((c % (combinations / 10)) == 0)
						{
							log.info((int) generationProgress + "%");
						}
					}

					// END COMBO
				}
				
				generationProgress = 100;
				log.info((int) generationProgress + "%");
				
				// All the items need to get processed, but the ett is
				// influenced by
				// the
				// order (randomise it in an attempt to reduce influence)
				Collections.shuffle(queuedItems);
				
				needGenerated = false;
				
				log.info("Generated Items Batch " + batchId);
				
			}
		});
		backgroundGenerate.setName("Item Generation Background Thread Batch " + batchId);
		backgroundGenerate.start();
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
				itemLog = new PrintWriter(
						new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + "ItemLog.log", true), BW_BUFFER_SIZE));
						
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
				log.error("Could not created item log file");
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
		
		batchStatsExportDir = baseExportDir + File.separator + date + "@" + time + "[" + batchId + "][" + itemSamples + "-" + uniqueItems
				+ "-" + batchItems + "-" + maxSteps + "] " + batchName;
				
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
		
		// For estimated complete time calculation
		cpuTotalTimes += item.getComputeTime();
		
		if(itemLogEnabled)
		{
			itemLog.println("[+Item]");
			itemLog.println("IID=" + item.getItemId());
			itemLog.println("SID=" + item.getSampleId());
			ArrayList<Integer> coords = item.getCoordinates();
			ArrayList<Double> coordsValues = item.getCoordinatesValues();
			for(int c = 0; c < coords.size(); c++)
			{
				itemLog.println("[+Coordinate]");
				itemLog.println("Pos=" + coords.get(c));
				itemLog.println("Value=" + coordsValues.get(c));
				itemLog.println("[-Coordinate]");
			}
			itemLog.println("Hash=" + item.getItemHash());
			itemLog.println("RunTime=" + item.getComputeTime());
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
				String fullExportPath = batchStatsExportDir + File.separator + item.getItemId() + File.separator + item.getSampleId();
				
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
						resultsZipOut.putNextEntry(new ZipEntry(item.getItemId() + "/" + "itemconfig-" + item.getItemHash() + ".xml"));
						
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
						PrintWriter configFile = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator
								+ item.getItemId() + File.separator + "itemconfig-" + item.getItemHash() + ".xml", true)));
								
						configFile.write(new String(itemDiskCache.getFile(item.getItemHash()), "ISO-8859-1"));
						configFile.flush();
						configFile.close();
					}
					catch(IOException e)
					{
						log.error("Could not save item " + item.getItemId() + " config (Batch " + item.getBatchId() + ")");
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
						resultsZipOut.flush();
						resultsZipOut.close();
						
						log.info("Closed Results Zip for " + "Batch " + batchId);
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
					PrintWriter infoLog = new PrintWriter(
							new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + "InfoLog.xml", true)));
					infoLog.println("<Batch>");
					infoLog.println("<ID>" + batchId + "</ID>");
					infoLog.println("<ScenarioType>" + type + "</ScenarioType>");
					infoLog.println("<Description>" + batchName + "</Description>");
					infoLog.println("<BaseFile>" + baseScenarioFileName + "</BaseFile>");
					infoLog.println("<Start>" + startDateTime + "</Start>");
					infoLog.println("<Finished>" + endDateTime + "</Finished>");
					infoLog.println(
							"<TotalTime>" + jCompute.util.Text.longTimeToDHMS(System.currentTimeMillis() - startTime) + "</TotalTime>");
					infoLog.println("<Batch>");
					infoLog.flush();
					infoLog.close();
				}
				catch(IOException e)
				{
					log.error("Error writing info log");
				}
			}
			
			log.info("Clearing Batch " + batchId + " DiskCache");
			itemDiskCache.clear();
			
			performBatchFinishedCompaction();
			
			finished = true;
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
			ArrayList<Double> coordinatesValues)
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
	
	/*
	 * Batch Row Fields Getters
	 */
	public int getBatchId()
	{
		return batchId;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getBaseScenarioFileName()
	{
		return baseScenarioFileName;
	}
	
	public int getBatchItems()
	{
		return batchItems;
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
			return ((cpuTotalTimes + ioTotalTimes) / completed) * (batchItems - completed) / active;
		}
		
		return 0;
	}
	
	/*
	 * Batch Info Getter
	 */
	public String[] getBatchInfo()
	{
		batchLock.acquireUninterruptibly();
		
		ArrayList<String> info = new ArrayList<String>();
		
		if(!finished)
		{
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
					infoCache.add("Buffer Size");
					infoCache.add(String.valueOf(bosBufferSize));
					infoCache.add("Compression Level");
					infoCache.add(String.valueOf(singleArchiveCompressionLevel));
					infoCache.add("Info Log");
					infoCache.add(infoLogEnabled == true ? "Enabled" : "Disabled");
					infoCache.add("Item Log");
					infoCache.add(itemLogEnabled == true ? "Enabled" : "Disabled");
				}
			}
			
			// Add The Cache Header
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
				info.add("Buffer Size");
				info.add(String.valueOf(bosBufferSize));
				info.add("Compression Level");
				info.add(String.valueOf(singleArchiveCompressionLevel));
				info.add("Info Log");
				info.add(infoLogEnabled == true ? "Enabled" : "Disabled");
				info.add("Item Log");
				info.add(itemLogEnabled == true ? "Enabled" : "Disabled");
			}
			
			info.add("");
			info.add("");
			info.add("Items Generated");
			info.add(needGenerated == false ? "Yes" : "No");
			info.add("Generation Progress");
			info.add(String.valueOf(generationProgress));
			
			if(!needGenerated)
			{
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
				
				info.add("Items IO Time");
				info.add(Text.longTimeToDHMSM(ioTotalTimes));
				info.add("Items IO Avg");
				info.add(Text.longTimeToDHMSM(ioTotalTimes / div));
				
				info.add("Items Total Time");
				info.add(Text.longTimeToDHMSM(cpuTotalTimes + ioTotalTimes));
				info.add("Items Avg Time");
				info.add(Text.longTimeToDHMSM((cpuTotalTimes + ioTotalTimes) / div));
			}
			
			info.add("");
			info.add("");
			info.add("Added");
			info.add(addedDateTime);
			info.add("Started");
			info.add(startDateTime);
			if(!needGenerated)
			{
				info.add("Est Finished");
				info.add(jCompute.util.Text.timeNowPlus(getETT()));
				info.add("Finished");
				info.add(endDateTime);
				info.add("Run Time");
				info.add(Text.longTimeToDHMS(getRunTime()));
			}
		}
		else
		{
			info.addAll(infoCache);
		}
		
		batchLock.release();
		
		return info.toArray(new String[info.size()]);
	}
	
	private void performBatchFinishedCompaction()
	{
		log.info("Compacting Batch Info");
		
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
		
		// Add The parameters
		infoCache.addAll(parameters);
		
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
		infoCache.add("Buffer Size");
		infoCache.add(String.valueOf(bosBufferSize));
		infoCache.add("Compression Level");
		infoCache.add(String.valueOf(singleArchiveCompressionLevel));
		infoCache.add("Info Log");
		infoCache.add(infoLogEnabled == true ? "Enabled" : "Disabled");
		infoCache.add("Item Log");
		infoCache.add(itemLogEnabled == true ? "Enabled" : "Disabled");
		
		infoCache.add("");
		infoCache.add("");
		infoCache.add("Items Generated");
		infoCache.add(needGenerated == false ? "Yes" : "No");
		infoCache.add("Generation Progress");
		infoCache.add(String.valueOf(generationProgress));
		
		infoCache.add("");
		infoCache.add("");
		infoCache.add("Active Items");
		infoCache.add(String.valueOf(getActiveItemsCount()));
		
		infoCache.add("Items Completed");
		infoCache.add(String.valueOf(completed));
		infoCache.add("Items Requested");
		infoCache.add(String.valueOf(itemsRequested));
		infoCache.add("Items Returned");
		infoCache.add(String.valueOf(itemsReturned));
		
		int div = 1;
		if(completed > 0)
		{
			div = completed;
		}
		
		infoCache.add("");
		infoCache.add("");
		infoCache.add("Items Cpu Time");
		infoCache.add(Text.longTimeToDHMSM(cpuTotalTimes));
		infoCache.add("Items Cpu Avg");
		infoCache.add(Text.longTimeToDHMSM(cpuTotalTimes / div));
		
		infoCache.add("Items IO Time");
		infoCache.add(Text.longTimeToDHMSM(ioTotalTimes));
		infoCache.add("Items IO Avg");
		infoCache.add(Text.longTimeToDHMSM(ioTotalTimes / div));
		
		infoCache.add("Items Total Time");
		infoCache.add(Text.longTimeToDHMSM(cpuTotalTimes + ioTotalTimes));
		infoCache.add("Items Avg Time");
		infoCache.add(Text.longTimeToDHMSM((cpuTotalTimes + ioTotalTimes) / div));
		
		infoCache.add("");
		infoCache.add("");
		infoCache.add("Added");
		infoCache.add(addedDateTime);
		infoCache.add("Started");
		infoCache.add(startDateTime);
		infoCache.add("Est Finished");
		infoCache.add(jCompute.util.Text.timeNowPlus(getETT()));
		infoCache.add("Finished");
		infoCache.add(endDateTime);
		infoCache.add("Run Time");
		infoCache.add(Text.longTimeToDHMS(getRunTime()));
		infoCache.add("Run Time (s)");
		
		infoCache.add(String.valueOf(((double) getRunTime() / (double) 1000)));
		
		// Batch Attributes
		// batchName = null;
		// priority = null;
		baseScenarioFileName = null;
		baseScenarioFileName = null;
		parameters = null;
		
		// Set if this batch's items can be processed (stop/start)
		type = null;
		
		// For human readable date/time info
		addedDateTime = null;
		
		// Log - total time calc
		startDateTime = null;
		// endDateTime = null;
		
		// The export dir for stats
		batchStatsExportDir = null;
		resultsZipOut = null;
		
		// Item log writer
		itemLog = null;
		
		// Used for combination and for saving axis names
		parameterName = null;
		groupName = null;
		
		// Our Queue of Items yet to be processed
		queuedItems = null;
		
		// The active Items currently being processed.
		activeItems = null;
		
		// Get Batch Info Cache (Non Changing Data / All Final Info )
		// infoCache =null;
		
		// The Batch Configuration Text
		batchConfigText = null;
		
		// The Configuration Processor
		batchConfigProcessor = null;
		
		// The base scenario
		basePath = null;
		
		// Base scenario text
		baseScenarioText = null;
		
		// Disk Cache for Items
		itemDiskCache = null;
		
		log.info("Batch Info Compacted");
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
	
	public ExportFormat getStatExportFormat()
	{
		return statExportFormat;
	}
}
