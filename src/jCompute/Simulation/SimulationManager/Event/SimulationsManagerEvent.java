package jCompute.Simulation.SimulationManager.Event;

import jCompute.Debug.DebugLogger;

public class SimulationsManagerEvent
{
	private int simId;
	private SimulationsManagerEventType eventType;
	
	public SimulationsManagerEvent(int simId,SimulationsManagerEventType eventType)
	{
		this.simId = simId;
		this.eventType = eventType;
		
		DebugLogger.output("Created new SimulationsManagerEvent : " + eventType.toString() + " " + simId);
	}
	
	public int getSimId()
	{
		return simId;
	}
	
	public SimulationsManagerEventType getEventType()
	{
		return eventType;
	}
	

}
