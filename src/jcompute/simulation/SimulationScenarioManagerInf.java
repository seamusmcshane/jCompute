package jcompute.simulation;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.scenario.ScenarioInf;
import jcompute.stats.StatManager;

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

	public String getHelpTitleText();
	public String[] getHelpKeyList();
}
