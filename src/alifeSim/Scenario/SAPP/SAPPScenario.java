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
	
	public boolean readScenarioSettings()
	{	
		
		readWorldSettings();
		
		readAgentsSettings();

		readPlantSettings();
		
		return true;
		
	}
	
	public void readPlantSettings()
	{			
		
		String section = "Plants";
		
		plantSettings = new GenericPlantSetupSettings();
		
		if(super.hasSection("Plants"))
		{
			plantSettings.setInitialPlantNumbers(super.getIntValue(section,"InitialNumbers"));
			
			plantSettings.setPlantStartingEnergy(super.getIntValue(section,"StartingEnergy"));
			
			plantSettings.setPlantEnergyAbsorptionRate(super.getIntValue(section,"EnergyAbsorptionRate"));
			
			plantSettings.setPlantRegenRate(super.getIntValue(section,"PlantRegeratonRate"));
			
			plantSettings.setPlantRegenerationNSteps(super.getIntValue(section,"plantRegenerationNSteps"));
		}
		else
		{
			System.out.println("No Plants Requested");
		}

				
	}
	
	private void readAgentsSettings()
	{
		readAgentSettings(AgentType.PREDATOR);
		readAgentSettings(AgentType.PREY);
	}
	
	private void readAgentSettings(AgentType type)
	{
		SimpleAgentSetupSettings agentSettings = new SimpleAgentSetupSettings();
		
		String section = "";
		
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
		if(super.hasSection(section))
		{
			System.out.println(section + " Setting");

			if(super.hasIntValue(section, "InitialNumbers"))
			{
				agentSettings.setInitalNumbers(super.getIntValue(section,"InitialNumbers"));
				System.out.println("InitialNumbers : " + agentSettings.getInitalNumbers());
			}
			else
			{			
				System.out.println("InitialNumbers not set using Default : " + agentSettings.getInitalNumbers());
			}
			
			if(super.hasFloatValue(section, "Speed"))
			{
				agentSettings.setSpeed(super.getFloatValue(section,"Speed"));
				System.out.println("Speed : " + agentSettings.getSpeed());
			}
			else
			{
				System.out.println("Speed not set using Default : " + agentSettings.getSpeed());			
			}
			
			if(super.hasFloatValue(section, "ViewRange"))
			{
				agentSettings.setViewRange(super.getFloatValue(section,"ViewRange"));
				System.out.println("ViewRange : " + agentSettings.getViewRange());
			}
			else
			{
				System.out.println("Speed not set using Default : " + agentSettings.getViewRange());			
			}
			
			if(super.hasFloatValue(section, "MovementCost"))
			{
				agentSettings.setMoveCost(super.getFloatValue(section,"MovementCost"));
				System.out.println("MovementCost : " + agentSettings.getMoveCost());
			}
			else
			{
				System.out.println("MovementCost not set using Default : " + agentSettings.getMoveCost());			
			}
			
			if(super.hasFloatValue(section, "StartingEnergy"))
			{
				agentSettings.setStartingEnergy(super.getFloatValue(section,"StartingEnergy"));
				System.out.println("StartingEnergy : " + agentSettings.getStartingEnergy());
			}
			else
			{
				System.out.println("StartingEnergy not set using Default : " + agentSettings.getStartingEnergy());			
			}
			
			if(super.hasFloatValue(section, "DigestiveEfficiency"))
			{
				agentSettings.setDigestiveEfficiency(super.getFloatValue(section,"DigestiveEfficiency"));
				System.out.println("DigestiveEfficiency : " + agentSettings.getDigestiveEfficiency());
			}
			else
			{
				System.out.println("DigestiveEfficiency not set using Default : " + agentSettings.getDigestiveEfficiency());			
			}
			
			if(super.hasFloatValue(section, "HungerThreshold"))
			{
				agentSettings.setHungerThres(super.getFloatValue(section,"HungerThreshold"));
				System.out.println("HungerThreshold : " + agentSettings.getHungerThres());

			}
			else
			{
				System.out.println("HungerThreshold not set using Default : " + agentSettings.getHungerThres());			
			}
			
			
			if(super.hasFloatValue(section, "EnergyConsumptionRate"))
			{
				agentSettings.setConsumptionRate(super.getFloatValue(section,"EnergyConsumptionRate"));
				System.out.println("EnergyConsumptionRate : " + agentSettings.getConsumptionRate());
			}
			else
			{
				System.out.println("EnergyConsumptionRate not set using Default : " + agentSettings.getConsumptionRate());			
			}
			
			if(super.hasFloatValue(section, "ReproductionAndSurvivalDivisor"))
			{
				agentSettings.setREDiv(super.getFloatValue(section,"ReproductionAndSurvivalDivisor"));
				System.out.println("ReproductionAndSurvivalDivisor : " + agentSettings.getREDiv());
			}
			else
			{
				System.out.println("ReproductionAndSurvivalDivisor not set using Default : " + agentSettings.getREDiv());			
			}
			
			if(super.hasFloatValue(section, "ReproductionCost"))
			{
				agentSettings.setReproductionCost(super.getFloatValue(section,"ReproductionCost"));
				System.out.println("ReproductionCost : " + agentSettings.getReproductionCost());
			}
			else
			{
				System.out.println("ReproductionCost not set using Default : " + agentSettings.getReproductionCost());			
			}			
			
		}
		else
		{
			System.out.println("No " + section + " Section");
		}
		
		


	}
	
	private void readWorldSettings()
	{
		worldSettings = new WorldSetupSettings();

		String section = "World";
				
		if(super.hasSection(section))
		{
			if(super.hasIntValue(section, "WorldSize"))
			{
				worldSettings.setWorldSize(super.getIntValue(section,"WorldSize"));
				System.out.println("World Size : " + worldSettings.getWorldSize());
			}
			else
			{
				System.out.println("WorldSize not set using Default : " + worldSettings.getWorldSize());
			}
			
			if(super.hasIntValue(section, "Barriers"))
			{
				worldSettings.setBarrierMode(super.getIntValue(section,"Barriers"));
				System.out.println("BarrierMode : " + worldSettings.getBarrierMode());
			}
			else
			{
				System.out.println("BarrierMode not set using Default : " + worldSettings.getBarrierMode());
			}
			
			if(super.hasIntValue(section, "BarrierScenario"))
			{
				worldSettings.setBarrierScenario(super.getIntValue(section,"BarrierScenario"));
				System.out.println("BarrierScenario : " + worldSettings.getBarrierScenario());
			}
			else
			{
				System.out.println("BarrierScenario not set using Default : " + worldSettings.getBarrierScenario());
			}
		}
		else
		{
			System.out.println("No " + section + " Section");

		}
		
	}
	
	public SimulationManagerInf getSimManager()
	{
		return simManager;
		
	}
	
}
