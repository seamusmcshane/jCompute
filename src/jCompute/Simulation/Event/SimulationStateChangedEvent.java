package jCompute.Simulation.Event;

import jCompute.Debug.DebugLogger;
import jCompute.Simulation.SimulationState.SimState;

public class SimulationStateChangedEvent
{
	private int simId;
	private SimState state;
	
	public SimulationStateChangedEvent(int simId, SimState state)
	{
		DebugLogger.output("Simulation " + simId + " State " + state.toString());
		this.simId = simId;
		this.state = state;
	}

	public int getSimId()
	{
		return simId;
	}

	public SimState getState()
	{
		return state;
	}
	
}
