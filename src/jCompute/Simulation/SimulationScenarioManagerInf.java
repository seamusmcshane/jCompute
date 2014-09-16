package jCompute.Simulation;

import jCompute.Gui.View.View;
import jCompute.Gui.View.ViewCam;
import jCompute.Scenario.ScenarioInf;
import jCompute.Stats.StatManager;

public interface SimulationScenarioManagerInf
{
	public void cleanUp();

	public void doSimulationUpdate();
	
	public StatManager getStatmanger();
	
	public void drawSim(View simView,boolean viewRangeDrawing,boolean viewsDrawing);

	public int getWorldSize();
	
	public boolean hasEndEventOccurred();

	public String getEndEvent();

	public void setScenarioStepCountEndEvent(SimulationStats simState);
	
	public ScenarioInf getScenario();
	
	public ViewCam getSimViewCam();

	public String getInfo();
}
