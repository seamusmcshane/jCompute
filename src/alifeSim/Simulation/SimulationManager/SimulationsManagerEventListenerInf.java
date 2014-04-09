package alifeSim.Simulation.SimulationManager;

import alifeSim.Simulation.SimulationManager.SimulationsManager.SimulationManagerEvent;
import alifeSim.Simulation.SimulationState.SimState;

public interface SimulationsManagerEventListenerInf
{
	public void SimulationsManagerEvent(int simId,SimulationManagerEvent event);
}
