package alifeSim.Scenario.Debug;

import java.io.File;

import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.SAPP.SAPPSimulationManager;
import alifeSim.World.WorldSetupSettings;

public class DebugScenario extends ScenarioVT
{
	/** World Settings */
	public WorldSetupSettings worldSettings;
	
	public DebugSimulationManager simManager;
	
	private int debugAgentMouseCTRL;
	private int testAgentNum;
	private int randomTestArrangement;
	
	public DebugScenario(File file)
	{
		super(file);
		
		readScenarioSettings (file);
		
		simManager = new DebugSimulationManager(this);
	}

	private void readScenarioSettings(File file)
	{
		readDebugSettings(file);
		
		readWorldSettings(file);	
	}
	
	private void readDebugSettings(File file)
	{
		String section = "Debug Agent";
		debugAgentMouseCTRL = super.getIntValue(section,"MouseCtrl");
		
		section = "Test Agent";
		
		testAgentNum = super.getIntValue(section,"TestAgentNumbers");
		
		randomTestArrangement = super.getIntValue(section,"RandomTestArrangement");
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

	private void readWorldSettings(File file)
	{
		worldSettings = new WorldSetupSettings();

		String section = "World";
		
		worldSettings.setWorldSize(super.getIntValue(section,"WorldSize"));
		
		worldSettings.setBarrierMode(super.getIntValue(section,"Barriers"));
		
		worldSettings.setBarrierScenario(super.getIntValue(section,"BarrierScenario"));
	}
}
