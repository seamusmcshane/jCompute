package jcompute.simulation;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.results.ResultManager;
import jcompute.scenario.ScenarioInf;

/**
 * Implementers of this class manage the processing of single simulation of their plugin-type.
 * 
 * @author Seamus McShane
 */
public interface SimulationScenarioManagerInf
{
	
	/*
	 * ***************************************************************************************************
	 * Simulation
	 *****************************************************************************************************/
	
	/**
	 * This method contains the logic to compute the problem at hand, it will be called repeatedly until an endevent triggers.
	 * Code to solve the problem should be designed with the following in mind
	 * - It cannot be coded as one self contained tight loop that attempts complete the whole problem within one method call.
	 * - but make incremental progress with successive calls.
	 * - E.g If a task took 100,000 iterations to complete with in a loop, then here you would be making the the progress equivalent to one complete loop.
	 * The method must be synchronised with the cleanup method and the render() method of the render object.
	 */
	public void doSimulationUpdate();
	
	/**
	 * If the simulation needs data it will be retrieved and set by this call back.
	 * 
	 * @param data
	 */
	public void setData(byte[][] data);
	
	/**
	 * Called at the end of every step.
	 * 
	 * @return true if one of the set end events has triggered.
	 * In which case the event name should be stored.
	 */
	public boolean hasEndEventOccurred();
	
	/**
	 * @return The end event that has triggered.
	 * If multiple are expected to trigger at the same time - It will on your implementation of hasEndEventOccurred() which gets priority.
	 */
	public String getEndEvent();
	
	/**
	 * This method is called after an end event has triggered.
	 * It is an opportunity to perform some light processing, such as generating a log line for custom item results.
	 * It should not be used to perform an extra step update.
	 * 
	 * @param simulationSteps
	 * @param averageStepRate
	 * @param totalStepsTime
	 */
	public void finalProcessing(long runTime, int simulationSteps, int averageStepRate);
	
	/**
	 * This method is called when a simulation is being destroyed.
	 * - You should assume this method can be called at anytime..
	 * - If the simulation uses native memory or files they must be closed/freed now.
	 * This method must be synchronised the doSimulationUpdate method and the render() method of the render object.
	 */
	public void cleanUp();
	
	/*
	 * ***************************************************************************************************
	 * Statistics and Information
	 *****************************************************************************************************/
	
	/**
	 * Retries a fully configured result manager for this simulation.
	 * 
	 * @return a result manager object.
	 */
	public ResultManager getResultManager();
	
	/**
	 * Used by GUI Tabs to populate editor text from a running simulation.
	 * 
	 * @return
	 */
	public ScenarioInf getScenario();	
	
	/**
	 * If the plugin has a visual representation, this value is used to scale to the screen.
	 * 
	 * @return
	 */
	public int getUniverseSize();
	
	/*
	 * ***************************************************************************************************
	 * Renderer
	 *****************************************************************************************************/
	
	/**
	 * @return a String used as the title text in the view.
	 */
	public String getInfo();
	
	/**
	 * This method retrieves the renderer object for this simulation type or null if the simulation does no support a renderer.
	 * 
	 * @return the renderer used to create a visual representation of the simulation.
	 */
	public ViewRendererInf getRenderer();
	
	/**
	 * Text to be used as the help menu title in the renderer when help key is triggered.
	 * 
	 * @return
	 */
	public String getHelpTitleText();
	
	/**
	 * Two string columns as a packed array.
	 * Typically used to display key mapping and associated action.
	 * Displayed in the renderer when help key is triggered.
	 * 
	 * @return
	 */
	public String[] getHelpKeyList();
}
