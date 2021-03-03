package jcompute.batch.batchresults;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.BatchSettings;
import jcompute.batch.batchitem.BatchItem;
import jcompute.batch.itemgenerator.ItemGeneratorConfigInf;
import jcompute.batch.itemstore.ItemStore;
import jcompute.batch.log.item.custom.logger.CustomItemLogger;
import jcompute.batch.log.item.logger.BatchItemLogInf;
import jcompute.results.custom.CustomItemResultInf;
import jcompute.results.custom.CustomItemResultParser;
import jcompute.results.export.Result;
import jcompute.util.file.FileUtil;
import jcompute.batch.log.item.custom.logger.CustomItemResultsSettings;

public class BatchResultsExporter
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(BatchResultsExporter.class);
	
	// Item log writer
	private final int BW_BUFFER_SIZE = 1024 * 1000;
	
	// Item Log
	private final BatchItemLogInf itemLog;
	
	// Custom Item logs
	private HashMap<String, CustomItemLogger> customItemLoggers;
	
	private ZipOutputStream resultsZipOut;
	
	private final BatchResultSettings BatchResultSettings;
	private final CustomItemResultsSettings CustomItemResultsSettings;
	
	public BatchResultsExporter(BatchItemLogInf itemLog, ArrayList<CustomItemResultInf> customItemResultList, BatchSettings batchSettings) throws IOException
	{
		this.BatchResultSettings = batchSettings.batchResultSettings;
		
		createExportLocation(batchSettings.batchResultSettings);
		
		// Create Item Log
		this.itemLog = itemLog;
		
		if(BatchResultSettings.ItemLogEnabled)
		{
			try
			{
				ItemGeneratorConfigInf itemGeneratorConfig = batchSettings.itemGeneratorConfig;
				
				log.info("ParameterName " + itemGeneratorConfig.getParameterName());
				log.info("GroupName " + itemGeneratorConfig.getGroupName());
				log.info("Buffer Size " + BW_BUFFER_SIZE);
				log.info("Batch Name " + batchSettings.batchName);
				log.info("Item Samples " + itemGeneratorConfig.getItemSamples());
				log.info("Export dir" + BatchResultSettings.BatchStatsExportDir);
				
				itemLog.init(itemGeneratorConfig.getParameterName(), itemGeneratorConfig.getGroupName(), BW_BUFFER_SIZE, batchSettings.batchName,
				itemGeneratorConfig.getItemSamples(), BatchResultSettings.BatchStatsExportDir);
			}
			catch(IOException e)
			{
				log.error("Could not create item log file in " + BatchResultSettings.BatchStatsExportDir);
				
				throw e;
			}
		}
		
		this.CustomItemResultsSettings = batchSettings.customItemResultsSettings;
		
		if(CustomItemResultsSettings.Enabled)
		{
			customItemLoggers = new HashMap<String, CustomItemLogger>();
			
			// Locate the custom item results list
			ArrayList<CustomItemResultInf> cirs = customItemResultList;
			
			// Create a logger for each
			for(CustomItemResultInf cir : cirs)
			{
				String fileName = cir.getLogFileName();
				
				CustomItemLogger logger = new CustomItemLogger(cir, batchSettings.customItemResultsSettings.Format);
				
				// Index by file name
				customItemLoggers.put(fileName, logger);
			}
		}
		
		if(CustomItemResultsSettings.Enabled)
		{
			Set<String> names = customItemLoggers.keySet();
			
			// Close all the custom log files
			for(String name : names)
			{
				try
				{
					customItemLoggers.get(name).init(BW_BUFFER_SIZE, batchSettings.batchName, BatchResultSettings.BatchStatsExportDir);
				}
				catch(IOException e)
				{
					log.error("Could not custom item log + " + name + " file in " + BatchResultSettings.BatchStatsExportDir);
					
					throw e;
				}
			}
		}
		
		// Due to items being processed in any order, the zip archive is pre-populated with the directories needed.
		// This avoids having to check for pre-existance of the directory on every write.
		
		// Are stats enabled
		if(BatchResultSettings.ResultsEnabled)
		{
			// Item Samples
			int itemSamples = batchSettings.itemGeneratorConfig.getItemSamples();
			int combinations = batchSettings.itemGeneratorConfig.getTotalCombinations();
			
			for(int comboNo = 0; comboNo < combinations; comboNo++)
			{
				
				// Create Sub Directories Entry in Zip Archive or a disk Directory
				if(BatchResultSettings.TraceStoreSingleArchive)
				{
					
					// Create if needed
					String zipPath = BatchResultSettings.BatchStatsExportDir + File.separator + "results.zip";
					
					// Create and populate Results Zip archive with Directories
					resultsZipOut = createZipExport(BatchResultSettings, zipPath);
					
					log.info("Zip Archive : " + zipPath);
					
					try
					{
						// Create Item Directories
						resultsZipOut.putNextEntry(new ZipEntry(Integer.toString(comboNo) + "/"));
						resultsZipOut.closeEntry();
						
						for(int sid = 1; sid < (itemSamples + 1); sid++)
						{
							// Create Sample Directories
							resultsZipOut.putNextEntry(new ZipEntry(Integer.toString(comboNo) + "/" + Integer.toString(sid) + "/"));
							resultsZipOut.closeEntry();
						}
					}
					catch(IOException e)
					{
						log.error("Could not create create directory " + comboNo + " in " + zipPath);
						
						e.printStackTrace();
					}
				}
				else
				{
					// Create the item export dir
					FileUtil.createDirIfNotExist(BatchResultSettings.BatchStatsExportDir + File.separator + comboNo);
					
					for(int sid = 1; sid < (itemSamples + 1); sid++)
					{
						String fullExportPath = BatchResultSettings.BatchStatsExportDir + File.separator + comboNo + File.separator + sid;
						
						// Create the item sample full export path dir
						FileUtil.createDirIfNotExist(fullExportPath);
					}
				}
			}
		}
		
		// itemStore = itemGenerator.getItemStore();
	}
	
	public void exportItemResult(BatchItem item, Result exporter, ItemStore itemStore)
	{
		if(BatchResultSettings.ItemLogEnabled)
		{
			// writeItemLogItem(ITEM_LOG_VERSION, item);
			
			itemLog.logItem(item, null);
		}
		
		if(CustomItemResultsSettings.Enabled)
		{
			String[] loggerNames = exporter.getCustomLoggerNames();
			byte[][] data = exporter.getCustomLoggerData();
			
			int numLoggers = loggerNames.length;
			
			for(int l = 0; l < numLoggers; l++)
			{
				// Get the correct logger
				CustomItemLogger logger = customItemLoggers.get(loggerNames[l]);
				
				try
				{
					// Important
					// This code calls the default constructor for the generic result class (creating the specific object) and returns it via the interface.
					// It allows us using the generic interface (CustomItemResultInf) to create objects we do not know the type of at runtime.
					
					// Create the correct container format
					CustomItemResultInf rowFormat = logger.getItemResult().getClass().getDeclaredConstructor().newInstance();
					
					// Fill the container
					CustomItemResultParser.BytesToRow(data[l], rowFormat);
					
					// Write the log line
					// TODO Select the format
					logger.logItem(item, rowFormat);
				}
				catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e)
				{
					log.error("Could not create Item Result instance" + logger.getItemResult());
					
					e.printStackTrace();
				}
			}
		}
		
		// Only Save configs if stats are enabled
		if(BatchResultSettings.ResultsEnabled)
		{
			String fullExportPath = BatchResultSettings.BatchStatsExportDir + File.separator + item.getItemId() + File.separator + item.getSampleId();
			
			if(BatchResultSettings.TraceStoreSingleArchive)
			{
				// Export the stats
				exporter.exportPerItemTraceResultsToZipArchive(resultsZipOut, item.getItemId(), item.getSampleId());
				
				// Export any bin results
				exporter.exportBinResults(fullExportPath, BatchResultSettings.BatchStatsExportDir, item.getItemId());
			}
			else
			{
				// Export the per item traces
				exporter.exportPerItemTraceResults(fullExportPath);
				
				// Export any bin results
				exporter.exportBinResults(fullExportPath, BatchResultSettings.BatchStatsExportDir, item.getItemId());
			}
			
			// Only the first sample needs to save the item config (all
			// identical
			// samples)
			if(item.getSampleId() == 1)
			{
				if(BatchResultSettings.TraceStoreSingleArchive)
				{
					try
					{
						// FileName
						resultsZipOut.putNextEntry(new ZipEntry(item.getItemId() + "/" + "itemconfig-" + item.getCacheIndex() + ".xml"));
						
						// Data
						resultsZipOut.write(itemStore.getData(item.getCacheIndex()));
						
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
						PrintWriter configFile = new PrintWriter(new BufferedWriter(new FileWriter(BatchResultSettings.BatchStatsExportDir + File.separator
						+ item.getItemId() + File.separator + "itemconfig-" + item.getCacheIndex() + ".xml", true)));
						
						configFile.write(new String(itemStore.getData(item.getCacheIndex()), "ISO-8859-1"));
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
	}
	
	/**
	 * Creates the export location used by the batch and the on disk item cache.
	 * 
	 * @param batchConfigProcessor
	 * @return Directory name of the export location.
	 */
	private void createExportLocation(BatchResultSettings batchResultSettings)
	{
		String exportPath = batchResultSettings.BaseExportDir;
		
		final String GroupDirName = batchResultSettings.GroupDirName;
		
		final String SubgroupDirName = batchResultSettings.SubgroupDirName;
		
		// Create Export Path
		FileUtil.createDirIfNotExist(exportPath);
		
		// Append Group name to export dir and create if needed
		if(GroupDirName != null)
		{
			exportPath = exportPath + File.separator + GroupDirName;
			
			FileUtil.createDirIfNotExist(exportPath);
			
			// Do the same for the Sub Group
			if(SubgroupDirName != null)
			{
				exportPath = exportPath + File.separator + SubgroupDirName;
				
				FileUtil.createDirIfNotExist(exportPath);
			}
		}
		
		FileUtil.createDirIfNotExist(batchResultSettings.BatchStatsExportDir);
	}
	
	public void close()
	{
		if(BatchResultSettings.ResultsEnabled)
		{
			if(BatchResultSettings.TraceStoreSingleArchive)
			{
				try
				{
					resultsZipOut.flush();
					resultsZipOut.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if(BatchResultSettings.ItemLogEnabled)
		{
			itemLog.close();
		}
		
		if(CustomItemResultsSettings.Enabled)
		{
			Set<String> names = customItemLoggers.keySet();
			
			// Close all the custom log files
			for(String name : names)
			{
				customItemLoggers.get(name).close();
			}
		}
	}
	
	private ZipOutputStream createZipExport(BatchResultSettings batchResultSettings, String zipPath)
	{
		ZipOutputStream resultsZipOut = null;
		
		try
		{
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipPath)/*, batchResultSettings.BufferSize*/);
			
			resultsZipOut = new ZipOutputStream(bos);
			
			resultsZipOut.setMethod(ZipOutputStream.DEFLATED);
			resultsZipOut.setLevel(batchResultSettings.TraceArchiveCompressionLevel);
		}
		catch(FileNotFoundException e1)
		{
			log.error("Could not create create  " + zipPath);
			
			e1.printStackTrace();
		}
		
		return resultsZipOut;
	}
}
