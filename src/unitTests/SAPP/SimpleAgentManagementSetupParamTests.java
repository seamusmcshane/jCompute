package unitTests.SAPP;

import static org.junit.Assert.*;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentSetupSettings;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
/**
 * 
 * Simple Agent Management Setup Param Tests
 * This set of tests, ensures that the parameters can be correctly set and recovered from the 
 * SimpleAgentManagementSetupParam object.
 */
public class SimpleAgentManagementSetupParamTests
{

	SimpleAgentSetupSettings agentSettings;
	int maxValue = 1000;
	float value;

	/* Value Generator */
	Random r;

	@Before
	public void setUp() throws Exception
	{
		agentSettings = new SimpleAgentSetupSettings("TEST");
		r = new Random();
		value = r.nextInt(maxValue) + 1;
		System.out.println("====================================================");
		System.out.println("----------------------------------------------------");
		System.out.println("Random Value For test : " + value);
	}

	/* Setting and getting of speeds */
	@Test
	public void Speed()
	{
		agentSettings.setSpeed(value);
		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getSpeed() + " Should be : " + value);

		assertEquals(true, agentSettings.getSpeed() == value);
	}


	/* Setting and getting of View range */
	@Test
	public void ViewRange()
	{
		agentSettings.setViewRange(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getViewRange() + " Should be : " + value);
		assertEquals(true, agentSettings.getViewRange() == value);
	}

	/* Setting and getting of DE */
	@Test
	public void DigestiveEfficiency()
	{
		agentSettings.setDigestiveEfficiency(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getDigestiveEfficiency() + " Should be : " + value);
		assertEquals(true, agentSettings.getDigestiveEfficiency() == value);
	}

	/* Setting and getting of REDiv */
	@Test
	public void REDiv()
	{
		agentSettings.setREDiv(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getREDiv() + " Should be : " + value);
		assertEquals(true, agentSettings.getREDiv() == value);
	}

	/* Setting and getting of MoveCost */
	@Test
	public void MoveCost()
	{
		agentSettings.setMoveCost(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getMoveCost() + " Should be : " + value);
		assertEquals(true, agentSettings.getMoveCost() == value);
	}

	/* Setting and getting of HungerThres */
	@Test
	public void HungerThres()
	{
		agentSettings.setHungerThres(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getHungerThres() + " Should be : " + value);
		assertEquals(true, agentSettings.getHungerThres() == value);
	}

	@Test
	public void preyHungerThres()
	{
		agentSettings.setHungerThres(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getHungerThres() + " Should be : " + value);
		assertEquals(true, agentSettings.getHungerThres() == value);
	}

	/* Setting and getting of ConsumptionRate */
	@Test
	public void ConsumptionRate()
	{
		agentSettings.setConsumptionRate(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorConsumptionRate");
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getConsumptionRate() + " Should be : " + value);
		assertEquals(true, agentSettings.getConsumptionRate() == value);
	}

	/* Setting and getting of RepoCost */
	@Test
	public void ReproductionCost()
	{
		agentSettings.setReproductionCost(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getReproductionCost() + " Should be : " + value);
		assertEquals(true, agentSettings.getReproductionCost() == value);
	}

	/* Setting and getting of StartingEnergy */
	@Test
	public void StartingEnergy()
	{
		agentSettings.setStartingEnergy(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println((new Exception()).getStackTrace()[0].getMethodName() + " " + agentSettings.getStartingEnergy() + " Should be : " + value);
		assertEquals(true, agentSettings.getStartingEnergy() == value);
	}

}
