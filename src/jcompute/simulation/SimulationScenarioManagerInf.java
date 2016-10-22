package jcompute.simulation;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.scenario.ScenarioInf;
import jcompute.stats.StatManager;

public interface SimulationScenarioManagerInf
{
	
	/*
	 * ***************************************************************************************************
	 * Simulation
	 *****************************************************************************************************/
	
	/**
	 * This method contains the logic to compute the problem at hand.
	 * Code to solve the problem should be designed with the following in mind
	 * - It cannot be coded as one self contained tight loop that attempts complete the whole problem within one method call.
	 * - but make incremental progress with successive calls.
	 * - E.g If a task took 100,000 iterations to complete with in a loop, then here you would be making the the progress equivalent to one complete loop.
	 * The method must be synchronised with the cleanup method and the render() method of the render object.
	 */
	public void doSimulationUpdate();
	
	/**
	 * @return true if one of the set end events has triggered.
	 */
	public boolean hasEndEventOccurred();
	
	/**
	 * @return The end event that has triggered.
	 * If multiple are expected to trigger at the same time - It will on your design which gets priority.
	 */
	public String getEndEvent();
	
	/**
	 * This method is equivalent to call-back.
	 * It allows a special end event to be set that all simulations must support.
	 * It is the bare minimum end event required by cluster operation so that task completion can be implemented.
	 * For interactive mode setting 0 as the trigger value will suppress this end event and allow continuous running requiring manual intervention to stop.
	 * 
	 * @param simulation
	 * The simulation container.
	 */
	public void setScenarioStepCountEndEvent(Simulation simulation);
	
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
	
	public StatManager getStatmanger();
	
	/**
	 * Used scheduling and control logic.
	 * 
	 * @return
	 */
	public ScenarioInf getScenario();
	
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
	 * A two string columns as a packed array.
	 * Typically used to display key mapping and associated action.
	 * Displayed in the renderer when help key is triggered.
	 * 
	 * @return
	 */
	public String[] getHelpKeyList();
}
