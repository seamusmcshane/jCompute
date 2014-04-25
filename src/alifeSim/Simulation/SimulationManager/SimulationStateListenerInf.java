package alifeSim.Simulation.SimulationManager;

import alifeSim.Simulation.SimulationState;
import alifeSim.Simulation.SimulationState.SimState;

public interface SimulationStateListenerInf
{
	public void simulationStateChanged(int simId,SimState state);
}
