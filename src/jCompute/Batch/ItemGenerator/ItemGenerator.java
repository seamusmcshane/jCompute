package jCompute.Batch.ItemGenerator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Datastruct.cache.DiskCache;
import jCompute.Scenario.ConfigurationInterpreter;
import jCompute.util.FileUtil;

public abstract class ItemGenerator
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ItemGenerator.class);

	// Store here
	private int batchId = -1;
	private String batchName;
	private ConfigurationInterpreter batchConfigProcessor;

	// Generated here
	private String batchStatsExportDir;
	private DiskCache itemDiskCache;

	// Type interface methods
	public abstract String[] getGroupNames();

	public abstract String[] getParameterNames();

	public abstract ArrayList<String> getParameters();

	public abstract ZipOutputStream getResultsZipOut();

	public abstract int getGeneratedItemCount();

	public abstract boolean subgenerator();

	// Call back from sub class.
	public final void setBatchLazyInitStorageVariables(int batchId, String batchName, ConfigurationInterpreter batchConfigProcessor)
	{
		this.batchId = batchId;
		this.batchName = batchName;
		this.batchConfigProcessor = batchConfigProcessor;
	}

	public final boolean generate()
	{
		if((batchId < 0) || (batchConfigProcessor == null) || (batchName == null))
		{
			log.error(
			"You must call setBatchLazyInitStorageVariables(batchId, batchName,batchConfigProcessor) in your ItemGenerator constructor with initialised variables");

			return false;
		}

		createDirectoriesAndItemCache();

		return subgenerator();
	}

	/**
	 * Creates directories used by the batch and the on disk item cache and sets the batch statistic/results export directory name.
	 * This is called during generate to creation of every batches stat/disk cache directories as soon as it is added.
	 * @param batchConfigProcessor
	 */
	private final void createDirectoriesAndItemCache()
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

				// Format the export directory name
				batchStatsExportDir = baseExportDir + File.separator + date + "@" + time + "[" + batchId + "] " + batchName;

				FileUtil.createDirIfNotExist(batchStatsExportDir);

				log.debug("Batch Stats Export Dir : " + batchStatsExportDir);

				// Create DiskCache
				itemDiskCache = new DiskCache(batchStatsExportDir, Deflater.BEST_SPEED);

				log.info("Created an Item DiskCache for Batch " + batchId);
			}
		}
	}

	// Sub Classes can call this
	public final int getBatchId()
	{
		return batchId;
	}

	// Sub Classes can call this
	public final ConfigurationInterpreter getBatchConfigProcessor()
	{
		return batchConfigProcessor;
	}

	// The batch needs this and Sub Classes can call this
	public final DiskCache getItemDiskCache()
	{
		return itemDiskCache;
	}

	// The batch needs this.
	public final String getBatchStatsExportDir()
	{
		return batchStatsExportDir;
	}
}