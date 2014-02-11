package alifeSim.Simulation;

import alifeSim.Simulation.SimulationState.SimStatus;

public interface SimulationStatusListenerInf
{
	public void simulationStatusChanged(SimStatus status);
}
