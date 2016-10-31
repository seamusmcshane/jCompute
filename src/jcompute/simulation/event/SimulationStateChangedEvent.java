package jcompute.simulation.event;

import jcompute.results.ResultExporter;
import jcompute.simulation.SimulationState.SimState;

public class SimulationStateChangedEvent
{
	private int simId;
	private SimState state;
	private long runTime;
	private String endEvent;
	private long stepCount;
	private ResultExporter exporter;
	
	public SimulationStateChangedEvent(int simId, SimState state, long runTime, long stepCount, String endEvent, ResultExporter exporter)
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
	
	public ResultExporter getStatExporter()
	{
		return exporter;
	}
	
}
