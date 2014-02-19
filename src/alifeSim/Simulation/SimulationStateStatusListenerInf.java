package alifeSim.Simulation;

import alifeSim.Simulation.SimulationState.SimStatus;

public interface SimulationStateStatusListenerInf
{
	public void simulationStateStatusChanged(SimStatus status);
}
