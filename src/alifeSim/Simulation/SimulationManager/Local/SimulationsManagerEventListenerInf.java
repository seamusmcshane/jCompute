package alifeSim.Simulation.SimulationManager.Local;

import alifeSim.Simulation.SimulationManager.Local.SimulationsManager.SimulationManagerEvent;
import alifeSim.Simulation.SimulationState.SimState;

public interface SimulationsManagerEventListenerInf
{
	public void SimulationsManagerEvent(int simId,SimulationManagerEvent event);
}
