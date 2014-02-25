package alifeSim.Scenario.EndEvents;

import alifeSim.Simulation.SimulationStats;

public class ScenarioStepCountEndEvent implements ScenarioEndEventInf
{
	private SimulationStats stat;
	private int endStepNum;
	
	public ScenarioStepCountEndEvent(SimulationStats stat,int endStepNum)
	{
		this.stat = stat;
		this.endStepNum = endStepNum;
	}

	@Override
	public boolean checkEvent()
	{
		stat.updateProgress(endStepNum);
		
		return (stat.getSimulationSteps() == endStepNum);
	}
}
