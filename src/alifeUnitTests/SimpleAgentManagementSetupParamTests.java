package alifeUnitTests;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import alife.SimpleAgentManagementSetupParam;
/**
 * 
 * Simple Agent Management Setup Param Tests
 * This set of tests, ensures that the parameters can be correctly set and recovered from the 
 * SimpleAgentManagementSetupParam object.
 */
public class SimpleAgentManagementSetupParamTests
{

	SimpleAgentManagementSetupParam params;
	int maxValue = 1000;
	float value;

	/* Value Generator */
	Random r;

	@Before
	public void setUp() throws Exception
	{
		params = new SimpleAgentManagementSetupParam();
		r = new Random();
		value = r.nextInt(maxValue) + 1;
		System.out.println("====================================================");
		System.out.println("----------------------------------------------------");
		System.out.println("Random Value For test : " + value);
	}

	/* Setting and getting of speeds */
	@Test
	public void predatorSpeed()
	{
		params.setPredatorSpeed(value);
		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorSpeed");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorSpeed : " + params.getPredatorSpeed() + " Should be : " + value);

		assertEquals(true, params.getPredatorSpeed() == value);
	}

	@Test
	public void preySpeed()
	{

		params.setPreySpeed(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - preySpeed");
		System.out.println("----------------------------------------------------");
		System.out.println("preySpeed : " + params.getPreySpeed() + " Should be : " + value);

		assertEquals(true, params.getPreySpeed() == value);

	}

	/* Setting and getting of View range */

	@Test
	public void predatorViewRange()
	{
		params.setPredatorViewRange(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorViewRange");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorViewRange : " + params.getPredatorViewRange() + " Should be : " + value);
		assertEquals(true, params.getPredatorViewRange() == value);
	}

	@Test
	public void preyViewRange()
	{
		params.setPreyViewRange(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - preyViewRange");
		System.out.println("----------------------------------------------------");
		System.out.println("preyViewRange : " + params.getPreyViewRange() + " Should be : " + value);
		assertEquals(true, params.getPreyViewRange() == value);
	}

	/* Setting and getting of DE */
	@Test
	public void predatorDE()
	{
		params.setPredatorDE(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorDE");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorDE : " + params.getPredatorDE() + " Should be : " + value);
		assertEquals(true, params.getPredatorDE() == value);
	}

	@Test
	public void preyDE()
	{
		params.setPreyDE(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - preyDE");
		System.out.println("----------------------------------------------------");
		System.out.println("preyDE : " + params.getPreyDE() + " Should be : " + value);
		assertEquals(true, params.getPreyDE() == value);
		System.out.println("----------------------------------------------------");

	}

	/* Setting and getting of REDiv */
	@Test
	public void predatorREDiv()
	{
		params.setPredatorREDiv(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorREDiv");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorREDiv : " + params.getPredatorREDiv() + " Should be : " + value);
		assertEquals(true, params.getPredatorREDiv() == value);
	}

	@Test
	public void preyREDiv()
	{
		params.setPreyREDiv(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - preyREDiv");
		System.out.println("----------------------------------------------------");
		System.out.println("preyREDiv : " + params.getPreyREDiv() + " Should be : " + value);
		assertEquals(true, params.getPreyREDiv() == value);
	}

	/* Setting and getting of MoveCost */
	@Test
	public void predatorMoveCost()
	{
		params.setPredatorMoveCost(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorMoveCost");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorMoveCost : " + params.getPredatorMoveCost() + " Should be : " + value);
		assertEquals(true, params.getPredatorMoveCost() == value);
	}

	@Test
	public void preyMoveCost()
	{
		params.setPreyMoveCost(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - preyMoveCost");
		System.out.println("----------------------------------------------------");
		System.out.println("preyMoveCost : " + params.getPreyMoveCost() + " Should be : " + value);
		assertEquals(true, params.getPreyMoveCost() == value);
	}

	/* Setting and getting of HungerThres */
	@Test
	public void predatorHungerThres()
	{
		params.setPredatorHungerThres(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorHungerThres");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorHungerThres : " + params.getPredatorHungerThres() + " Should be : " + value);
		assertEquals(true, params.getPredatorHungerThres() == value);
	}

	@Test
	public void preyHungerThres()
	{
		params.setPreyHungerThres(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - preyHungerThres");
		System.out.println("----------------------------------------------------");
		System.out.println("preyHungerThres : " + params.getPreyHungerThres() + " Should be : " + value);
		assertEquals(true, params.getPreyHungerThres() == value);
	}

	/* Setting and getting of ConsumptionRate */
	@Test
	public void predatorConsumptionRate()
	{
		params.setPredatorConsumptionRate(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorConsumptionRate");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorConsumptionRate : " + params.getPredatorConsumptionRate() + " Should be : " + value);
		assertEquals(true, params.getPredatorConsumptionRate() == value);
	}

	@Test
	public void preyConsumptionRate()
	{
		params.setPreyConsumptionRate(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - preyConsumptionRate");
		System.out.println("----------------------------------------------------");
		System.out.println("preyConsumptionRate : " + params.getPreyConsumptionRate() + " Should be : " + value);
		assertEquals(true, params.getPreyConsumptionRate() == value);
	}

	/* Setting and getting of RepoCost */
	@Test
	public void predatorRepoCost()
	{
		params.setPredRepoCost(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorRepoCost");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorRepoCost : " + params.getPredRepoCost() + " Should be : " + value);
		assertEquals(true, params.getPredRepoCost() == value);
	}

	@Test
	public void preyRepoCost()
	{
		params.setPreyRepoCost(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - preyRepoCost");
		System.out.println("----------------------------------------------------");
		System.out.println("preyRepoCost : " + params.getPreyRepoCost() + " Should be : " + value);
		assertEquals(true, params.getPreyRepoCost() == value);
	}

	/* Setting and getting of StartingEnergy */
	@Test
	public void predatorStartingEnergy()
	{
		params.setPredStartingEnergy(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorStartingEnergy");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorStartingEnergy : " + params.getPredStartingEnergy() + " Should be : " + value);
		assertEquals(true, params.getPredStartingEnergy() == value);
	}

	@Test
	public void preyStartingEnergy()
	{
		params.setPreyStartingEnergy(value);

		System.out.println("----------------------------------------------------");
		System.out.println("Test - preyStartingEnergy");
		System.out.println("----------------------------------------------------");
		System.out.println("preyStartingEnergy : " + params.getPreyStartingEnergy() + " Should be : " + value);
		assertEquals(true, params.getPreyStartingEnergy() == value);
	}

}
