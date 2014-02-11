package alifeSim.Simulation;

import alifeSim.Simulation.SimulationState.SimStatus;

public interface SimulationStateListenerInf
{
	public void simulationStateChanged(SimStatus status,long time,long stepNo,int asps);
}
