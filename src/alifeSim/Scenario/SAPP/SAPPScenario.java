package alifeSim.Scenario.SAPP;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import alifeSim.Alife.GenericPlant.GenericPlantSetupSettings;
import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;
import alifeSim.Alife.SimpleAgent.SimpleAgentSetupSettings;
import alifeSim.Alife.SimpleAgent.SimpleAgentType;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioKeyValuePair;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Stats.StatGroupSetting;
import alifeSim.World.WorldSetupSettings;

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
		for(int i=0;i<agentGroups;i++)
		{
			agentSettings = new SimpleAgentSetupSettings();
			section = "Agents.SimpleAgent("+i+")";
			
			System.out.println("SimpleAgent Group : " + i);
			
			/* Set the Type */
			SimpleAgentType type = new SimpleAgentType(AgentType.INVALID);
			agentSettings.setType(type.typeFromString(super.getStringValue(section, "Type")));
			System.out.println("Agent Type : " + agentSettings.getType());
				
			agentSettings.setInitalNumbers(super.getIntValue(section,"InitialNumbers"));
			System.out.println("InitialNumbers : " + agentSettings.getInitalNumbers());

			agentSettings.setSize(super.getFloatValue(section,"Size"));
			System.out.println("Size : " + agentSettings.getSize());
			
			agentSettings.setSpeed(super.getFloatValue(section,"Speed"));
			System.out.println("Speed : " + agentSettings.getSpeed());

			agentSettings.setViewRange(super.getFloatValue(section,"ViewRange"));
			System.out.println("ViewRange : " + agentSettings.getViewRange());

			agentSettings.setMoveCost(super.getFloatValue(section,"MovementCost"));
			System.out.println("MovementCost : " + agentSettings.getMoveCost());

			agentSettings.setStartingEnergy(super.getFloatValue(section,"StartingEnergy"));
			System.out.println("StartingEnergy : " + agentSettings.getStartingEnergy());

			agentSettings.setDigestiveEfficiency(super.getFloatValue(section,"DigestiveEfficiency"));
			System.out.println("DigestiveEfficiency : " + agentSettings.getDigestiveEfficiency());

			agentSettings.setHungerThres(super.getFloatValue(section,"HungerThreshold"));
			System.out.println("HungerThreshold : " + agentSettings.getHungerThres());

			agentSettings.setConsumptionRate(super.getFloatValue(section,"EnergyConsumptionRate"));
			System.out.println("EnergyConsumptionRate : " + agentSettings.getConsumptionRate());

			agentSettings.setREDiv(super.getFloatValue(section,"ReproductionAndSurvivalDivisor"));
			System.out.println("ReproductionAndSurvivalDivisor : " + agentSettings.getREDiv());


			agentSettings.setReproductionCost(super.getFloatValue(section,"ReproductionCost"));
			System.out.println("ReproductionCost : " + agentSettings.getReproductionCost());

			agentSettingsList.add(agentSettings);
		}
		
		System.out.println("Loaded " + agentGroups + " AgentGroups");
	}
		
	private void readWorldSettings()
	{
		worldSettings = new WorldSetupSettings();

		String section = "World";
		
		worldSettings.setWorldSize(super.getIntValue(section,"Size"));
		System.out.println("World Size : " + worldSettings.getWorldSize());
		
		worldSettings.setBarrierNum(super.getIntValue(section,"Barriers"));
		System.out.println("BarrierMode : " + worldSettings.getBarrierNum());
		
		worldSettings.setBarrierScenario(super.getIntValue(section,"BarriersScenario"));
		System.out.println("BarriersScenario : " + worldSettings.getBarrierScenario());

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
