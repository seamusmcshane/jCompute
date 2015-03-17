package jCompute.Simulation;

import jCompute.Gui.View.ViewRendererInf;
import jCompute.Scenario.ScenarioInf;
import jCompute.Stats.StatManager;

public interface SimulationScenarioManagerInf
{
	public void cleanUp();

	public void doSimulationUpdate();
	
	public StatManager getStatmanger();
	
	public int getUniverseSize();
	
	public boolean hasEndEventOccurred();

	public String getEndEvent();

	public void setScenarioStepCountEndEvent(SimulationStats simState);
	
	public ScenarioInf getScenario();
	
	public String getInfo();

	public ViewRendererInf getRenderer();
}
