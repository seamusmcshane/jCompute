package jCompute.SimulationManager.Event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimulationsManagerEvent
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(SimulationsManagerEvent.class);
	
	private int simId;
	private SimulationsManagerEventType eventType;
	
	public SimulationsManagerEvent(int simId, SimulationsManagerEventType eventType)
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
