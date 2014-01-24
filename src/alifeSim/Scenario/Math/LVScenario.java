package alifeSim.Scenario.Math;

import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.SAPP.SAPPSimulationManager;
import alifeSim.Simulation.SimulationManagerInf;

public class LVScenario extends ScenarioVT
{
	public SimulationManagerInf simManager;
	
	public LVScenario()
	{		
		super();
	}
	
	@Override
	public void loadConfig(String text)
	{
		super.loadConfig(text);
		
		readScenarioSettings();
		
		simManager = new LVSimulationManager(this);
	}
	
	public boolean readScenarioSettings()
	{
		return false;		
	}
}
