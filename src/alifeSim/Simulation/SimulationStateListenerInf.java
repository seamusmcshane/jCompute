package alifeSim.Simulation;

import alifeSim.Simulation.SimulationState.SimState;

public interface SimulationStateListenerInf
{
	public void simulationStateChanged(int simId,SimState state);
}
