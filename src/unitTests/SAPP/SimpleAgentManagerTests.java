package unitTests.SAPP;

import static org.junit.Assert.*;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentManager;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentSetupSettings;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentEnum.AgentType;
import jCompute.Scenario.SAPP.World.World;
import jCompute.Scenario.SAPP.World.WorldSetupSettings;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
/**
 * 
 * Simple Agent Manager Tests
 * Testing that the numbers of initial Agents requested are correctly set.
 *
 */
public class SimpleAgentManagerTests
{
	SimpleAgentManager agentManger;

	int worldSize = 1024;

	WorldSetupSettings worldSettings = new WorldSetupSettings();
	
	List<SimpleAgentSetupSettings> agentSettingsList = new ArrayList(2);
	SimpleAgentSetupSettings predatorAgentSettings = new SimpleAgentSetupSettings("TEST");
	SimpleAgentSetupSettings preyAgentSettings = new SimpleAgentSetupSettings("TEST");
	
	int agentPreyNumbers = 512;
	int agentPredatorNumbers = 256;

	@Before
	public void setUp() throws Exception
	{

		World world = new World(worldSize,0,0);

		int value = 100;
		
		worldSettings.setWorldSize(1024);
		
		predatorAgentSettings.setType(AgentType.PREDATOR);
		
		predatorAgentSettings.setColor(255, 0, 0);
		
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
		
		preyAgentSettings.setType(AgentType.PREY);
		
		preyAgentSettings.setColor(255, 0, 0);

		preyAgentSettings.setSpeed(1);

		preyAgentSettings.setViewRange(2);

		preyAgentSettings.setDigestiveEfficiency(3);

		preyAgentSettings.setREDiv(4);

		preyAgentSettings.setMoveCost(5);

		preyAgentSettings.setHungerThres(6);

		preyAgentSettings.setConsumptionRate(7);

		preyAgentSettings.setReproductionCost(8);

		preyAgentSettings.setStartingEnergy(9);		

		agentSettingsList.add(predatorAgentSettings);
		agentSettingsList.add(preyAgentSettings);
		
		agentManger = new SimpleAgentManager(world,agentSettingsList );

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