package alifeSim.Scenario.EndEvents;

import alifeSim.Simulation.SimulationState;

public class ScenarioStepCountEndEvent implements ScenarioEndEventInf
{
	private SimulationState state;
	private int endStepNum;
	
	public ScenarioStepCountEndEvent(SimulationState state,int endStepNum)
	{
		this.state = state;
		this.endStepNum = endStepNum;
	}

	@Override
	public boolean checkEvent()
	{
		return (state.getSimulationSteps() == endStepNum);
	}
}
