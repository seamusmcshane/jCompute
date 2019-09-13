package jcompute.batch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.itemgenerator.ItemGeneratorConfigInf;
import jcompute.batch.log.item.custom.logger.CustomItemResultsSettings;
import jcompute.scenario.ConfigurationInterpreter;
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
	
	// Write stats to a single archive or directories with sub archives
	private final int BOS_DEFAULT_BUFFER_SIZE = 8192;
	
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
		int maxSteps;
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
		
		// The Configuration Interpreter
		ConfigurationInterpreter batchConfigInterpreter = new ConfigurationInterpreter();
		
		batchConfigInterpreter.loadConfig(batchConfigText);
		
		// We have loaded a file but is it a batch file?
		if(batchConfigInterpreter.getScenarioType().equalsIgnoreCase("Batch"))
		{
			log.info("Processing BatchFile");
			
			/*
			 * ********************************************************************************
			 * Log Section
			 ********************************************************************************/
			
			// Batch Info Log
			final boolean InfoLogEnabled = batchConfigInterpreter.getBooleanValue("Log", "InfoLog");
			
			// Item Log
			final boolean ItemLogEnabled = batchConfigInterpreter.getBooleanValue("Log", "ItemLog");
			
			/*
			 * ********************************************************************************
			 * Stat Section
			 ********************************************************************************/
			
			// Enable / Disable writing the generated result files to disk
			final boolean ResultsEnabled = batchConfigInterpreter.getBooleanValue("Stats", "Store", false);
			
			// Enable / Disable writing the generated result files to disk
			final boolean TraceEnabled = batchConfigInterpreter.getBooleanValue("Stats", "TraceResult", false);
			
			// Enable / Disable writing the generated result files to disk
			final boolean BDFCEnabled = batchConfigInterpreter.getBooleanValue("Stats", "BDFCResult", false);
			
			// Get the Custom Result Format
			final String CustomResultsFormat = batchConfigInterpreter.getStringValue("Stats", "CustomResultFormat", "");
			
			// Check if the format is disabled, mean custom results are not requested.
			final boolean CustomResultsEnabled = !CustomResultsFormat.equals("Disabled") ? true : false;
			
			final boolean BatchHeaderInCustomResult = batchConfigInterpreter.getBooleanValue("Stats", "BatchHeaderInCustomResult", false);
			
			final boolean ItemInfoInCustomResult = batchConfigInterpreter.getBooleanValue("Stats", "ItemInfoInCustomResult", false);
			
			// Store traces in a single archive
			final boolean TraceStoreSingleArchive = batchConfigInterpreter.getBooleanValue("Stats", "SingleArchive", false);
			
			// Compression Level for above
			final int TraceArchiveCompressionLevel = batchConfigInterpreter.getIntValue("Stats", "CompressionLevel", 9);
			
			final int BufferSize = batchConfigInterpreter.getIntValue("Stats", "BufferSize", BOS_DEFAULT_BUFFER_SIZE);
			
			/*
			 * ********************************************************************************
			 * Export Paths
			 ********************************************************************************/
			
			// The base results path under which we place the rest - Normally stats/
			final String BaseExportDir = batchConfigInterpreter.getStringValue("Stats", "BatchStatsExportDir");
			
			// Check for a Group dir for this Batch
			final String GroupDirName = batchConfigInterpreter.getStringValue("Stats", "BatchGroupDir");
			
			final String SubgroupDirName = batchConfigInterpreter.getStringValue("Stats", "BatchSubGroupDirName");
			
			// The complete export dir for stats
			final String BatchStatsExportDir = generateExportLocation(batchId, batchName, BaseExportDir, GroupDirName, SubgroupDirName);
			
			batchResultSettings = new BatchResultSettings(ResultsEnabled, BaseExportDir, GroupDirName, SubgroupDirName, BatchStatsExportDir, TraceEnabled,
			BDFCEnabled, TraceStoreSingleArchive, TraceArchiveCompressionLevel, BufferSize, InfoLogEnabled, ItemLogEnabled);
			
			customItemResultsSettings = new CustomItemResultsSettings(CustomResultsEnabled, CustomResultsFormat, BatchHeaderInCustomResult,
			ItemInfoInCustomResult);
			
			/*
			 * ********************************************************************************
			 * Config Section
			 ********************************************************************************/
			
			/* BaseScenarioFile */
			
			log.info("Processing BaseScenarioFile");
			
			// Store the base fileName
			baseScenarioFileName = batchConfigInterpreter.getStringValue("Config", "BaseScenarioFileName");
			
			// Assumes the file is in the same dir as the batch file
			String baseScenaroFilePath = baseDirectoryPath + File.separator + baseScenarioFileName;
			
			// Attempt to load the text into a string
			String tempText = JCText.textFileToString(baseScenaroFilePath);
			
			// Base scenario text to be set in Item Generator config if the config is useable
			String baseScenarioText = null;
			
			// No null if the text from the file has been loaded
			if(tempText != null)
			{
				// Use a ConfigurationInterpreter to prevent a BatchStats/ItemStats mismatch which could cause a stats req for stats that do not exist
				ConfigurationInterpreter tempIntrp = new ConfigurationInterpreter();
				
				// Is the base scenario currently a valid scenario file
				if(tempIntrp.loadConfig(tempText))
				{
					// Check if Batch stats are disabled globally
					if(!batchResultSettings.ResultsEnabled)
					{
						// If so remove the Statistics section from XML config (disabling them on each item)
						tempIntrp.removeSection("Statistics");
						
						log.warn("Removing base scenario statistics section as batch has results disabled globally.");
					}
					else
					{
						// Check if the base scenario has a statistics section
						if(!tempIntrp.hasSection("Statistics"))
						{
							log.error("The batch has statistics enabled but the base scenario does not.");
							
							VALID = false;
							
							return;
						}
						else
						{
							boolean statStatus = false;
							
							// We have a statistics section but are there any stats and is one enabled!
							if(tempIntrp.atLeastOneElementEqualValue("Statistics.Stat", "Enabled", true) & batchResultSettings.TraceEnabled)
							{
								statStatus = true;
							}
							
							if(tempIntrp.atLeastOneElementEqualValue("Statistics.BDFC", "Enabled", true) & batchResultSettings.BDFCEnabled)
							{
								statStatus = true;
							}
							
							if(tempIntrp.atLeastOneElementEqualValue("Statistics.Custom", "Enabled", true) & customItemResultsSettings.Enabled)
							{
								statStatus = true;
							}
							
							// If the batch stats and scenario stats have a valid combination then create a scenario object.
							if(statStatus)
							{
								log.info("Batch and Scenario have valid stats combination, proceeding.");
								
								baseScenarioText = tempIntrp.getText();
								
								// Finally create a real Scenariot
								baseScenario = ScenarioPluginManager.getScenario(baseScenarioText);
							}
							else
							{
								// If we got here there is an issue, batch has stats but base scenario is not set to record them
								
								log.error("Results mismatch - At least one must be enabled in Batch and in base scenario");
								
								VALID = false;
								
								return;
							}
						}
					}
				}
				else
				{
					log.error("Base scenario failed sanity checks.");
					
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
			final int itemSamples = batchConfigInterpreter.getIntValue("Config", "ItemSamples", 1);
			
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
			log.info("Archive Buffer Size " + batchResultSettings.BufferSize);
			log.info("Archive Compression Level " + batchResultSettings.TraceArchiveCompressionLevel);
			log.info("Batch Stats Export Dir : " + batchResultSettings.BatchStatsExportDir);
			
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
				maxSteps = baseScenario.getEndEventTriggerValue("StepCount");
				type = baseScenario.getScenarioType();
				
				log.info("Scenario StepCount " + maxSteps);
				log.info("Scenario Type " + type);
				
				// If samples requested and maxSteps is valid.
				if(!((itemSamples > 0) & (maxSteps > 0)))
				{
					log.error("ItemSamples : " + itemSamples + " MaxSteps : " + maxSteps);
					
					VALID = false;
					
					return;
				}
				
				// Get ItemGeneratorConfig from base scenario
				itemGeneratorConfig = baseScenario.getItemGeneratorConfig(batchConfigInterpreter, baseScenarioText, itemSamples);
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
		batchSettings = new BatchSettings(batchName, batchFileName, baseScenarioFileName, baseDirectoryPath, maxSteps, type, customItemResultsSettings,
		batchResultSettings, itemGeneratorConfig, baseScenario);
		
		// Validation and setup is complete.
		VALID = true;
	}
	
	/**
	 * Used by the batch and the on disk item cache and returns the procedurally generated directory name.
	 * 
	 * @param batchConfigProcessor
	 * @return Directory name of the export location.
	 */
	private String generateExportLocation(int batchId, String batchName, String BaseExportDir, String GroupDirName, String SubgroupDirName)
	{
		String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		String time = new SimpleDateFormat("HHmm").format(Calendar.getInstance().getTime());
		
		String fullExportDir = BaseExportDir;
		
		// Append Group name to export dir
		if(GroupDirName != null)
		{
			fullExportDir = fullExportDir + File.separator + GroupDirName;
			
			// Sub Groups
			if(SubgroupDirName != null)
			{
				fullExportDir = fullExportDir + File.separator + SubgroupDirName;
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
