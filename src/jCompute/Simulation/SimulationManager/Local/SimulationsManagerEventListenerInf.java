package jCompute.Simulation.SimulationManager.Local;

import jCompute.Simulation.SimulationManager.Local.SimulationsManager.SimulationManagerEvent;
import jCompute.Simulation.SimulationState.SimState;

public interface SimulationsManagerEventListenerInf
{
	public void SimulationsManagerEvent(int simId,SimulationManagerEvent event);
}
