package jCompute.Simulation.Listener;

import jCompute.Simulation.SimulationState.SimState;

public interface SimulationStateListenerInf
{
	public void simulationStateChanged(int simId,SimState state);
}
