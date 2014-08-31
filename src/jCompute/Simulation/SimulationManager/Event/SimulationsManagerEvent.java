package jCompute.Simulation.SimulationManager.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationsManagerEvent
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(SimulationsManagerEvent.class);
	
	private int simId;
	private SimulationsManagerEventType eventType;
	
	public SimulationsManagerEvent(int simId,SimulationsManagerEventType eventType)
	{
		this.simId = simId;
		this.eventType = eventType;
		
		log.debug("Created new SimulationsManagerEvent : " + eventType.toString() + " " + simId);
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
