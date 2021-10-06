package jcompute.scenario;

import java.util.List;

import jcompute.batch.itemgenerator.ItemGenerator;
import jcompute.batch.itemgenerator.ItemGeneratorConfigInf;
import jcompute.batch.itemgenerator.Parameter;
import jcompute.batch.itemstore.ItemStore;
import jcompute.batch.log.item.logger.BatchItemLogInf;
import jcompute.simulation.Simulation;
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
	/** Allows detecting if there is an error loading a plugin */
	public static final String INVALID = "Invalid";
	
	/**
	 * Method to provide the settings for a scenario.
	 * 
	 * @param scenarioSettings
	 * @return true if settings where applied successfully, false on any error.
	 */
	public boolean loadConfig(String configText);
	
	/**
	 * @return The configuration text used to create this scenario.
	 */
	public String getScenarioText();
	
	/**
	 * Returns the class type of the scenario config object
	 * 
	 * @return
	 */
	public Class<?> getScenarioConfigClass();
	
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
	public SimulationScenarioManagerInf getSimulationScenarioManager(Simulation simulation);
	
	/*
	 * ***************************************************************************************************
	 * Data 
	 *****************************************************************************************************/
	
	public boolean needsData();
	
	public String[] dataFileNames();
	
	/*
	 * ***************************************************************************************************
	 * Cluster Support
	 *****************************************************************************************************/
	
	/**
	 * Only required in cluster mode.
	 * Note The standard item generator is available and recommended.
	 * 
	 * @return A configuration for the Item generator type used.
	 */
	public ItemGeneratorConfigInf getItemGeneratorConfig(List<Parameter> parameterList, String baseScenarioText,
	int itemSamples);
	
	/**
	 * Only required in cluster mode.
	 * Note The standard item generator is available and recommended.
	 * 
	 * @return the item generator which can generate batch items for this scenario type.
	 */
	public ItemGenerator getItemGenerator();
	
	/**
	 * Only required in cluster mode.
	 * Item configs generated by the Item Generator can use signigifant amounts of memory.
	 * Allows controlling where the Item configs are stored.
	 * 
	 * @return An Itemstore in which to place item configs.
	 */
	public ItemStore getItemStore();
	
	/**
	 * @return
	 */
	public BatchItemLogInf getItemLogWriter();
}