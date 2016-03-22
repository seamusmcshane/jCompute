package jCompute.Batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Batch.ItemGenerator.ItemGenerator;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.ItemLogTextv2Format;
import jCompute.Batch.Logger.InfoLogger;
import jCompute.Datastruct.List.Interface.StoredQueuePosition;
import jCompute.Datastruct.cache.DiskCache;
import jCompute.Scenario.ConfigurationInterpreter;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.ScenarioManager;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;
import jCompute.util.FileUtil;
import jCompute.util.Text;

public class Batch implements StoredQueuePosition
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(Batch.class);

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
	private boolean status = false;
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

	// Our Queue of Items yet to be processed
	private LinkedList<BatchItem> queuedItems;

	// The active Items currently being processed.
	private ArrayList<BatchItem> activeItems;
	private int active;

	// Completed items count
	private int itemsCompleted = 0;

	// Batch Finished Status
	private boolean finished;
	private boolean failed;

	// Get Batch Info Cache (Non Changing Data / All Final Info )
	private ArrayList<String> infoCache;

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

		finished = false;
		failed = false;
	}

	public boolean loadConfig(String filePath)
	{
		// Path String
		if(filePath == null)
		{
			log.error("No file path");

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
		String batchConfigText = jCompute.util.Text.textFileToString(filePath);

		// Null if there was an error reading the batch file in
		if(batchConfigText == null)
		{
			log.error("Failed to read file into memory");

			return false;
		}

		// Last check - set the batch status to the outcome, if ok the batch will be enabled.
		status = processBatchConfig(batchConfigText);

		return status;
	}

	/**
	 * Processes and Validates the batch config text
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
				log.debug(type);

				if(itemSamples > 0)
				{
					// Create a generator (type HC for now and too many vars)
					itemGenerator = new SAPPItemGenerator(batchId, batchName, batchConfigProcessor, queuedItems, itemSamples, generationProgress,
					baseScenarioText, storeStats, statsMethodSingleArchive, singleArchiveCompressionLevel, bosBufferSize);
				}
				else
				{
					status = false;
				}
			}
			else
			{
				status = false;
			}
		}

		batchLock.release();

		return status;
	}

	/**
	 * Validates the Base Scenario file used for the batch items.
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
		String tempText = jCompute.util.Text.textFileToString(baseScenaroFilePath);

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
						// We have a statistics section but are there any stats and is one enabled!
						if(!tempIntrp.atLeastOneElementEqualValue("Statistics.Stat", "Enabled", true))
						{
							status = false;

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
				scenario = ScenarioManager.getScenario(baseScenarioText);
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
	 * This method is used for lazy initialisation of a batch.
	 * This prevents considerable up-front processor usage when more than 1 batch is added as we then avoid generating items till the batch is scheduled.
	 * It can safe considerable a mounts of memory and disk space where there are multiple batches queued (vs all generated when added).
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
				if(itemGenerator.generate())
				{
					// All the items need to get processed, but the estimated total time (see getETT()) can be influenced by their processing order.
					// Randomise items in an attempt to reduce influence of item difficulty increasing/decreasing with combination order.
					Collections.shuffle(queuedItems);

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

					infoLogger.writeItemInfo(batchItems, itemSamples, maxSteps);

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

			log.info("Clearing Batch " + batchId + " DiskCache");
			itemDiskCache.clear();

			performBatchFinishedCompaction();

			finished = true;
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

	/*
	 * Batch Info Getter
	 */
	public String[] getBatchInfo()
	{
		batchLock.acquireUninterruptibly();

		ArrayList<String> info = new ArrayList<String>();

		if(!finished)
		{
			if(!needGenerated.get())
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
					infoCache.add("Directory");
					infoCache.add(baseDirectoryPath);
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
			if(failed)
			{
				info.add("Warning");
				info.add("Batch Failed");
			}
			info.add("Queue Position");
			info.add(String.valueOf(position));
			info.add("Status");
			info.add(status == true ? "Enabled" : "Disabled");

			if(!needGenerated.get())
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
				info.add("Directory");
				info.add(baseDirectoryPath);
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
			info.add(needGenerated.get() == false ? "Yes" : "No");
			info.add("Generation Progress");
			info.add(String.valueOf(generationProgress[0]));

			if(!needGenerated.get())
			{
				info.add("");
				info.add("");
				info.add("Active Items");
				info.add(String.valueOf(getActiveItemsCount()));

				info.add("Items Completed");
				info.add(String.valueOf(itemsCompleted));
				info.add("Items Requested");
				info.add(String.valueOf(itemsRequested));
				info.add("Items Returned");
				info.add(String.valueOf(itemsReturned));

				info.add("");
				info.add("");
				info.add("Cache Size");
				info.add(String.valueOf(itemDiskCache.getCacheSize()));
				info.add("Unique Ratio");
				info.add(String.valueOf(itemDiskCache.getUniqueRatio()));
				info.add("MemCacheEnabled");
				info.add(String.valueOf(itemDiskCache.getMemCacheEnabled()));
				info.add("Mem CacheHit");
				info.add(String.valueOf(itemDiskCache.getMemCacheHit()));
				info.add("MemCacheMiss");
				info.add(String.valueOf(itemDiskCache.getMemCacheMiss()));
				info.add("MemCacheHMRatio");
				info.add(String.valueOf(itemDiskCache.getMemHitMissRatio()));

				int div = 1;
				if(itemsCompleted > 0)
				{
					div = itemsCompleted;
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
			if(!needGenerated.get())
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
		infoCache.add(baseDirectoryPath);
		infoCache.add("Export Directory");
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
		infoCache.add(needGenerated.get() == false ? "Yes" : "No");
		infoCache.add("Generation Progress");
		infoCache.add(String.valueOf(generationProgress[0]));

		infoCache.add("");
		infoCache.add("");
		infoCache.add("Active Items");
		infoCache.add(String.valueOf(getActiveItemsCount()));

		infoCache.add("Items Completed");
		infoCache.add(String.valueOf(itemsCompleted));
		infoCache.add("Items Requested");
		infoCache.add(String.valueOf(itemsRequested));
		infoCache.add("Items Returned");
		infoCache.add(String.valueOf(itemsReturned));

		int div = 1;
		if(itemsCompleted > 0)
		{
			div = itemsCompleted;
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
		infoCache.add("Cache Size");
		infoCache.add(String.valueOf(itemDiskCache.getCacheSize()));
		infoCache.add("Unique Ratio");
		infoCache.add(String.valueOf(itemDiskCache.getUniqueRatio()));
		infoCache.add("MemCacheEnabled");
		infoCache.add(String.valueOf(itemDiskCache.getMemCacheEnabled()));
		infoCache.add("Mem CacheHit");
		infoCache.add(String.valueOf(itemDiskCache.getMemCacheHit()));
		infoCache.add("MemCacheMiss");
		infoCache.add(String.valueOf(itemDiskCache.getMemCacheMiss()));
		infoCache.add("MemCacheHMRatio");
		infoCache.add(String.valueOf(itemDiskCache.getMemHitMissRatio()));

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

		// The base directory
		baseDirectoryPath = null;

		// Base scenario text
		baseScenarioText = null;

		// Disk Cache for Items
		itemDiskCache = null;

		log.info("Batch Info Compacted");
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
