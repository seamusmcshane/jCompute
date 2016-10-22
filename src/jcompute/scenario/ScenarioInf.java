package jcompute.scenario;

import java.util.LinkedList;
import java.util.List;

import jcompute.batch.BatchItem;
import jcompute.batch.itemgenerator.ItemGenerator;
import jcompute.simulation.SimulationScenarioManagerInf;
import jcompute.stats.StatGroupSetting;

public interface ScenarioInf
{
	/**
	 * The logic to load and process a configuration file.
	 * It should create all required simulation structures.
	 * 
	 * @param interpreter
	 * @return
	 */
	public boolean loadConfig(ConfigurationInterpreter interpreter);
	
	/**
	 * A unique string indicating what scenario type this plugin is.
	 * 
	 * @return
	 */
	public String getScenarioType();
	
	/**
	 * Plugin version value - its is up to you what this means and what you do with it.
	 * 
	 * @return
	 */
	public double getScenarioVersion();
	
	/**
	 * Get the object that manages the processing for this scenario type.
	 * 
	 * @return
	 */
	public SimulationScenarioManagerInf getSimulationScenarioManager();
	
	/**
	 * The list of settings for all the stat groups.
	 * 
	 * @return
	 */
	public List<StatGroupSetting> getStatGroupSettingsList();
	
	/**
	 * The configuration text used to create this scenario.
	 * 
	 * @return
	 */
	public String getScenarioText();
	
	/**
	 * Checks if a particular end event is set.
	 * 
	 * @param eventName
	 * @return true if the end event is set.
	 */
	public boolean endEventIsSet(String eventName);
	
	/**
	 * Retrives the trigger value for a particular end event.
	 * 
	 * @param eventName
	 * @return
	 */
	public int getEndEventTriggerValue(String eventName);
	
	public static final String INVALID = "Invalid";
	
	/**
	 * Only required in cluster mode.
	 * Note The standard item generator is available and recommended.
	 * 
	 * @param batchId
	 * @param batchName
	 * @param batchConfigProcessor
	 * @param destinationItemList
	 * @param itemSamples
	 * @param progress1dArray
	 * @param baseScenarioText
	 * @param storeStats
	 * @param statsMethodSingleArchive
	 * @param singleArchiveCompressionLevel
	 * @param bosBufferSize
	 * @return the item generator which can generate batch items for this scenario type.
	 */
	public ItemGenerator getItemGenerator(int batchId, String batchName, ConfigurationInterpreter batchConfigProcessor,
	LinkedList<BatchItem> destinationItemList, int itemSamples, double[] progress1dArray, String baseScenarioText, boolean storeStats,
	boolean statsMethodSingleArchive, int singleArchiveCompressionLevel, int bosBufferSize);
}