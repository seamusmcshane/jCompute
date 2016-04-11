package jCompute.cluster.controlnode.computenodemanager.event;

import jCompute.Simulation.Event.SimulationStateChangedEvent;

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
