package jCompute.Simulation;

import jCompute.Scenario.ScenarioInf;
import jCompute.Stats.StatManager;
import jCompute.gui.view.ViewRendererInf;

public interface SimulationScenarioManagerInf
{
	public void cleanUp();
	
	public void doSimulationUpdate();
	
	public StatManager getStatmanger();
	
	public int getUniverseSize();
	
	public boolean hasEndEventOccurred();
	
	public String getEndEvent();
	
	public void setScenarioStepCountEndEvent(Simulation simulation);
	
	public ScenarioInf getScenario();
	
	public String getInfo();
	
	public ViewRendererInf getRenderer();
}
