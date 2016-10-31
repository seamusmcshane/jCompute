package jcompute.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.itemgenerator.ItemGenerator;
import jcompute.batch.logfileprocessor.logformatprocessor.ItemLogTextv2Format;
import jcompute.batch.logger.InfoLogger;
import jcompute.datastruct.cache.DiskCache;
import jcompute.datastruct.list.StoredQueuePosition;
import jcompute.results.ResultExporter;
import jcompute.results.ResultExporter.ExportFormat;
import jcompute.scenario.ConfigurationInterpreter;
import jcompute.scenario.ScenarioInf;
import jcompute.scenario.ScenarioPluginManager;
import jcompute.timing.TimerObj;
import jcompute.util.FileUtil;
import jcompute.util.TimeString;
import jcompute.util.TimeString.TimeStringFormat;

public class Batch implements StoredQueuePosition
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(Batch.class);
	
	// needInitialized records the start and end of init()
	// Allows batch manager logic to handle a batch that needs init() or skip init() call that is already init. - get via needsInit()
	private AtomicBoolean needInitialized = new AtomicBoolean(true);
	
	// Initialising is for thread safety of the method init() and avoiding calling init() when already initialising - get via isInit()
	private AtomicBoolean initialising = new AtomicBoolean(false);
	
	// Does this batch need items generated.
	private AtomicBoolean needGenerated = new AtomicBoolean(true);
	private double[] generationProgress = new double[1];
	
	// Batch Attributes
	private int position;
	private int batchId;
	private String batchName;
	private String baseScenarioFileName;
	private String batchFileName;
	private ArrayList<String> parameters;
	
	// Set if this batch's items can be processed (stop/start)
	private boolean enabled;
	
	// Or if it has failed
	private boolean failed;
	
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
	private long startTimeMillis;
	private String startDateTime = "Not Started";
	private String endDateTime = "Not Finished";
	
	// Items processing times and eta calculation
	private long cpuTotalTimes;
	private long ioTotalTimes;
	private long lastCompletedItemTimeMillis;
	
	// Enable / Disable writing the generated statistic files to disk
	private boolean storeStats;
	
	// Write stats to a single archive or directories with sub archives
	private final int BOS_DEFAULT_BUFFER_SIZE = 8192;
	private int bosBufferSize;
	private boolean statsMethodSingleArchive;
	private int singleArchiveCompressionLevel;
	private final ExportFormat statExportFormat = ExportFormat.CSV;
	
	// The export dir for stats
	private String batchStatsExportDir = "";
	private ZipOutputStream resultsZipOut;
	
	// Item log writer
	private final int BW_BUFFER_SIZE = 1024 * 1000;
	private PrintWriter itemLog;
	
	// Item log version
	private final int ITEM_LOG_VERSION = 2;
	
	private boolean itemLogEnabled;
	
	// Used for combination and for saving axis names
	private String parameterName[];
	private String groupName[];
	
	// Simple Batch Log
	private boolean infoLogEnabled;
	
	private ItemGenerator itemGenerator;
	private long itemGenerationTime;
	
	// Our Queue of Items yet to be processed
	private LinkedList<BatchItem> queuedItems;
	
	// The active Items currently being processed.
	private ArrayList<BatchItem> activeItems;
	private int active;
	
	// Completed items count
	private int itemsCompleted = 0;
	
	// Info Cache (Non Changing Data)
	private ArrayList<String> infoCache;
	
	// Compacted info is created end of the batch (from now static info) and used in place of the live info normally generated when getBatchInfo is called.
	private String[] compactedInfo;
	private boolean infoCompacted;
	
	// The base directory path
	private String baseDirectoryPath;
	
	// Base scenario text
	private String baseScenarioText;
	
	// Disk Cache for Items
	private DiskCache itemDiskCache;
	
	// To protect our shared variables/data structures
	private Semaphore batchLock = new Semaphore(1, false);
	
	public Batch(int batchId)
	{
		this.batchId = batchId;
		
		// Processing Times
		cpuTotalTimes = 0;
		ioTotalTimes = 0;
		
		addedDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		
		// Item management data structures
		queuedItems = new LinkedList<BatchItem>();
		activeItems = new ArrayList<BatchItem>();
		
		// Active Items
		active = 0;
		
		// Not enabled
		enabled = false;
		
		// Batch info is not compacted until batch is complete
		infoCompacted = false;
		compactedInfo = null;
		
		failed = false;
	}
	
	public boolean loadConfig(String filePath)
	{
		// Path String
		if(filePath == null)
		{
			log.error("Batch loadConfig file path is null");
			return false;
		}
		
		log.info("New Batch based on : " + filePath);
		
		batchFileName = FileUtil.getFileName(filePath);
		
		// Failed to read a file name
		if(batchFileName == null)
		{
			log.error("Failed to find file");
			
			return false;
		}
		
		log.info("File : " + batchFileName);
		
		// Get the ext index.
		int extStartIndex = batchFileName.lastIndexOf('.');
		
		// No ext? 0=first char,-1 = no .
		if(extStartIndex <= 0)
		{
			log.error("Cannot detect a file extension");
			
			return false;
		}
		
		try
		{
			// Use the value before the ext as the batch name
			batchName = batchFileName.substring(0, extStartIndex);
		}
		catch(IndexOutOfBoundsException e)
		{
			log.error("Cannot set batch name");
			
			e.printStackTrace();
			
			return false;
		}
		
		baseDirectoryPath = FileUtil.getPath(filePath);
		
		// Could fail if there is no parent.
		if(baseDirectoryPath == null)
		{
			log.error("Failed to set base directory path - could not find parent of file");
			
			return false;
		}
		
		log.info("Base Path : " + baseDirectoryPath);
		
		// The Batch Configuration Text
		String batchConfigText = jcompute.util.JCText.textFileToString(filePath);
		
		// Null if there was an error reading the batch file in
		if(batchConfigText == null)
		{
			log.error("Failed to read file into memory");
			
			return false;
		}
		
		// Last check - set the batch status to the outcome, if ok the batch will be enabled.
		enabled = processBatchConfig(batchConfigText);
		
		return enabled;
	}
	
	/**
	 * Processes and Validates the batch config text
	 *
	 * @param fileText
	 * @return boolean
	 * @throws IOException
	 */
	private boolean processBatchConfig(String batchConfigText)
	{
		boolean status = true;
		
		batchLock.acquireUninterruptibly();
		
		log.info("Processing BatchFile");
		
		// The Configuration Processor
		ConfigurationInterpreter batchConfigProcessor = new ConfigurationInterpreter();
		
		batchConfigProcessor.loadConfig(batchConfigText);
		
		status = checkBatchFile(batchConfigProcessor);
		
		if(status)
		{
			// Logs + Stats
			storeStats = batchConfigProcessor.getBooleanValue("Stats", "Store");
			
			statsMethodSingleArchive = batchConfigProcessor.getBooleanValue("Stats", "SingleArchive");
			singleArchiveCompressionLevel = batchConfigProcessor.getIntValue("Stats", "CompressionLevel");
			infoLogEnabled = batchConfigProcessor.getBooleanValue("Log", "InfoLog");
			itemLogEnabled = batchConfigProcessor.getBooleanValue("Log", "ItemLog");
			bosBufferSize = batchConfigProcessor.getIntValue("Stats", "BufferSize", BOS_DEFAULT_BUFFER_SIZE);
			
			// How many times to run each batchItem.
			itemSamples = batchConfigProcessor.getIntValue("Config", "ItemSamples");
			
			log.info("Store Stats " + storeStats);
			log.info("Single Archive " + statsMethodSingleArchive);
			log.info("BufferSize " + bosBufferSize);
			log.info("Compression Level " + singleArchiveCompressionLevel);
			log.info("InfoLog " + infoLogEnabled);
			log.info("ItemLog " + itemLogEnabled);
			log.info("ItemSamples " + itemSamples);
			
			ScenarioInf baseScenario = processAndCheckBaseScenarioFile(batchConfigProcessor);
			
			if(baseScenario != null)
			{
				maxSteps = baseScenario.getEndEventTriggerValue("StepCount");
				type = baseScenario.getScenarioType();
				
				if(itemSamples > 0)
				{
					// Create a generator (type HC for now and too many vars)
					itemGenerator = baseScenario.getItemGenerator(batchId, batchName, batchConfigProcessor, queuedItems, itemSamples, generationProgress,
					baseScenarioText, storeStats, statsMethodSingleArchive, singleArchiveCompressionLevel, bosBufferSize);
					
					if(itemGenerator == null)
					{
						log.error("Scenario does not support batch items");
						
						status = false;
					}
					
					// itemGenerator = new SAPPItemGenerator(batchId, batchName, batchConfigProcessor, queuedItems, itemSamples, generationProgress,
					// baseScenarioText, storeStats, statsMethodSingleArchive, singleArchiveCompressionLevel, bosBufferSize);
				}
				else
				{
					log.error("ItemGenerator did not create any items");
					
					status = false;
				}
			}
			else
			{
				log.error("Processing batch config - error found checking base scenario.");
				
				status = false;
			}
		}
		
		batchLock.release();
		
		return status;
	}
	
	/**
	 * Validates the Base Scenario file used for the batch items.
	 *
	 * @param batchConfigProcessor
	 * @param fileText
	 * @return
	 */
	private ScenarioInf processAndCheckBaseScenarioFile(ConfigurationInterpreter batchConfigProcessor)
	{
		log.info("Processing BaseScenarioFile");
		
		boolean status = true;
		
		// Null returned if any problems occur
		ScenarioInf scenario = null;
		
		// Store the base fileName
		baseScenarioFileName = batchConfigProcessor.getStringValue("Config", "BaseScenarioFileName");
		
		// Assumes the file is in the same dir as the batch file
		String baseScenaroFilePath = baseDirectoryPath + File.separator + baseScenarioFileName;
		
		log.debug("Base Scenario File : " + baseScenaroFilePath);
		
		// Attempt to load the text into a string
		String tempText = jcompute.util.JCText.textFileToString(baseScenaroFilePath);
		
		// No null if the text from the file has been loaded
		if(tempText != null)
		{
			// Use a ConfigurationInterpreter to prevent a BatchStats/ItemStats mismatch which could cause a stats req for stats that do not exist
			ConfigurationInterpreter tempIntrp = new ConfigurationInterpreter();
			
			// Is the base scenario currently a valid scenario file
			if(tempIntrp.loadConfig(tempText))
			{
				// Check if Batch stats are disabled globally
				if(!storeStats)
				{
					// If so remove the Statistics section from XML config (disabling them on each item)
					tempIntrp.removeSection("Statistics");
				}
				else
				{
					// Check if the base scenario has a statistics section
					if(!tempIntrp.hasSection("Statistics"))
					{
						status = false;
						
						log.error("The batch has statistics enabled but the base scenario does not.");
					}
					else
					{
						status = false;
						
						// We have a statistics section but are there any stats and is one enabled!
						if(tempIntrp.atLeastOneElementEqualValue("Statistics.Stat", "Enabled", true))
						{
							status = true;
						}
						
						if(tempIntrp.atLeastOneElementEqualValue("Statistics.BDFC", "Enabled", true))
						{
							status = true;
						}
						
						if(!status)
						{
							log.error("The batch has statistics enabled but there are none enabled in the base scenario");
						}
					}
				}
			}
			
			if(status)
			{
				// Store the scenario text
				baseScenarioText = tempIntrp.getText();
			}
			
			// Finally create a real Scenario as the final test
			if(baseScenarioText != null)
			{
				scenario = ScenarioPluginManager.getScenario(baseScenarioText);
			}
		}
		
		return scenario;
	}
	
	public boolean isInit()
	{
		// We don't need init if we are initialising now.
		return initialising.get();
	}
	
	public boolean needsInit()
	{
		// If base does not need generated then it was already initialised
		return needInitialized.get();
	}
	
	/**
	 * This method is used for lazy initialisation of a batch. This prevents considerable up-front processor usage when more than 1 batch is added as we then
	 * avoid generating items
	 * till the batch is scheduled. It can safe considerable a mounts of memory and disk space where there are multiple batches queued (vs all generated when
	 * added).
	 */
	public void init()
	{
		// If currently initialising then abort the new attempt
		if(!initialising.compareAndSet(false, true))
		{
			// initialising?
			log.error("Attempted to initialise batch while still initialising - batch id " + batchId);
			
			return;
		}
		
		if(!needInitialized.get())
		{
			// Initialised
			log.error("Attempted to initialise batch when already initialised - batch id " + batchId);
			
			return;
		}
		
		// This background thread avoids a GUI lockup during item generation means need the atomic booleans for correctness
		Thread backgroundGenerate = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				TimerObj to = new TimerObj();
				
				to.startTimer();
				
				if(itemGenerator.generate())
				{
					log.info("Generated Items Batch " + batchId);
					
					// Super Class - Lazy Inits Storage
					itemDiskCache = itemGenerator.getItemDiskCache();
					batchStatsExportDir = itemGenerator.getBatchStatsExportDir();
					
					// Sub Class Lazy Inits Items
					batchItems = itemGenerator.getGeneratedItemCount();
					groupName = itemGenerator.getGroupNames();
					parameterName = itemGenerator.getParameterNames();
					parameters = itemGenerator.getParameters();
					resultsZipOut = itemGenerator.getResultsZipOut();
					
					// No longer needed
					itemGenerator = null;
					
					needInitialized.set(false);
					needGenerated.set(false);
					
					// No longer initialising.
					initialising.compareAndSet(true, false);
				}
				else
				{
					log.error("Item Generation Failed");
				}
				
				to.stopTimer();
				
				itemGenerationTime = to.getTimeTaken();
			}
		});
		backgroundGenerate.setName("Item Generation Background Thread Batch " + batchId);
		backgroundGenerate.start();
	}
	
	private boolean checkBatchFile(ConfigurationInterpreter batchConfigProcessor)
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
				
				switch(ITEM_LOG_VERSION)
				{
					case 1:
					{
						itemLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + "ItemLog.log", true),
						BW_BUFFER_SIZE));
					}
					break;
					case 2:
					{
						itemLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + "ItemLog.v2log", true),
						BW_BUFFER_SIZE));
					}
					break;
				}
				
				writeItemLogHeader(ITEM_LOG_VERSION, numCordinates);
				
			}
			catch(IOException e)
			{
				log.error("Could not create item log file in " + batchStatsExportDir);
			}
		}
	}
	
	public String getBatchStatsExportDir()
	{
		return batchStatsExportDir;
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
			startTimeMillis = System.currentTimeMillis();
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
	
	public void setItemComplete(BatchItem item, ResultExporter exporter)
	{
		batchLock.acquireUninterruptibly();
		
		long ioStart = System.currentTimeMillis();
		long ioEnd;
		
		log.debug("Setting Completed Sim " + item.getSimId() + " Item " + item.getItemId());
		
		// For estimated complete time calculation
		cpuTotalTimes += item.getComputeTime();
		
		if(itemLogEnabled)
		{
			writeItemLogItem(ITEM_LOG_VERSION, item);
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
				exporter.exportStats(fullExportPath, batchStatsExportDir, item.getItemId());
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
						resultsZipOut.putNextEntry(new ZipEntry(item.getItemId() + "/" + "itemconfig-" + item.getCacheIndex() + ".xml"));
						
						// Data
						resultsZipOut.write(itemDiskCache.getData(item.getCacheIndex()));
						
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
						PrintWriter configFile = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + item.getItemId()
						+ File.separator + "itemconfig-" + item.getCacheIndex() + ".xml", true)));
						
						configFile.write(new String(itemDiskCache.getData(item.getCacheIndex()), "ISO-8859-1"));
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
		
		itemsCompleted++;
		
		ioEnd = System.currentTimeMillis();
		
		ioTotalTimes += ioEnd - ioStart;
		
		lastCompletedItemTimeMillis = System.currentTimeMillis();
		
		if(itemsCompleted == batchItems)
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
				switch(ITEM_LOG_VERSION)
				{
					case 1:
					{
						// Close the Items Section in v1 log
						itemLog.println("[-Items]");
					}
					break;
				}
				
				// Close Batch Log
				itemLog.flush();
				itemLog.close();
			}
			
			endDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			
			// Write the info log
			if(infoLogEnabled)
			{
				try
				{
					InfoLogger infoLogger = new InfoLogger(batchStatsExportDir);
					
					infoLogger.writeGeneralInfo(batchId, batchName, type, baseScenarioFileName);
					
					infoLogger.writeItemInfo(batchItems, itemSamples, maxSteps, TimeString.timeInMillisAsFormattedString(itemGenerationTime,
					TimeStringFormat.DHMS));
					
					infoLogger.writeProcessedInfo(addedDateTime, startDateTime, endDateTime, startTimeMillis);
					
					infoLogger.writeCacheInfo(itemDiskCache);
					
					infoLogger.writeItemComputeInfo(itemsCompleted, cpuTotalTimes, ioTotalTimes);
					
					infoLogger.writeParameters(parameters);
					
					infoLogger.close();
				}
				catch(IOException e)
				{
					log.error("Error writing info log");
				}
			}
			
			performBatchFinishedCompaction();
		}
		
		batchLock.release();
	}
	
	public int getRemaining()
	{
		return queuedItems.size();
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
		return (int) (((double) itemsCompleted / (double) batchItems) * 100.0);
	}
	
	public int getCompleted()
	{
		return itemsCompleted;
	}
	
	public int getActiveItemsCount()
	{
		return activeItems.size();
	}
	
	public long getRunTime()
	{
		return lastCompletedItemTimeMillis - startTimeMillis;
	}
	
	public long getETT()
	{
		if((active > 0) && (itemsCompleted > 0))
		{
			return ((cpuTotalTimes + ioTotalTimes) / itemsCompleted) * ((batchItems - itemsCompleted) / active);
		}
		
		return 0;
	}
	
	private void addBatchInfoSectionHeader(boolean formated, boolean pad, String text, ArrayList<String> targetList)
	{
		// Pad with a line
		// The top section header is not padded, other main section headers are, other wise a subsection is not,
		if(pad)
		{
			targetList.add("");
			targetList.add("");
		}
		
		// The Section Header Text Formated
		if(formated)
		{
			targetList.add("<html><b>" + text + "</b></html>");
		}
		else
		{
			targetList.add(text);
		}
		targetList.add("");
	}
	
	private void addBatchDetailsHeaderToList(boolean formated, boolean batchGenerated, ArrayList<String> targetList)
	{
		addBatchInfoSectionHeader(formated, true, "Batch", targetList);
		
		targetList.add("Id");
		targetList.add(String.valueOf(batchId));
		targetList.add("Name");
		targetList.add(batchName);
		targetList.add("Scenario Type");
		targetList.add(type);
		
		if(batchGenerated)
		{
			// Values wont exist if not generated
			addBatchInfoSectionHeader(formated, false, "Item", targetList);
			targetList.add("Unique Items");
			targetList.add(String.valueOf(batchItems / itemSamples));
			targetList.add("Sample per Item");
			targetList.add(String.valueOf(itemSamples));
			targetList.add("Total Items");
			targetList.add(String.valueOf(batchItems));
			targetList.add("Max Steps");
			targetList.add(String.valueOf(maxSteps));
			
			addBatchInfoSectionHeader(formated, false, "Parameters", targetList);
			targetList.addAll(parameters);
		}
		
		addBatchInfoSectionHeader(formated, false, "Files/Paths", targetList);
		targetList.add("Batch");
		targetList.add(batchFileName);
		targetList.add("Scenario");
		targetList.add(baseScenarioFileName);
		targetList.add("Base");
		targetList.add(baseDirectoryPath);
		targetList.add("Export");
		targetList.add(batchStatsExportDir);
		
		addBatchInfoSectionHeader(formated, false, "Statistics", targetList);
		targetList.add("Stats Store");
		targetList.add(storeStats == true ? "Enabled" : "Disabled");
		targetList.add("Single Archive");
		targetList.add(statsMethodSingleArchive == true ? "Enabled" : "Disabled");
		targetList.add("Buffer Size");
		targetList.add(String.valueOf(bosBufferSize));
		targetList.add("Compression Level");
		targetList.add(String.valueOf(singleArchiveCompressionLevel));
		targetList.add("Info Log");
		targetList.add(infoLogEnabled == true ? "Enabled" : "Disabled");
		targetList.add("Item Log");
		targetList.add(itemLogEnabled == true ? "Enabled" : "Disabled");
	}
	
	private void addBatchDetailsQueueInfoToList(boolean formated, ArrayList<String> targetList)
	{
		addBatchInfoSectionHeader(formated, false, "Queue", targetList);
		
		targetList.add("Position");
		targetList.add(String.valueOf(position));
		targetList.add("Status");
		targetList.add(enabled == true ? "Enabled" : "Disabled");
	}
	
	private void addBatchDetailsGenerationProgressInfoToList(boolean formated, boolean batchGenerated, ArrayList<String> targetList)
	{
		addBatchInfoSectionHeader(formated, true, "Generation", targetList);
		
		targetList.add("Generated");
		targetList.add(batchGenerated ? "Yes" : "No");
		targetList.add("Progress");
		targetList.add(String.valueOf(generationProgress[0]));
		targetList.add("Time");
		targetList.add(TimeString.timeInMillisAsFormattedString(itemGenerationTime, TimeStringFormat.DHMS));
	}
	
	private void addBatchDetailsItemInfoToList(boolean formated, ArrayList<String> targetList)
	{
		addBatchInfoSectionHeader(formated, true, "Items", targetList);
		
		targetList.add("Active");
		targetList.add(String.valueOf(getActiveItemsCount()));
		targetList.add("Completed");
		targetList.add(String.valueOf(itemsCompleted));
		targetList.add("Requested");
		targetList.add(String.valueOf(itemsRequested));
		targetList.add("Returned");
		targetList.add(String.valueOf(itemsReturned));
	}
	
	private void addBatchDetailsDiskCacheInfoToList(boolean formated, ArrayList<String> targetList)
	{
		addBatchInfoSectionHeader(formated, true, "DiskCache", targetList);
		
		targetList.add("Cache Size");
		targetList.add(String.valueOf(itemDiskCache.getCacheSize()));
		targetList.add("Unique Ratio");
		targetList.add(String.valueOf(itemDiskCache.getUniqueRatio()));
		targetList.add("MemCacheEnabled");
		targetList.add(String.valueOf(itemDiskCache.getMemCacheEnabled()));
		
		addBatchInfoSectionHeader(formated, false, "MemCache", targetList);
		
		targetList.add("Size");
		targetList.add(String.valueOf(itemDiskCache.getMemCacheSize()));
		targetList.add("Requests");
		targetList.add(String.valueOf(itemDiskCache.getMemCacheRequests()));
		targetList.add("Hits");
		targetList.add(String.valueOf(itemDiskCache.getMemCacheHits()));
		targetList.add("Misses");
		targetList.add(String.valueOf(itemDiskCache.getMemCacheMisses()));
		targetList.add("Hit Ratio");
		targetList.add(String.valueOf(itemDiskCache.getMemCacheHitRatio()));
		targetList.add("Miss Ratio");
		targetList.add(String.valueOf(itemDiskCache.getMemCacheMissRatio()));
	}
	
	private void addBatchDetailsComputeInfoToList(boolean formated, ArrayList<String> targetList)
	{
		int div = 1;
		if(itemsCompleted > 0)
		{
			div = itemsCompleted;
		}
		
		addBatchInfoSectionHeader(formated, true, "Item Times", targetList);
		
		targetList.add("Total Cpu");
		targetList.add(TimeString.timeInMillisAsFormattedString((cpuTotalTimes), TimeStringFormat.DHMS));
		targetList.add("Avg Cpu");
		targetList.add(TimeString.timeInMillisAsFormattedString((cpuTotalTimes / div), TimeStringFormat.DHMS));
		
		targetList.add("Items IO");
		targetList.add(TimeString.timeInMillisAsFormattedString((ioTotalTimes), TimeStringFormat.DHMS));
		targetList.add("Items IO");
		targetList.add(TimeString.timeInMillisAsFormattedString((ioTotalTimes / div), TimeStringFormat.DHMS));
		
		targetList.add("Items Total");
		targetList.add(TimeString.timeInMillisAsFormattedString((cpuTotalTimes + ioTotalTimes), TimeStringFormat.DHMS));
		targetList.add("Items Avg");
		targetList.add(TimeString.timeInMillisAsFormattedString(((cpuTotalTimes + ioTotalTimes) / div), TimeStringFormat.DHMS));
	}
	
	private void addBatchDetailsTimeInfoToList(boolean formated, boolean batchGenerated, ArrayList<String> targetList)
	{
		addBatchInfoSectionHeader(formated, true, "Batch Times", targetList);
		
		targetList.add("Added");
		targetList.add(addedDateTime);
		targetList.add("Started");
		targetList.add(startDateTime);
		
		if(batchGenerated)
		{
			targetList.add("Est Finished");
			targetList.add(TimeString.timeNowPlus(getETT()));
			targetList.add("Finished");
			targetList.add(endDateTime);
			targetList.add("Running");
			targetList.add(TimeString.timeInMillisAsFormattedString(getRunTime(), TimeStringFormat.DHMS));
			targetList.add("Running (s)");
			targetList.add(String.valueOf(((double) getRunTime() / (double) 1000)));
		}
	}
	
	private void populateListWithBatchInfo(boolean formated, boolean batchIsGenerated, ArrayList<String> targetList)
	{
		// If failed add a message directly to the the top of the info list
		if(failed)
		{
			targetList.add("<html><b><font color='red'>Warning</font></b></html>");
			targetList.add("<html><b><font color='red'>Batch Failed</font></b></html>");
		}
		
		// Always add batches queue info
		addBatchDetailsQueueInfoToList(formated, targetList);
		
		// If generated cache some non changing values and reuse them each call
		if(batchIsGenerated)
		{
			// do once
			if(infoCache == null)
			{
				infoCache = new ArrayList<String>();
				
				// Add the batch header - batchIsGenerated used to skip any values that don't exist
				addBatchDetailsHeaderToList(formated, batchIsGenerated, infoCache);
				
				// Cache the final progress info - in this case batchIsGenerated is used for informational purposes
				addBatchDetailsGenerationProgressInfoToList(formated, batchIsGenerated, infoCache);
			}
			
			// Add the non changing cached values.
			targetList.addAll(infoCache);
			
			// item info.
			addBatchDetailsItemInfoToList(formated, targetList);
			
			// disk cache info.
			addBatchDetailsDiskCacheInfoToList(formated, targetList);
			
			// compute stats
			addBatchDetailsComputeInfoToList(formated, targetList);
			
			// Add the current time info minus any values that are valid yet (batchIsGenerated)
			addBatchDetailsTimeInfoToList(formated, batchIsGenerated, targetList);
		}
		else
		{
			// If not generated add reduced info and info each call.
			
			// Add the batch header - batchIsGenerated used to skip any values that don't exist
			addBatchDetailsHeaderToList(formated, batchIsGenerated, targetList);
			
			// Add progress each call (it updates until finished) - in this case batchIsGenerated is used for informational purposes
			addBatchDetailsGenerationProgressInfoToList(formated, batchIsGenerated, targetList);
		}
	}
	
	/*
	 * Batch Info Getter
	 */
	public String[] getBatchInfo()
	{
		batchLock.acquireUninterruptibly();
		
		// Formated array of strings (Field/Value)
		String[] batchInfo = null;
		
		// Check if the batch info is compacted.
		if(!infoCompacted)
		{
			// Not compacted - info must be generated and infoCache will be used for non changing values
			
			// The list that is returned
			ArrayList<String> info = new ArrayList<String>();
			
			// Has the batch been generated - Note !
			boolean batchIsGenerated = !needGenerated.get();
			
			populateListWithBatchInfo(true, batchIsGenerated, info);
			
			// Size the array.
			batchInfo = new String[info.size()];
			
			// Copy data to the array
			info.toArray(batchInfo);
		}
		else
		{
			// BatchInfo was compacted - use the compacted info - there is nothing to generate.
			batchInfo = compactedInfo;
		}
		
		batchLock.release();
		
		return batchInfo;
	}
	
	// This method should only be called when the batch has successfully finished and anything needed is wrote to disk before hand
	// When called the batchLock needs to be already acquired.
	private void performBatchFinishedCompaction()
	{
		log.info("Compacting Batch Info");
		
		// We clear and use info cache this time for cache the final info before freeing memory as much as we can
		ArrayList<String> finalInfo = new ArrayList<String>();
		
		// Populate the final info list
		populateListWithBatchInfo(true, true, finalInfo);
		
		// Now begin compacting all info in the batch
		
		// Batch Attributes
		// batchName - need to keep batch name.
		// priority - need to keep priority.
		
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
		
		// Info Cache can be cleared/nulled
		infoCache.clear();
		infoCache = null;
		
		// The base directory
		baseDirectoryPath = null;
		
		// Base scenario text
		baseScenarioText = null;
		
		log.info("Clearing Batch " + batchId + " DiskCache");
		itemDiskCache.clear();
		
		// Disk Cache for Items
		itemDiskCache = null;
		
		log.info("Batch was compacted information is now cached");
		
		// Size the array.
		compactedInfo = new String[finalInfo.size()];
		
		// Copy data to the array
		finalInfo.toArray(compactedInfo);
		
		infoCompacted = true;
	}
	
	public void setEnabled(boolean enable)
	{
		enabled = enable;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public String getFinished()
	{
		return endDateTime;
	}
	
	@Override
	public int getPosition()
	{
		return position;
	}
	
	@Override
	public void setPosition(int position)
	{
		this.position = position;
	}
	
	public String getFileName()
	{
		return batchFileName;
	}
	
	public boolean isFinished()
	{
		if(!needGenerated.get())
		{
			return getCompleted() == getBatchItems();
		}
		
		return false;
	}
	
	public byte[] getItemConfig(int uniqueId) throws IOException
	{
		return itemDiskCache.getData(uniqueId);
	}
	
	public ExportFormat getStatExportFormat()
	{
		return statExportFormat;
	}
	
	public void setFailed()
	{
		failed = true;
	}
	
	public boolean hasFailed()
	{
		return failed;
	}
	
	private void writeItemLogItem(int version, BatchItem item)
	{
		switch(version)
		{
			case 1:
			{
				itemLog.println("[+Item]");
				itemLog.println("IID=" + item.getItemId());
				itemLog.println("SID=" + item.getSampleId());
				ArrayList<Integer> coords = item.getCoordinates();
				ArrayList<Float> coordsValues = item.getCoordinatesValues();
				for(int c = 0; c < coords.size(); c++)
				{
					itemLog.println("[+Coordinate]");
					itemLog.println("Pos=" + coords.get(c));
					itemLog.println("Value=" + coordsValues.get(c));
					itemLog.println("[-Coordinate]");
				}
				itemLog.println("CacheIndex=" + item.getCacheIndex());
				itemLog.println("RunTime=" + item.getComputeTime());
				itemLog.println("EndEvent=" + item.getEndEvent());
				itemLog.println("StepCount=" + item.getStepCount());
				itemLog.println("[-Item]");
			}
			break;
			case 2:
			{
				StringBuilder itemLine = new StringBuilder();
				
				// Item Id
				itemLine.append("IID=");
				itemLine.append(item.getItemId());
				itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
				
				// Sample Id
				itemLine.append("SID=");
				itemLine.append(item.getSampleId());
				itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
				
				// Surface Coords and Values
				ArrayList<Integer> coords = item.getCoordinates();
				ArrayList<Float> coordsValues = item.getCoordinatesValues();
				
				itemLine.append("Coordinates=");
				itemLine.append("Num=" + coords.size());
				// ; done in loop - loop then exits skipping an ending ;
				for(int c = 0; c < coords.size(); c++)
				{
					itemLine.append(ItemLogTextv2Format.SUBOPTION_DELIMITER);
					itemLine.append("Pos=");
					itemLine.append(coords.get(c));
					itemLine.append(ItemLogTextv2Format.SUBOPTION_DELIMITER);
					
					itemLine.append("Value=");
					itemLine.append(coordsValues.get(c));
				}
				itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
				
				// Cache
				itemLine.append("CacheIndex=");
				itemLine.append(item.getCacheIndex());
				itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
				
				// Runtime
				itemLine.append("RunTime=");
				itemLine.append(item.getComputeTime());
				itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
				
				// Endevent
				itemLine.append("EndEvent=");
				itemLine.append(item.getEndEvent());
				itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
				
				// StepCount
				itemLine.append("StepCount=");
				itemLine.append(item.getStepCount());
				
				// No ending ,
				itemLog.println(itemLine);
			}
			break;
		}
	}
	
	private void writeItemLogHeader(int version, int numCordinates)
	{
		switch(version)
		{
			case 1:
			{
				itemLog.println("[+Header]");
				itemLog.println("Name=" + batchName);
				itemLog.println("LogType=BatchItems");
				itemLog.println("Samples=" + itemSamples);
				itemLog.println("[+AxisLabels]");
				for(int c = 1; c < (numCordinates + 1); c++)
				{
					itemLog.println("id=" + c);
					itemLog.println("AxisName=" + groupName[c - 1] + parameterName[c - 1]);
				}
				
				itemLog.println("[-AxisLabels]");
				itemLog.println("[-Header]");
				itemLog.println("[+Items]");
			}
			break;
			case 2:
				StringBuilder header = new StringBuilder();
				
				// Name
				header.append("Name=");
				header.append(batchName);
				header.append(ItemLogTextv2Format.OPTION_DELIMITER);
				
				// Type
				header.append("LogType=BatchItems");
				header.append(ItemLogTextv2Format.OPTION_DELIMITER);
				
				header.append("Samples=");
				header.append(itemSamples);
				header.append(ItemLogTextv2Format.OPTION_DELIMITER);
				
				// AxisLabels
				header.append("AxisLabels=");
				header.append("Num=" + numCordinates);
				// ; done in loop - loop then exits skipping an ending ;
				for(int c = 1; c < (numCordinates + 1); c++)
				{
					header.append(ItemLogTextv2Format.SUBOPTION_DELIMITER);
					header.append("id=" + c);
					header.append(ItemLogTextv2Format.SUBOPTION_DELIMITER);
					header.append("AxisName=" + groupName[c - 1] + parameterName[c - 1]);
				}
				// No ending ,
				itemLog.println(header.toString());
				
			break;
		}
		
	}
	
}
