package jcompute.scenario;

import java.util.LinkedList;
import java.util.List;

import jcompute.batch.BatchItem;
import jcompute.batch.itemgenerator.ItemGenerator;
import jcompute.simulation.SimulationScenarioManagerInf;
import jcompute.stats.StatGroupSetting;

public interface ScenarioInf
{
	public boolean loadConfig(ConfigurationInterpreter interpreter);
	
	public String getScenarioType();
	
	public double getScenarioVersion();
	
	public SimulationScenarioManagerInf getSimulationScenarioManager();
	
	public List<StatGroupSetting> getStatGroupSettingsList();
	
	public String getScenarioText();
	
	public boolean endEventIsSet(String eventName);
	
	public int getEndEventTriggerValue(String eventName);
	
	public static final String INVALID = "Invalid";
	
	// TODO - rework
	public ItemGenerator getItemGenerator(int batchId, String batchName, ConfigurationInterpreter batchConfigProcessor,
	LinkedList<BatchItem> destinationItemList, int itemSamples, double[] progress1dArray, String baseScenarioText, boolean storeStats,
	boolean statsMethodSingleArchive, int singleArchiveCompressionLevel, int bosBufferSize);
}