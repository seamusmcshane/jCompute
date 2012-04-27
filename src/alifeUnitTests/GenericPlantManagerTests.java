package alifeUnitTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import alife.BarrierManager;
import alife.GenericPlantManager;
/**
 * 
 * Generic Plant Manager Tests
 * Testing that the numbers of initial plants requested are correctly set.
 *
 */
public class GenericPlantManagerTests
{
	GenericPlantManager plantManager;

	BarrierManager barrierManager = null;

	int worldSize = 1024;
	int plantRegenRate = 8;
	int plantStartingEnergy = 100;
	int plantEnergyAbsorptionRate = 10;

	int initalNumber = 100;

	@Before
	public void setUp() throws Exception
	{
		System.out.println("Create a plant manager.");
		plantManager = new GenericPlantManager(barrierManager, worldSize, initalNumber, plantRegenRate, plantStartingEnergy, plantEnergyAbsorptionRate);
	}

	/*
	 * Tests that the inital plant numbers are correctly set.
	 */
	@Test
	public void intialPlantNumbersTest()
	{
		System.out.println("Inital Plant No : " + plantManager.getPlantNo() + " Should be : " + initalNumber);
		assertEquals(true, initalNumber == plantManager.getPlantNo());
	}

}
