package alifeUnitTests;
/**
 * Tests the GenericPlantStats class
 */
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import alife.GenericPlantStats;

public class GenericPlantStatsTest
{
	float startingEnergy = 50f;
	float maxEnergy = 100f;
	float absorptionRate = 10f;
	float baseReproductionCost = 50f;

	GenericPlantStats plant1;

	@Before
	public void setUp() throws Exception
	{
		plant1 = new GenericPlantStats(startingEnergy, maxEnergy, absorptionRate, baseReproductionCost);
	}

	/*
	 * Tests the plant is alive at object creation.
	 */
	@Test
	public void plantNotDead()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - plantNotDead");
		System.out.println("----------------------------------------------------");

		System.out.println("New Plant");
		System.out.println("Plant is dead : " + plant1.isDead() + " Should be : false");
		assertEquals(false, plant1.isDead());
	}

	/*
	 * Tests if the plants max energy is correctly being enforced.
	 */
	@Test
	public void plantMaxEnergyOverFlowTest()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - plantMaxEnergyOverFlowTest");
		System.out.println("----------------------------------------------------");

		/* Not dead */
		System.out.println("New Plant");
		System.out.println("Plant is dead : " + plant1.isDead() + " Should be : false");
		assertEquals(false, plant1.isDead());

		System.out.println("Plant Absorption Overflow Check");
		/* Deliberate designed to test overflow energy */
		for (int i = 0; i < (absorptionRate * maxEnergy); i++) // Will over flow by a good bit if not enforced
		{
			plant1.increment();
			System.out.println("Plant Energy : " + plant1.getEnergy());

		}

		/* Energy should not over flow and must equal maxEnergy */
		System.out.println("Final Plant Energy : " + plant1.getEnergy() + " Should be :" + maxEnergy);
		assertEquals(true, maxEnergy == plant1.getEnergy());

		/* Should also be alive */
		System.out.println("Plant is dead : " + plant1.isDead() + " Should be : false");
		assertEquals(false, plant1.isDead());
	}

	/*
	 * Tests if the plant dies after all its energy is taken.
	 */
	@Test
	public void plantDiesAfterBeingAte()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - plantDiesAfterBeingAte");
		System.out.println("----------------------------------------------------");
		/* Not Dead */
		System.out.println("Plant is dead : " + plant1.isDead() + " Should be : false");
		assertEquals(false, plant1.isDead());

		/* Take almost all all the plants energy */
		System.out.println("Plant Energy :" + plant1.getEnergy());
		plant1.decrementEnergy(startingEnergy - 1);
		System.out.println("Plant Energy Removed :" + (startingEnergy - 1));

		/* Not dead yet */
		System.out.println("Plant is dead : " + plant1.isDead() + " Should be : false");
		assertEquals(false, plant1.isDead());

		/* Take the reset of the plants energy */
		System.out.println("Plant Energy :" + plant1.getEnergy());
		plant1.decrementEnergy(1);
		System.out.println("Plant Energy Removed :" + (1));

		/* is Dead */
		System.out.println("Plant is dead : " + plant1.isDead() + " Should be : true");
		assertEquals(true, plant1.isDead());
	}

	/*
	 * Initial Parameter setting tests
	 */

	@Test
	public void startingEnergySetCorrect()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - startingEnergySetCorrect");
		System.out.println("----------------------------------------------------");
		System.out.println("startingEnergy :" + plant1.getEnergy() + " Should be : " + startingEnergy);
		assertEquals(true, startingEnergy == plant1.getEnergy());
	}

	@Test
	public void maxEnergySetCorrect()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - maxEnergySetCorrect");
		System.out.println("----------------------------------------------------");
		System.out.println("maxEnergy:" + plant1.getMaxEnergy() + " Should be : " + maxEnergy);
		assertEquals(true, maxEnergy == plant1.getMaxEnergy());
	}

	@Test
	public void absorptionRateSetCorrect()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - absorptionRateSetCorrect");
		System.out.println("----------------------------------------------------");
		System.out.println("absorptionRate :" + plant1.getAbsorptionRate() + " Should be : " + absorptionRate);
		assertEquals(true, absorptionRate == plant1.getAbsorptionRate());
	}

	@Test
	public void baseReproductionCostSetCorrect()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - baseReproductionCostSetCorrect");
		System.out.println("----------------------------------------------------");
		System.out.println("baseReproductionCost :" + plant1.getBaseReproductionCost() + " Should be : " + baseReproductionCost);
		assertEquals(true, baseReproductionCost == plant1.getBaseReproductionCost());
	}

}
