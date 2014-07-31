package jCompute.Simulation.SimulationManager.Local;

import jCompute.Simulation.SimulationManager.Local.SimulationsManager.SimulationManagerEvent;

public interface SimulationsManagerEventListenerInf
{
	public void SimulationsManagerEvent(int simId,SimulationManagerEvent event);
}
