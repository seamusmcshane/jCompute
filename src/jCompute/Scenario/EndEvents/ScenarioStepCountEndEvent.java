package jCompute.Scenario.EndEvents;

import jCompute.Simulation.SimulationStats;

public class ScenarioStepCountEndEvent implements ScenarioEndEventInf
{
	private String name = "StepCountEndEvent";
	private SimulationStats stat;
	private int triggerValue;
	
	public ScenarioStepCountEndEvent(SimulationStats stat,int triggerValue)
	{
		this.stat = stat;
		this.triggerValue = triggerValue;
	}

	@Override
	public boolean checkEvent()
	{
		stat.updateProgress(triggerValue);
		
		return (stat.getSimulationSteps() == triggerValue);
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
