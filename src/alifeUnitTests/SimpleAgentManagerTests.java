package alifeUnitTests;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import alife.BarrierManager;
import alife.SimpleAgentManagementSetupParam;
import alife.SimpleAgentManager;
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

	SimpleAgentManagementSetupParam agentSettings= new SimpleAgentManagementSetupParam();
	
	int agentPreyNumbers=512;
	int agentPredatorNumbers=256;
		
	@Before
	public void setUp() throws Exception
	{

		int value = 100;

		agentSettings.setPredatorSpeed(value);

		agentSettings.setPreySpeed(value);

		agentSettings.setPredatorViewRange(value);

		agentSettings.setPreyViewRange(value);

		agentSettings.setPredatorDE(value);

		agentSettings.setPreyDE(value);

		agentSettings.setPredatorREDiv(value);

		agentSettings.setPreyREDiv(value);

		agentSettings.setPredatorMoveCost(value);

		agentSettings.setPreyMoveCost(value);

		agentSettings.setPredatorHungerThres(value);

		agentSettings.setPreyHungerThres(value);

		agentSettings.setPredatorConsumptionRate(value);

		agentSettings.setPreyConsumptionRate(value);

		agentSettings.setPredRepoCost(value);
		
		agentSettings.setPreyRepoCost(value);

		agentSettings.setPreyStartingEnergy(value);
		
		agentManger = new SimpleAgentManager(barrierManager,worldSize, agentPreyNumbers,agentPredatorNumbers,agentSettings);

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

		assertEquals(true,agentPreyNumbers == agentManger.getPreyCount());
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
		assertEquals(true,agentPredatorNumbers == agentManger.getPredatorCount());
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
		System.out.println("initialTotalAgentNumbers : " + agentManger.getAgentCount() + " Should be : " + (agentPreyNumbers+agentPredatorNumbers));	
		assertEquals(true, agentPreyNumbers+agentPredatorNumbers == agentManger.getAgentCount());		
	}

}
