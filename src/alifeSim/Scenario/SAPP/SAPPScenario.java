package alifeSim.Scenario.SAPP;

import java.io.File;

import alifeSim.Alife.GenericPlant.GenericPlantSetupSettings;
import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;
import alifeSim.Alife.SimpleAgent.SimpleAgentSetupSettings;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Simulation.SimulationManagerInf;
import alifeSim.World.WorldSetupSettings;

public class SAPPScenario extends ScenarioVT
{	
	
	public SimulationManagerInf simManager;
	
	/** World Settings */
	public WorldSetupSettings worldSettings;
	
	/** Plant Settings */
	public GenericPlantSetupSettings plantSettings;
	
	/** Agent Settings */
	public SimpleAgentSetupSettings predatorAgentSettings;
	public SimpleAgentSetupSettings preyAgentSettings;
	
	public SAPPScenario(String text)
	{		
		super(text);
				
		readScenarioSettings();
		
		simManager = new SAPPSimulationManager(this);
		
	}	
	
	public void readScenarioSettings()
	{	
		
		readWorldSettings();
		
		readAgentsSettings();

		readPlantSettings();
		
	}
	
	public void readPlantSettings()
	{			
		
		String section = "Plants";
		
		plantSettings = new GenericPlantSetupSettings();
		
		plantSettings.setInitialPlantNumbers(super.getIntValue(section,"InitialNumbers"));
		
		plantSettings.setPlantStartingEnergy(super.getIntValue(section,"StartingEnergy"));
		
		plantSettings.setPlantEnergyAbsorptionRate(super.getIntValue(section,"EnergyAbsorptionRate"));
		
		plantSettings.setPlantRegenRate(super.getIntValue(section,"PlantRegeratonRate"));
		
		plantSettings.setPlantRegenerationNSteps(super.getIntValue(section,"plantRegenerationNSteps"));
				
	}
	
	private void readAgentsSettings()
	{
		readAgentSettings(AgentType.PREDATOR);
		readAgentSettings(AgentType.PREY);
	}
	
	private void readAgentSettings(AgentType type)
	{
		SimpleAgentSetupSettings agentSettings = new SimpleAgentSetupSettings();
		
		String section = "Invalid";
		
		/* Decide on Agent Type */
		if(type == AgentType.PREDATOR)
		{
			section = "Predators";			
			predatorAgentSettings = agentSettings;
		}
		else
		{
			section = "Prey";
			preyAgentSettings = agentSettings;
		}
		
		/* Now read the settings */
		
		agentSettings.setInitalNumbers(super.getIntValue(section,"InitialNumbers"));
				
		agentSettings.setSpeed(super.getFloatValue(section,"Speed"));
		
		agentSettings.setViewRange(super.getFloatValue(section,"ViewRange"));
		
		agentSettings.setMoveCost(super.getFloatValue(section,"MovementCost"));
		
		agentSettings.setStartingEnergy(super.getFloatValue(section,"StartingEnergy"));
		
		agentSettings.setDigestiveEfficiency(super.getFloatValue(section,"DigestiveEfficiency"));
		
		agentSettings.setHungerThres(super.getFloatValue(section,"HungerThreshold"));
		
		agentSettings.setConsumptionRate(super.getFloatValue(section,"EnergyConsumptionRate"));

		agentSettings.setREDiv(super.getFloatValue(section,"ReproductionAndSurvivalDivisor"));
		
		agentSettings.setReproductionCost(super.getFloatValue(section,"ReproductionCost"));
		
	}
	
	private void readWorldSettings()
	{
		worldSettings = new WorldSetupSettings();

		String section = "World";
		
		worldSettings.setWorldSize(super.getIntValue(section,"WorldSize"));
		
		worldSettings.setBarrierMode(super.getIntValue(section,"Barriers"));
		
		worldSettings.setBarrierScenario(super.getIntValue(section,"BarrierScenario"));
	}
	
	public SimulationManagerInf getSimManager()
	{
		return simManager;
		
	}
	
}
