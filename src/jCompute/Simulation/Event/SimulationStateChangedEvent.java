package jCompute.Simulation.Event;

import jCompute.Debug.DebugLogger;
import jCompute.Simulation.SimulationState.SimState;

public class SimulationStateChangedEvent
{
	private int simId;
	private SimState state;
	private long runTime;
	private String endEvent;
	private long stepCount;
	
	public SimulationStateChangedEvent(int simId, SimState state, long runTime, long stepCount , String endEvent)
	{
		DebugLogger.output("SimulationStateChangedEvent " + simId + " State " + state.toString());
		this.simId = simId;
		this.state = state;
		this.runTime = runTime;
		this.stepCount = stepCount;
		this.endEvent = endEvent;
	}

	public int getSimId()
	{
		return simId;
	}

	public SimState getState()
	{
		return state;
	}
	
	public long getRunTime()
	{
		return runTime;
	}
	
	public long getStepCount()
	{
		return stepCount;
	}
	
	public String getEndEvent()
	{
		return endEvent;
	}
	
}
