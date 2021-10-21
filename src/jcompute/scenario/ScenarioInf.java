package jcompute.scenario;

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
	
	/**
	 * Returns the object needed to support cluster operations.
	 * For performance implementers should only create the needed support for a scenario object when this method called the first time.
	 * 
	 * @return null if there is no support.
	 */
	public ClusterSupportInf ClusterSupport();
}