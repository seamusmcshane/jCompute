package jCompute.Simulation;

import jCompute.Gui.View.GUISimulationView;
import jCompute.Gui.View.SimViewCam;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Scenario.ScenarioInf;
import jCompute.Stats.StatManager;

public interface SimulationScenarioManagerInf
{
	public void cleanUp();

	public void doSimulationUpdate();
	
	public StatManager getStatmanger();
	
	public void drawSim(GUISimulationView simView,boolean viewRangeDrawing,boolean viewsDrawing);

	public int getWorldSize();
	
	public boolean hasEndEventOccurred();

	public String getEndEvent();

	public void setScenarioStepCountEndEvent(SimulationStats simState);
	
	public ScenarioInf getScenario();
	
	public SimViewCam getSimViewCam();
}
