package jcompute.batch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.batchresults.BatchResultSettings;
import jcompute.batch.itemgenerator.ItemGeneratorConfigInf;
import jcompute.batch.log.item.custom.logger.CustomItemResultsSettings;
import jcompute.configuration.JComputeConfigurationUtility;
import jcompute.configuration.batch.BatchJobConfig;
import jcompute.scenario.ScenarioInf;
import jcompute.scenario.ScenarioPluginManager;
import jcompute.util.file.FileUtil;
import jcompute.util.text.JCText;

public class BatchConfigProcessor
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(BatchConfigProcessor.class);
	
	// Was the config processed fully
	private boolean VALID;
	
	// Resultant settings object if valid is true
	private BatchSettings batchSettings;
	
	public BatchConfigProcessor(int batchId, String filePath)
	{
		// Location of batch config and working directoy.
		final String batchFileName;
		final String batchName;
		final String baseDirectoryPath;
		
		// Settings objects to be created
		CustomItemResultsSettings customItemResultsSettings;
		BatchResultSettings batchResultSettings;
		ItemGeneratorConfigInf itemGeneratorConfig;
		
		// The base scenario for the batch
		ScenarioInf baseScenario = null;
		
		// Scenario max steps and type
		// int maxSteps;
		String type;
		
		/*
		 * ********************************************************************************
		 * Process the file path and load in the batch config text
		 ********************************************************************************/
		
		// Path String
		if(filePath == null)
		{
			log.error("Batch file path is null");
			
			VALID = false;
			
			return;
		}
		
		log.info("New Batch based on : " + filePath);
		
		batchFileName = FileUtil.getFileName(filePath);
		
		// Failed to read a file name
		if(batchFileName == null)
		{
			log.error("Failed to find batch filename");
			
			VALID = false;
			
			return;
		}
		
		log.info("File : " + batchFileName);
		
		batchName = FileUtil.removeExtfromFilename(batchFileName);
		
		if(batchName == null)
		{
			log.error("Could not detect extension on file.");
			
			VALID = false;
			
			return;
		}
		
		baseDirectoryPath = FileUtil.getPath(filePath);
		
		// Could fail if there is no parent.
		if(baseDirectoryPath == null)
		{
			log.error("Failed to set base directory path - could not find parent of file");
			
			VALID = false;
			
			return;
		}
		
		log.info("Base Path : " + baseDirectoryPath);
		
		// The Batch Configuration Text
		String batchConfigText = JCText.textFileToString(filePath);
		
		// Null if there was an error reading the batch file in
		if(batchConfigText == null)
		{
			log.error("Failed to read batch config file into memory");
			
			VALID = false;
			
			return;
		}
		
		// Set later then stored in settings
		String baseScenarioFileName = null;
		
		// Convert the XML text to a jobconfig
		BatchJobConfig bjc = (BatchJobConfig) JComputeConfigurationUtility.XMLtoConfig(batchConfigText,
		BatchJobConfig.class);
		
		// Was the Batch Job Config created
		if(bjc != null)
		{
			log.info("Processing BatchFile");
			
			// The complete export dir for stats
			final String CompleteBatchStatsExportDir = generateExportLocation(batchId, batchName, baseDirectoryPath,
			bjc);
			
			batchResultSettings = new BatchResultSettings(bjc, CompleteBatchStatsExportDir);
			
			customItemResultsSettings = new CustomItemResultsSettings(bjc);
			
			/*
			 * ********************************************************************************
			 * Config Section
			 ********************************************************************************/
			
			/* BaseScenarioFile */
			
			log.info("Processing BaseScenarioFile");
			
			// Store the base fileName
			baseScenarioFileName = bjc.getConfig().getBaseScenarioFileName();
			
			// Assumes the file is in the same dir as the batch file
			String baseScenaroFilePath = baseDirectoryPath + File.separator + baseScenarioFileName;
			
			// Attempt to load the text into a string
			String tempText = JCText.textFileToString(baseScenaroFilePath);
			
			// Base scenario text to be set in Item Generator config if the config is useable
			String baseScenarioText = null;
			
			// No null if the text from the file has been loaded
			if(tempText != null)
			{
				// Scenario Text
				baseScenarioText = tempText;
				
				// Finally create a real Scenario // TODO pass on the batch config so the scenario its self can handle stat enabled/missmatches.
				baseScenario = ScenarioPluginManager.getScenario(baseScenarioText);
				
				if(baseScenario == null)
				{
					VALID = false;
					
					return;
				}
			}
			else
			{
				// If we got here , we could not read the file in or there was an issue in the libray code.
				
				log.error("Failed to read base scenario config file into memory");
				
				return;
			}
			
			/* ItemSamples */
			
			// How many times to run each batchItem.
			final int itemSamples = bjc.getConfig().getItemSamples();
			
			log.info("Item Samples " + itemSamples);
			
			/*
			 * ********************************************************************************
			 * Output info to log
			 ********************************************************************************/
			
			log.info("Info Log " + batchResultSettings.InfoLogEnabled);
			log.info("Item Log " + batchResultSettings.ItemLogEnabled);
			
			log.info("Store Stats " + batchResultSettings.ResultsEnabled);
			log.info("Trace Enabled " + batchResultSettings.TraceEnabled);
			log.info("BDFC Enabled " + batchResultSettings.BDFCEnabled);
			log.info("Custom Results Enabled " + customItemResultsSettings.Enabled);
			log.info("Custom Results Format " + customItemResultsSettings.Format);
			log.info("Batch Header in Custom Result " + customItemResultsSettings.BatchHeaderInResult);
			log.info("Item Info in Custom Result " + customItemResultsSettings.ItemInfoInResult);
			log.info("Trace Single Archive " + batchResultSettings.TraceStoreSingleArchive);
			// log.info("Archive Buffer Size " + batchResultSettings.BufferSize);
			log.info("Archive Compression Level " + batchResultSettings.TraceArchiveCompressionLevel);
			log.info("Batch Stats Export Path : " + batchResultSettings.FullBatchStatsExportPath);
			
			/*
			 * ********************************************************************************
			 * Item Generator Settings
			 ********************************************************************************/
			
			if(baseScenario == null)
			{
				log.error("Processing batch config - error found checking base scenario.");
				
				VALID = false;
				
				return;
			}
			else
			{
				// maxSteps = baseScenario.getEndEventTriggerValue("StepCount");
				type = baseScenario.getScenarioType();
				
				// log.info("Scenario StepCount " + maxSteps);
				log.info("Scenario Type " + type);
				
				// If samples requested and maxSteps is valid.
				// if(!((itemSamples > 0) & (maxSteps > 0)))
				if(itemSamples >= 1)
				{
					log.error("ItemSamples : " + itemSamples);// + " MaxSteps : " + maxSteps);
					
					VALID = false;
					
					return;
				}
				
				// Get ItemGeneratorConfig from base scenario
				itemGeneratorConfig = baseScenario.getItemGeneratorConfig(bjc.getParameterList(), baseScenarioText,
				itemSamples);
			}
		}
		else
		{
			// Not a batch file.
			log.error("File is not a valid Batch File");
			
			VALID = false;
			
			return;
		}
		
		// All Settings created
		batchSettings = new BatchSettings(batchName, batchFileName, baseScenarioFileName,
		baseDirectoryPath/*, maxSteps*/, type, customItemResultsSettings, batchResultSettings, itemGeneratorConfig,
		baseScenario);
		
		// Validation and setup is complete.
		VALID = true;
	}
	
	/**
	 * Used by the batch and the on disk item cache and returns the procedurally generated directory name.
	 * 
	 * @param batchConfigProcessor
	 * @return Directory name of the export location.
	 */
	private String generateExportLocation(int batchId, String batchName, String baseDirectoryPath,
	BatchJobConfig config)
	{
		String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		String time = new SimpleDateFormat("HHmm").format(Calendar.getInstance().getTime());
		
		String fullExportDir = config.getStats().getStatsExportDir();
		String groupDirName = config.getStats().getGroupDir();
		String subgroupDirName = config.getStats().getSubGroupDir();
		
		// Append Group name to export dir
		if(groupDirName != null)
		{
			fullExportDir = fullExportDir + File.separator + groupDirName;
			
			// Sub Groups
			if(subgroupDirName != null)
			{
				fullExportDir = fullExportDir + File.separator + subgroupDirName;
			}
		}
		
		// Format the export directory name
		fullExportDir = fullExportDir + File.separator + date + "@" + time + "[" + batchId + "] " + batchName;
		
		return fullExportDir;
	}
	
	public boolean isValid()
	{
		return VALID;
	}
	
	public BatchSettings getBatchSettings()
	{
		return batchSettings;
	}
}
