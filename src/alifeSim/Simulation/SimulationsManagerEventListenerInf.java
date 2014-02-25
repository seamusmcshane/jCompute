package alifeSim.Simulation;

import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Simulation.SimulationsManager.SimulationManagerEvent;

public interface SimulationsManagerEventListenerInf
{
	public void SimulationsManagerEvent(int simId,SimulationManagerEvent event);
}
