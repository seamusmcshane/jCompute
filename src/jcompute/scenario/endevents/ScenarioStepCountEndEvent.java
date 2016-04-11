package jcompute.scenario.endevents;

import jcompute.simulation.Simulation;

public class ScenarioStepCountEndEvent implements ScenarioEndEventInf
{
	private String name = "StepCountEndEvent";
	private Simulation simulation;
	private int triggerValue;
	
	public ScenarioStepCountEndEvent(Simulation simulation, int triggerValue)
	{
		this.simulation = simulation;
		this.triggerValue = triggerValue;
		
		simulation.setEndStep(triggerValue);
	}
	
	@Override
	public boolean checkEvent()
	{
		return(simulation.getSimulationSteps() == triggerValue);
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
}
