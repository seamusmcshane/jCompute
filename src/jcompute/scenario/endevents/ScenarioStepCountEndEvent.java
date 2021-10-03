package jcompute.scenario.endevents;

import jcompute.simulation.Simulation;

public class ScenarioStepCountEndEvent implements ScenarioEndEventInf
{
	private String name = "StepCount";
	private int triggerValue;
	
	private Simulation simulation;
	
	public ScenarioStepCountEndEvent(int triggerValue)
	{
		this.triggerValue = triggerValue;
	}
	
	@Override
	public boolean checkEvent()
	{
		return(simulation.getTotalSteps() == triggerValue);
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public int getValue()
	{
		return triggerValue;
	}
	
	public void setSimulationReference(Simulation simulation)
	{
		/**
		 * Step count is no longer to be hardcoded as an endevent in every simulation.
		 * As such is is a generic optional end event that can be used or not.
		 * How ever end events are created in the plugin before simualtion managers are created thus this event needs
		 * a way to get the reference after its own creation.
		 */
		this.simulation = simulation;
		
	}
}