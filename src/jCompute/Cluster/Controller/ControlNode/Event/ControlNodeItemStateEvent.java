package jCompute.Cluster.Controller.ControlNode.Event;

import jCompute.Simulation.Event.SimulationStateChangedEvent;

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
