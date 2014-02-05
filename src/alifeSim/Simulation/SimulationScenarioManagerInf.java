package alifeSim.Simulation;

import alifeSim.Gui.NewSimView;
import alifeSim.Stats.StatManager;

public interface SimulationScenarioManagerInf
{
	public void cleanUp();

	public void doSimulationUpdate();
	
	public StatManager getStatmanger();
	
	public void drawSim(NewSimView simView,boolean viewRangeDrawing,boolean viewsDrawing);

	public int getWorldSize();

	public void displayDebug();
}
