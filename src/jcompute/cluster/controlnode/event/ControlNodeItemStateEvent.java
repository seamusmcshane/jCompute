package jcompute.cluster.controlnode.event;

import jcompute.simulation.event.SimulationStateChangedEvent;

public class ControlNodeItemStateEvent
{
	private SimulationStateChangedEvent simStateEvent;
	
	public ControlNodeItemStateEvent(SimulationStateChangedEvent simStateEvent)
	{
		this.simStateEvent = simStateEvent;
	}
	
	public SimulationStateChangedEvent getSimStateEvent()
	{
		return simStateEvent;
	}
}
