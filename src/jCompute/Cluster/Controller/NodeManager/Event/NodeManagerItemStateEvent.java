package jCompute.Cluster.Controller.NodeManager.Event;

import jCompute.Simulation.Event.SimulationStateChangedEvent;

public class NodeManagerItemStateEvent
{
	private SimulationStateChangedEvent simStateEvent;
	
	public NodeManagerItemStateEvent(SimulationStateChangedEvent simStateEvent)
	{
		this.simStateEvent = simStateEvent;
	}
	
	public SimulationStateChangedEvent getSimStateEvent()
	{
		return simStateEvent;
	}
}
