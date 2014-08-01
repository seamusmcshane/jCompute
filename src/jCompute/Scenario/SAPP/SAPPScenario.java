package jCompute.Scenario.SAPP;

import jCompute.Debug.DebugLogger;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.ScenarioVT;
import jCompute.Scenario.SAPP.Plant.GenericPlantSetupSettings;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentSetupSettings;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentType;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentEnum.AgentType;
import jCompute.Scenario.SAPP.World.WorldSetupSettings;
import jCompute.Simulation.SimulationScenarioManagerInf;

import java.util.ArrayList;
import java.util.List;

public class SAPPScenario extends ScenarioVT implements ScenarioInf
{	
	
	public SimulationScenarioManagerInf simManager;
	
	/** World Settings */
	public WorldSetupSettings worldSettings;
	
	/** Plant Settings */
	public GenericPlantSetupSettings plantSettings;
	
	/** Agent Settings */
	private ArrayList<SimpleAgentSetupSettings> agentSettingsList;
	
	public SAPPScenario()
	{
		super();
	}
	
	@Override
	public void loadConfig(String text)
	{
		super.loadConfig(text);
		
		readScenarioSettings();
		
		simManager = new SAPPSimulationManager(this);
	}
	
	public boolean readScenarioSettings()
	{
		
		readWorldSettings();
		
		readAgentsSettings();

		readPlantSettings();
		
		readStatSettings();
						
		return true;		
	}

	private void readPlantSettings()
	{
		String section = "Plants.GenericPlant";
		
		plantSettings = new GenericPlantSetupSettings();
		
		plantSettings.setInitialPlantNumbers(super.getIntValue(section,"InitialNumbers"));
			
		plantSettings.setPlantStartingEnergy(super.getIntValue(section,"StartingEnergy"));
			
		plantSettings.setPlantEnergyAbsorptionRate(super.getIntValue(section,"EnergyAbsorptionRate"));
			
		plantSettings.setPlantRegenRate(super.getIntValue(section,"PlantRegeratonRate"));
			
		plantSettings.setPlantRegenerationNSteps(super.getIntValue(section,"PlantRegenerationNSteps"));
		
	}
		
	private void readAgentsSettings()
	{
		int agentGroups = super.getSubListSize("Agents","SimpleAgent");
		
		agentSettingsList = new ArrayList<SimpleAgentSetupSettings>(agentGroups);
		
		SimpleAgentSetupSettings agentSettings;
		String section;
		for(int a=0;a<agentGroups;a++)
		{
			//DebugLogger.output("SimpleAgent Group : " + a);
			section = "Agents.SimpleAgent("+a+")";

			/* Create the settings and the Name */			
			agentSettings = new SimpleAgentSetupSettings(super.getStringValue(section, "Name"));
			
			/* Set the color */
			int red 	= super.getIntValue(section, "Color.Red");
			int green 	= super.getIntValue(section, "Color.Green");
			int blue 	= super.getIntValue(section, "Color.Blue");			
			
			agentSettings.setColor(red, green, blue);
			//DebugLogger.output("Color : " + red+" "+green+" "+blue);
			
			/* Set the Type */
			SimpleAgentType type = new SimpleAgentType(AgentType.INVALID);
			agentSettings.setType(type.typeFromString(super.getStringValue(section, "AgentType")));
			//DebugLogger.output("Agent Type : " + agentSettings.getType());
				
			agentSettings.setInitalNumbers(super.getIntValue(section,"InitialNumbers"));
			//DebugLogger.output("InitialNumbers : " + agentSettings.getInitalNumbers());

			agentSettings.setSize(super.getFloatValue(section,"Size"));
			//DebugLogger.output("Size : " + agentSettings.getSize());
			
			agentSettings.setSpeed(super.getFloatValue(section,"Speed"));
			//DebugLogger.output("Speed : " + agentSettings.getSpeed());

			agentSettings.setViewRange(super.getFloatValue(section,"ViewRange"));
			//DebugLogger.output("ViewRange : " + agentSettings.getViewRange());

			agentSettings.setMoveCost(super.getFloatValue(section,"MovementCost"));
			//DebugLogger.output("MovementCost : " + agentSettings.getMoveCost());

			agentSettings.setStartingEnergy(super.getFloatValue(section,"StartingEnergy"));
			//DebugLogger.output("StartingEnergy : " + agentSettings.getStartingEnergy());

			agentSettings.setDigestiveEfficiency(super.getFloatValue(section,"DigestiveEfficiency"));
			//DebugLogger.output("DigestiveEfficiency : " + agentSettings.getDigestiveEfficiency());

			agentSettings.setHungerThres(super.getFloatValue(section,"HungerThreshold"));
			//DebugLogger.output("HungerThreshold : " + agentSettings.getHungerThres());

			agentSettings.setConsumptionRate(super.getFloatValue(section,"EnergyConsumptionRate"));
			//DebugLogger.output("EnergyConsumptionRate : " + agentSettings.getConsumptionRate());

			agentSettings.setREDiv(super.getFloatValue(section,"ReproductionAndSurvivalDivisor"));
			//DebugLogger.output("ReproductionAndSurvivalDivisor : " + agentSettings.getREDiv());


			agentSettings.setReproductionCost(super.getFloatValue(section,"ReproductionCost"));
			//DebugLogger.output("ReproductionCost : " + agentSettings.getReproductionCost());

			agentSettingsList.add(agentSettings);
		}
		
		//DebugLogger.output("Loaded " + agentGroups + " AgentGroups");
	}
		
	private void readWorldSettings()
	{
		worldSettings = new WorldSetupSettings();

		String section = "World";
		
		worldSettings.setWorldSize(super.getIntValue(section,"Size"));
		//DebugLogger.output("World Size : " + worldSettings.getWorldSize());
		
		worldSettings.setBarrierNum(super.getIntValue(section,"Barriers"));
		//DebugLogger.output("BarrierMode : " + worldSettings.getBarrierNum());
		
		worldSettings.setBarrierScenario(super.getIntValue(section,"BarriersScenario"));
		//DebugLogger.output("BarriersScenario : " + worldSettings.getBarrierScenario());

	}
	
	@Override
	public SimulationScenarioManagerInf getSimulationScenarioManager()
	{
		return simManager;		
	}

	public List<SimpleAgentSetupSettings> getAgentSettingsList()
	{
		return agentSettingsList;
	}
	
	public String getScenarioText()
	{
		return super.scenarioText;
	}
	
}
