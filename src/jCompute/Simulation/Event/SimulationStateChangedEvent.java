package jCompute.Simulation.Event;

import jCompute.Simulation.SimulationState.SimState;
import jCompute.Stats.StatExporter;

public class SimulationStateChangedEvent
{
	private int simId;
	private SimState state;
	private long runTime;
	private String endEvent;
	private long stepCount;
	private StatExporter exporter;
	
	public SimulationStateChangedEvent(int simId, SimState state, long runTime, long stepCount, String endEvent,
			StatExporter exporter)
	{
		this.simId = simId;
		this.state = state;
		this.runTime = runTime;
		this.stepCount = stepCount;
		this.endEvent = endEvent;
		this.exporter = exporter;
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
	
	public StatExporter getStatExporter()
	{
		return exporter;
	}
	
}
