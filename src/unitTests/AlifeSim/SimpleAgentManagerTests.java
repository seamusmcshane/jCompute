package unitTests.AlifeSim;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import alifeSim.Alife.SimpleAgent.SimpleAgentSetupSettings;
import alifeSim.Alife.SimpleAgent.SimpleAgentManager;
import alifeSim.Simulation.BarrierManager;
import alifeSim.World.World;
import alifeSim.World.WorldSetupSettings;
/**
 * 
 * Simple Agent Manager Tests
 * Testing that the numbers of initial Agents requested are correctly set.
 *
 */
public class SimpleAgentManagerTests
{
	SimpleAgentManager agentManger;

	BarrierManager barrierManager = null;;
	int worldSize = 1024;

	WorldSetupSettings worldSettings = new WorldSetupSettings();
	SimpleAgentSetupSettings predatorAgentSettings = new SimpleAgentSetupSettings();
	SimpleAgentSetupSettings preyAgentSettings = new SimpleAgentSetupSettings();
	
	int agentPreyNumbers = 512;
	int agentPredatorNumbers = 256;

	@Before
	public void setUp() throws Exception
	{

		World world = new World(worldSize,0,0);

		int value = 100;
		
		worldSettings.setWorldSize(1024);
		
		predatorAgentSettings.setInitalNumbers(agentPredatorNumbers);
		
		predatorAgentSettings.setSpeed(1);

		predatorAgentSettings.setViewRange(2);

		predatorAgentSettings.setDigestiveEfficiency(3);

		predatorAgentSettings.setREDiv(4);

		predatorAgentSettings.setMoveCost(5);

		predatorAgentSettings.setHungerThres(6);

		predatorAgentSettings.setConsumptionRate(7);

		predatorAgentSettings.setReproductionCost(8);

		predatorAgentSettings.setStartingEnergy(9);
		
		preyAgentSettings.setInitalNumbers(agentPreyNumbers);
		
		preyAgentSettings.setSpeed(1);

		preyAgentSettings.setViewRange(2);

		preyAgentSettings.setDigestiveEfficiency(3);

		preyAgentSettings.setREDiv(4);

		preyAgentSettings.setMoveCost(5);

		preyAgentSettings.setHungerThres(6);

		preyAgentSettings.setConsumptionRate(7);

		preyAgentSettings.setReproductionCost(8);

		preyAgentSettings.setStartingEnergy(9);		

		agentManger = new SimpleAgentManager(world,barrierManager, predatorAgentSettings, preyAgentSettings);

	}

	/*
	 * Tests if the inital prey is correctly set.
	 */
	@Test
	public void initialPreyNumbers()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - initialPreyNumbers");
		System.out.println("----------------------------------------------------");
		System.out.println("initialPreyNumbers : " + agentManger.getPreyCount() + " Should be : " + agentPreyNumbers);

		assertEquals(true, agentPreyNumbers == agentManger.getPreyCount());
	}

	/*
	 * Tests if the inital predators are correctly set.
	 */
	@Test
	public void initialPredatorNumbers()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - initialPredatorNumbers");
		System.out.println("----------------------------------------------------");
		System.out.println("initialPredatorNumbers : " + agentManger.getPredatorCount() + " Should be : " + agentPredatorNumbers);
		assertEquals(true, agentPredatorNumbers == agentManger.getPredatorCount());
	}

	/*
	 * Tests that the count of all agents is correct.
	 */
	@Test
	public void initialTotalAgentNumbers()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - initialTotalAgentNumbers");
		System.out.println("----------------------------------------------------");
		System.out.println("initialTotalAgentNumbers : " + agentManger.getAgentCount() + " Should be : " + (agentPreyNumbers + agentPredatorNumbers));
		assertEquals(true, agentPreyNumbers + agentPredatorNumbers == agentManger.getAgentCount());
	}

}
