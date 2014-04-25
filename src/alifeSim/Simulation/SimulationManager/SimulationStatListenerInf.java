package alifeSim.Simulation.SimulationManager;

public interface SimulationStatListenerInf
{
	public void simulationStatChanged(int simId,long time,int stepNo, int progress, int asps);
}
