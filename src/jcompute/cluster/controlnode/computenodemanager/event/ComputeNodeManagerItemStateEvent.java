package jcompute.cluster.controlnode.computenodemanager.event;

import jcompute.simulation.event.SimulationStateChangedEvent;

public class ComputeNodeManagerItemStateEvent
{
	private SimulationStateChangedEvent simStateEvent;
	
	public ComputeNodeManagerItemStateEvent(SimulationStateChangedEvent simStateEvent)
	{
		this.simStateEvent = simStateEvent;
	}
	
	public SimulationStateChangedEvent getSimStateEvent()
	{
		return simStateEvent;
	}
}
