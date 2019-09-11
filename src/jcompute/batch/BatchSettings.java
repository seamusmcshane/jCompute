package jcompute.batch;

import jcompute.batch.itemgenerator.ItemGeneratorConfigInf;
import jcompute.batch.log.item.custom.logger.CustomItemResultsSettings;
import jcompute.scenario.ScenarioInf;

public class BatchSettings
{
	public final String batchFileName;
	public final String batchName;
	public final String baseScenarioFileName;
	public final String baseDirectoryPath;
	
	public final int maxSteps;
	public final String type;
	
	// Process BatchConfig
	public final CustomItemResultsSettings customItemResultsSettings;
	public final BatchResultSettings batchResultSettings;
	public final ItemGeneratorConfigInf itemGeneratorConfig;
	
	// Base Scenario
	public final ScenarioInf baseScenario;
	
	public BatchSettings(String batchFileName, String batchName, String baseScenarioFileName, String baseDirectoryPath, int maxSteps, String type,
	CustomItemResultsSettings customItemResultsSettings, BatchResultSettings batchResultSettings, ItemGeneratorConfigInf itemGeneratorConfig,
	ScenarioInf baseScenario)
	{
		this.batchFileName = batchFileName;
		this.batchName = batchName;
		this.baseScenarioFileName = baseScenarioFileName;
		this.baseDirectoryPath = baseDirectoryPath;
		
		this.maxSteps = maxSteps;
		this.type = type;
		
		this.customItemResultsSettings = customItemResultsSettings;
		this.batchResultSettings = batchResultSettings;
		this.itemGeneratorConfig = itemGeneratorConfig;
		this.baseScenario = baseScenario;
	}
}
