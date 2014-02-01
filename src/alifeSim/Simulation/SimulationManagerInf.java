package alifeSim.Simulation;

import alifeSim.Gui.NewSimView;
import alifeSim.Stats.StatManager;

public interface SimulationManagerInf
{
	public void cleanUp();

	public void doSimulationUpdate();
	
	public StatManager getStatmanger();
	
	public void drawSim(NewSimView simView);

	public int getWorldSize();

	public void displayDebug();
}
