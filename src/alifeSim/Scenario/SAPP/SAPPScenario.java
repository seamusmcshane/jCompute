package alifeSim.Scenario.SAPP;

import java.io.File;

import org.ini4j.Wini;

import alifeSim.Alife.SimpleAgent.SimpleAgentManagementSetupParam;
import alifeSim.Scenario.ScenarioVT;

public class SAPPScenario extends ScenarioVT
{	
	// A reused parameter object to carry variables though the classes.
	private static SimpleAgentManagementSetupParam agentSettings = new SimpleAgentManagementSetupParam();
	
	public SAPPScenario(File file)
	{		
		super(file);		
	}	
	
	public void setUp()
	{	
		
		/*
		 * Main Setup
		 */

		/* World Size */
		int worldSize = 1024;

		/* Barrier Mode */
		int barrierMode = 0;

		/* Barrier Scenario */
		int barrierScenario = 0;

		/* Prey Numbers */
		int preyNo = 800;

		/* Pred Numbers */
		int predNo = 0;

		/* Plant Numbers */
		int plantNo = 400;

		/*
		 * Plants via direct variable passing
		 */

		/* Plant Regeneration Rate */
		int plantRegenRate = 1;

		/* Plant Energy Absorption Rate */
		int plantEnergyAbsorptionRate = 8;

		/* Plant Starting Energy */
		int plantStartingEnergy = 25;
		
		/*
		 * Agents setup via agentSettings object
		 */

		/* Speeds */
		agentSettings.setPreySpeed(0.9f);
		agentSettings.setPredatorSpeed(1.0f);

		/* View Ranges */
		agentSettings.setPreyViewRange(25);
		agentSettings.setPredatorViewRange(25);

		/*
		 * Digestive Efficiency - how much energy consumed is converted to
		 * usable...
		 */
		agentSettings.setPreyDE(0.5f);
		agentSettings.setPredatorDE(0.5f);

		/* Reproduction Energy Divider */
		agentSettings.setPreyREDiv(0.5f);
		agentSettings.setPredatorREDiv(0.5f);

		/* Energy Movement Cost */
		agentSettings.setPreyMoveCost(0.025f);
		agentSettings.setPredatorMoveCost(0.025f);

		/* Hunger Threshold */
		agentSettings.setPreyHungerThres(50);
		agentSettings.setPredatorHungerThres(50);

		/* Energy Consumption Rate */
		agentSettings.setPreyConsumptionRate(10);
		agentSettings.setPredatorConsumptionRate(100); // Not Used 100%

		/* Reproduction Cost */
		agentSettings.setPreyRepoCost(0.50f);
		agentSettings.setPredRepoCost(0.50f);

		/* Starting Energy */
		agentSettings.setPreyStartingEnergy(25);
		agentSettings.setPredStartingEnergy(25);
	}
}
