package alifeSim.Scenario.Debug;

import java.io.File;

import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.SAPP.SAPPSimulationManager;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Stats.StatManager;
import alifeSim.World.WorldSetupSettings;

public class DebugScenario extends ScenarioVT implements ScenarioInf
{
	/** World Settings */
	public WorldSetupSettings worldSettings;
	
	public DebugSimulationManager simManager;
	
	private int debugAgentMouseCTRL;
	private int testAgentNum;
	private int randomTestArrangement;
	
	public DebugScenario(String text)
	{
		super();
		
		super.loadConfig(text);
		
		setScenarioSettings();
		
		simManager = new DebugSimulationManager(this);
	}

	private void setScenarioSettings()
	{
		setDebugSettings();
		
		setWorldSettings();	
	}
	
	private void setDebugSettings()
	{
		debugAgentMouseCTRL = 1;	
		testAgentNum = 4;
		randomTestArrangement = 0;
	}
	
	public WorldSetupSettings getWorldSettings()
	{
		return worldSettings;
	}

	public int getDebugAgentMouseCTRL()
	{
		return debugAgentMouseCTRL;
	}

	public int getTestAgentNum()
	{
		return testAgentNum;
	}

	public int getRandomTestArrangement()
	{
		return randomTestArrangement;
	}

	private void setWorldSettings()
	{
		worldSettings = new WorldSetupSettings();
		
		worldSettings.setWorldSize(512);
		
		worldSettings.setBarrierNum(0);
		
		worldSettings.setBarrierScenario(0);
	}

	@Override
	public SimulationScenarioManagerInf getSimulationScenarioManager()
	{
		return simManager;
	}
	
	
	public String getScenarioText()
	{
		return super.scenarioText;
	}

}
