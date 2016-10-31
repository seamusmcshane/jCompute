package jcompute.scenario;

import java.util.LinkedList;
import java.util.List;

import jcompute.batch.BatchItem;
import jcompute.batch.itemgenerator.ItemGenerator;
import jcompute.results.trace.group.TraceGroupSetting;
import jcompute.simulation.SimulationScenarioManagerInf;

/**
 * The scenario plugin-interface.
 * Controls setup of values from configuration file.
 * It also interfaces with the subsystems for
 * Statistics, View, Simulation and Batch execution and Batch Item Generation by providing the correct support objects.
 * 
 * @author Seamus McShane
 */
public interface ScenarioInf
{
	/**
	 * The logic to load and process a configuration file.
	 * It must create all required simulation structures ready for processing.
	 * 
	 * @param interpreter
	 * @return true if the configuration was successful or false for any error.
	 */
	public boolean loadConfig(ConfigurationInterpreter interpreter);
	
	/**
	 * @return String (Unique) identifier for this scenario plugin type.
	 */
	public String getScenarioType();
	
	/**
	 * @return A double representing the scenario version.
	 */
	public double getScenarioVersion();
	
	/**
	 * Gets the object that manages the processing for this scenario type.
	 * 
	 * @return A fully configured simulation manager object for this scenario type.
	 */
	public SimulationScenarioManagerInf getSimulationScenarioManager();
	
	/**
	 * @return The list of settings for all the stat groups.
	 */
	public List<TraceGroupSetting> getStatGroupSettingsList();
	
	/**
	 * @return The configuration text used to create this scenario.
	 */
	public String getScenarioText();
	
	/**
	 * @param eventName
	 * @return true if a particular end event is set
	 */
	public boolean endEventIsSet(String eventName);
	
	/**
	 * @param eventName
	 * @return The trigger value for a particular end event.
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