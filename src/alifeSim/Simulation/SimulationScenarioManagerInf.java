package alifeSim.Simulation;

import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Stats.StatManager;
import alifeSimGeom.A2DVector2f;

public interface SimulationScenarioManagerInf
{
	public void cleanUp();

	public void doSimulationUpdate();
	
	public StatManager getStatmanger();
	
	public void drawSim(GUISimulationView simView,boolean viewRangeDrawing,boolean viewsDrawing);

	public int getWorldSize();

	public void displayDebug();
	
	public void resetCamPos(float x, float y);
	
	public void adjCamZoom(float z);

	public float getCamZoom();

	public void resetCamZoom();

	public A2DVector2f getCamPos();

	public void moveCamPos(float x, float y);
	
	public boolean hasEndEventOccurred();

	public String getEndEvent();

	public void setScenarioStepCountEndEvent(SimulationStats simState);
	
	public ScenarioInf getScenario();

}
