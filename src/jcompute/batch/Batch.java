package jcompute.batch;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.batchitem.BatchItem;
import jcompute.batch.batchresults.BatchResultsExporter;
import jcompute.batch.itemgenerator.ItemGenerator;
import jcompute.batch.itemmanager.ItemManager;
import jcompute.batch.itemstore.ItemStore;
import jcompute.batch.log.info.logger.InfoLogger;
import jcompute.batch.log.item.logger.BatchItemLogInf;
import jcompute.datastruct.list.StoredQueuePosition;
import jcompute.results.custom.CustomItemResultInf;
import jcompute.results.export.ExportFormat;
import jcompute.results.export.Result;
import jcompute.scenario.ScenarioInf;
import jcompute.timing.ProgressObj;
import jcompute.timing.TimerObj;
import jcompute.util.text.TimeString;
import jcompute.util.text.TimeString.TimeStringFormat;

public class Batch implements StoredQueuePosition
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(Batch.class);
	
	// needInitialized records the start and end of init()
	// Allows batch manager logic to handle a batch that needs init() or skip init() call that is already init. - get via needsInit()
	private AtomicBoolean needInitialized = new AtomicBoolean(true);
	
	// Initialising is for thread safety of the method init() and avoiding calling init() when already initialising - get via isInit()
	private AtomicBoolean initialising = new AtomicBoolean(false);
	
	// Queue Positon
	private int position;
	
	// Batch id
	private int batchId;
	
	// Parameters used to generated the items
	private ArrayList<String> parameters;
	
	// Set if this batch's items can be processed (stop/start)
	private boolean enabled;
	
	// All batch settings
	private final BatchSettings settings;
	
	// Or if it has failed
	private boolean failed;
	
	// Does this batch need items generated.
	private AtomicBoolean needGenerated = new AtomicBoolean(true);
	
	// Progress of item generation
	private ProgressObj itemGenerationProgress;
	
	// Items Management
	private ItemManager itemManager;
	
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
	
	// Processing time generating items
	private long itemGenerationTime;
	
	private BatchResultsExporter batchResultsExporter;
	
	// Completed items count
	private int itemsCompleted = 0;
	
	// Info Cache (Non Changing Data)
	private ArrayList<String> infoCache;
	
	// Compacted info is created end of the batch (from now static info) and used in place of the live info normally generated when getBatchInfo is called.
	private String[] compactedInfo;
	private boolean infoCompacted;
	
	// ItemStore for Items
	private ItemStore itemStore;
	
	// To protect our shared variables/data structures
	private Semaphore batchLock = new Semaphore(1, false);
	
	public Batch(int batchId, String filePath)
	{
		this.batchId = batchId;
		
		// Processing Times
		cpuTotalTimes = 0;
		ioTotalTimes = 0;
		
		addedDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		
		// Item management data structures
		itemManager = new ItemManager();
		
		// Not enabled
		enabled = false;
		
		// Batch info is not compacted until batch is complete
		infoCompacted = false;
		compactedInfo = null;
		
		BatchConfigProcessor bcp = new BatchConfigProcessor(batchId, filePath);
		
		settings = bcp.getBatchSettings();
		
		boolean bcpIsValid = bcp.isValid();
		
		if(bcpIsValid)
		{
			log.info("Batch config is Valid : " + bcpIsValid);
			
			itemGenerationProgress = new ProgressObj(settings.itemGeneratorConfig.getTotalCombinations());
		}
		
		// If the config is invalid the batch has failed immediately
		failed = !bcpIsValid;
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
		// It is started when the batch begins processing.
		Thread backgroundGenerate = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				ScenarioInf baseScenario = settings.baseScenario;
				
				BatchItemLogInf itemLog = baseScenario.getItemLogWriter();
				
				ArrayList<CustomItemResultInf> customItemResultList = baseScenario.getSimulationScenarioManager(null)
				.getResultManager().getCustomItemResultList();
				
				try
				{
					// Create Results Exporter
					batchResultsExporter = new BatchResultsExporter(itemLog, customItemResultList, settings);
				}
				catch(IOException e)
				{
					log.error("Could not create batchResultsExporter for  Batch " + batchId);
					
					// status = false;
					e.printStackTrace();
					
					failed = true;
					
					// Do not continuie
					return;
				}
				
				TimerObj to = new TimerObj();
				
				to.startTimer();
				
				// Create a generator (type HC for now and too many vars)
				ItemGenerator itemGenerator = baseScenario.getItemGenerator();
				
				if(itemGenerator == null)
				{
					log.error("Scenario does not support batch items");
					
					failed = true;
					
					return;
				}
				
				// Ref Stored in batch
				itemStore = baseScenario.getItemStore();
				
				if(itemGenerator.generate(batchId, itemGenerationProgress, itemManager, itemStore, settings))
				{
					log.info("Generated Items Batch " + batchId);
					
					parameters = itemGenerator.getParameters();
					
					needInitialized.set(false);
					needGenerated.set(false);
					
					// No longer initialising.
					initialising.compareAndSet(true, false);
				}
				else
				{
					log.error("Item Generation Failed");
					
					failed = true;
					
					// Do not continuie
					return;
				}
				
				to.stopTimer();
				
				itemGenerationTime = to.getTimeTaken();
				
			}
		});
		backgroundGenerate.setName("Item Generation Background Thread Batch " + batchId);
		
		// Generate the item configs now
		backgroundGenerate.start();
	}
	
	public void returnItemToQueue(BatchItem item)
	{
		batchLock.acquireUninterruptibly();
		
		// Return the item
		itemManager.returnItem(item);
		
		batchLock.release();
	}
	
	public BatchItem getNext()
	{
		batchLock.acquireUninterruptibly();
		
		BatchItem temp = itemManager.getNext();
		
		// Is this the first Item && Sample
		if(itemManager.getItemsRequested() == 1)
		{
			// For run time calc
			startTimeMillis = System.currentTimeMillis();
			startDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		}
		
		batchLock.release();
		
		return temp;
	}
	
	// TODO Test removal / moving to setItemComplete
	public void setItemNotActive(BatchItem item)
	{
		batchLock.acquireUninterruptibly();
		
		itemManager.setNotActive(item);
		
		batchLock.release();
	}
	
	public void setItemComplete(BatchItem item, Result exporter)
	{
		batchLock.acquireUninterruptibly();
		
		long ioStart = System.currentTimeMillis();
		long ioEnd;
		
		log.debug("Setting Completed Sim " + item.getSimId() + " Item " + item.getItemId());
		
		// For estimated complete time calculation
		cpuTotalTimes += item.getComputeTime();
		
		batchResultsExporter.exportItemResult(item, exporter, itemStore);
		
		itemsCompleted++;
		
		ioEnd = System.currentTimeMillis();
		
		ioTotalTimes += ioEnd - ioStart;
		
		lastCompletedItemTimeMillis = System.currentTimeMillis();
		
		if(itemsCompleted == itemManager.getTotalItems())
		{
			batchResultsExporter.close();
			
			endDateTime = new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			
			// Write the info log
			if(settings.batchResultSettings.InfoLogEnabled)
			{
				try
				{
					InfoLogger infoLogger = new InfoLogger(settings.batchResultSettings.FullBatchStatsExportPath);
					
					infoLogger.writeGeneralInfo(batchId, settings.batchName, settings.type,
					settings.baseScenarioFileName);
					
					infoLogger.writeItemInfo(itemManager.getTotalItems(), settings.itemGeneratorConfig.getItemSamples(),
					/*settings.maxSteps,*/ TimeString.timeInMillisAsFormattedString(itemGenerationTime,
					TimeStringFormat.DHMS));
					
					infoLogger.writeProcessedInfo(addedDateTime, startDateTime, endDateTime, startTimeMillis);
					
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
		return itemManager.getCurrentItems();
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
		return settings.type;
	}
	
	public String getBaseScenarioFileName()
	{
		return settings.baseScenarioFileName;
	}
	
	public int getBatchTotalItems()
	{
		return itemManager.getTotalItems();
	}
	
	public int getProgress()
	{
		return (int) (((double) itemsCompleted / (double) itemManager.getTotalItems()) * 100.0);
	}
	
	public int getCompleted()
	{
		return itemsCompleted;
	}
	
	public int getActiveItemsCount()
	{
		return itemManager.getTotalActiveItems();
	}
	
	public long getRunTime()
	{
		return lastCompletedItemTimeMillis - startTimeMillis;
	}
	
	public long getETT()
	{
		int active = itemManager.getTotalActiveItems();
		
		if((active > 0) && (itemsCompleted > 0))
		{
			return ((cpuTotalTimes + ioTotalTimes) / itemsCompleted) * ((itemManager.getTotalItems() - itemsCompleted)
			/ active);
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
		targetList.add(settings.batchName);
		targetList.add("Scenario Type");
		targetList.add(settings.type);
		
		if(batchGenerated)
		{
			/*int tMaxSteps = settings.maxSteps;*/
			int tItemSamples = settings.itemGeneratorConfig.getItemSamples();
			
			// Values wont exist/bevalid if not generated
			addBatchInfoSectionHeader(formated, false, "Item", targetList);
			targetList.add("Unique Items");
			targetList.add(String.valueOf(itemManager.getTotalItems() / tItemSamples));
			targetList.add("Sample per Item");
			targetList.add(String.valueOf(tItemSamples));
			targetList.add("Total Items");
			targetList.add(String.valueOf(itemManager.getTotalItems()));
			/*targetList.add("Max Steps");
			targetList.add(String.valueOf(tMaxSteps));*/
			
			addBatchInfoSectionHeader(formated, false, "Parameters", targetList);
			targetList.addAll(parameters);
		}
		
		addBatchInfoSectionHeader(formated, false, "Files/Paths", targetList);
		targetList.add("Batch");
		targetList.add(settings.batchFileName);
		targetList.add("Scenario");
		targetList.add(settings.baseScenarioFileName);
		targetList.add("Base");
		targetList.add(settings.baseDirectoryPath);
		targetList.add("Export Path");
		targetList.add(settings.batchResultSettings.FullBatchStatsExportPath);
		
		addBatchInfoSectionHeader(formated, false, "Statistics", targetList);
		targetList.add("Stats Store");
		targetList.add(settings.batchResultSettings.ResultsEnabled == true ? "Enabled" : "Disabled");
		targetList.add("Single Archive");
		targetList.add(settings.batchResultSettings.TraceStoreSingleArchive == true ? "Enabled" : "Disabled");
		// targetList.add("Buffer Size");
		// targetList.add(String.valueOf(settings.batchResultSettings.BufferSize));
		targetList.add("Compression Level");
		targetList.add(String.valueOf(settings.batchResultSettings.TraceArchiveCompressionLevel));
		targetList.add("Info Log");
		targetList.add(settings.batchResultSettings.InfoLogEnabled == true ? "Enabled" : "Disabled");
		targetList.add("Item Log");
		targetList.add(settings.batchResultSettings.ItemLogEnabled == true ? "Enabled" : "Disabled");
	}
	
	private void addBatchDetailsQueueInfoToList(boolean formated, ArrayList<String> targetList)
	{
		addBatchInfoSectionHeader(formated, false, "Queue", targetList);
		
		targetList.add("Position");
		targetList.add(String.valueOf(position));
		targetList.add("Status");
		targetList.add(enabled == true ? "Enabled" : "Disabled");
	}
	
	private void addBatchDetailsGenerationProgressInfoToList(boolean formated, boolean batchGenerated,
	ArrayList<String> targetList)
	{
		addBatchInfoSectionHeader(formated, true, "Generation", targetList);
		
		targetList.add("Generated");
		targetList.add(batchGenerated ? "Yes" : "No");
		
		targetList.add("Progress");
		if(itemGenerationProgress != null)
		{
			targetList.add(String.valueOf((int) itemGenerationProgress.progressAsPercentage()));
		}
		else
		{
			targetList.add("0");
		}
		
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
		targetList.add(String.valueOf(itemManager.getItemsRequested()));
		targetList.add("Returned");
		targetList.add(String.valueOf(itemManager.getItemsReturned()));
	}
	
	// private void addBatchDetailsItemStoreInfoToList(boolean formated, ArrayList<String> targetList)
	// {
	// addBatchInfoSectionHeader(formated, true, "DiskCache", targetList);
	//
	// targetList.add("Cache Size");
	// targetList.add(String.valueOf(itemDiskCache.getCacheSize()));
	// targetList.add("Unique Ratio");
	// targetList.add(String.valueOf(itemDiskCache.getUniqueRatio()));
	// targetList.add("MemCacheEnabled");
	// targetList.add(String.valueOf(itemDiskCache.getMemCacheEnabled()));
	//
	// addBatchInfoSectionHeader(formated, false, "MemCache", targetList);
	//
	// targetList.add("Size");
	// targetList.add(String.valueOf(itemDiskCache.getMemCacheSize()));
	// targetList.add("Requests");
	// targetList.add(String.valueOf(itemDiskCache.getMemCacheRequests()));
	// targetList.add("Hits");
	// targetList.add(String.valueOf(itemDiskCache.getMemCacheHits()));
	// targetList.add("Misses");
	// targetList.add(String.valueOf(itemDiskCache.getMemCacheMisses()));
	// targetList.add("Hit Ratio");
	// targetList.add(String.valueOf(itemDiskCache.getMemCacheHitRatio()));
	// targetList.add("Miss Ratio");
	// targetList.add(String.valueOf(itemDiskCache.getMemCacheMissRatio()));
	// }
	
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
		targetList.add(TimeString.timeInMillisAsFormattedString(((cpuTotalTimes + ioTotalTimes) / div),
		TimeStringFormat.DHMS));
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
			// addBatchDetailsItemStoreInfoToList(formated, targetList);
			
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
		
		parameters = null;
		
		// For human readable date/time info
		addedDateTime = null;
		
		// Log - total time calc
		startDateTime = null;
		// endDateTime = null;
		
		// The item manager may have created a large datastructure
		itemManager.compact();
		
		// Info Cache can be cleared/nulled
		infoCache.clear();
		infoCache = null;
		
		log.info("Compact Batch " + batchId + " ItemStore");
		itemStore.compact();
		
		// Disk Cache for Items
		itemStore = null;
		
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
		return settings.batchFileName;
	}
	
	public boolean isFinished()
	{
		if(!needGenerated.get())
		{
			return getCompleted() == getBatchTotalItems();
		}
		
		return false;
	}
	
	public byte[] getItemConfig(int uniqueId) throws IOException
	{
		return itemStore.getData(uniqueId);
	}
	
	public ExportFormat getTraceExportFormat()
	{
		return settings.batchResultSettings.TraceExportFormat;
	}
	
	public void setFailed()
	{
		failed = true;
	}
	
	public boolean hasFailed()
	{
		return failed;
	}
}
