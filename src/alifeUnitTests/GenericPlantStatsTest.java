package alifeUnitTests;
/**
 * Tests the GenericPlantStats class
 */
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import alife.GenericPlantStats;
import alife.SimpleAgentStats;

public class GenericPlantStatsTest
{
	float starting_energy=50f;
	float max_energy=100f;
	float absorption_rate=10f;
	float base_reproduction_cost=50f;
	
	GenericPlantStats plant1;
	
	@Before
	public void setUp() throws Exception
	{
		plant1 = new GenericPlantStats(starting_energy, max_energy, absorption_rate, base_reproduction_cost);
	}
	
	/*
	 * Tests the plant is alive at object creation.
	 */
	@Test
	public void notDead()
	{
		assertEquals(false,plant1.isDead());
	}

	/*
	 * Tests if the plants max energy is correctly being enforced.
	 */
	@Test 
	public void plantMaxEnergyCorrect()
	{
		/* Not dead */
		assertEquals(false,plant1.isDead());
		
		/* Deliberate designed to test overflow energy*/
		for(int i=0;i< (absorption_rate*max_energy) ;i++)
		{
			plant1.increment();
		}
		
		/* Energy should not over flow and must equal max_energy */
		assertEquals(max_energy,max_energy,plant1.getEnergy());
		
		/* Should also be alive */
		assertEquals(false,plant1.isDead());
	}

	
	/*
	 * Tests if the plant dies after all its energy is taken.
	 */
	@Test
	public void plantDiesAfterBeingAte()
	{
		/* Not Dead */
		assertEquals(false,plant1.isDead());
		
		/* Take almost all all the plants energy */
		plant1.decrementEnergy(starting_energy-1);	
		
		/* Not dead yet */
		assertEquals(false,plant1.isDead());
		
		/* Take the reset of the plants energy */
		plant1.decrementEnergy(1);		
		
		/* is Dead */
		assertEquals(true,plant1.isDead());
	}
	
	/*
	 * Initial Parameter setting tests 
	 */
	
	@Test
	public void energySetCorrect()
	{
		assertEquals(starting_energy,starting_energy,plant1.getEnergy());
	}
	
	@Test
	public void maxEnergySetCorrect()
	{
		assertEquals(max_energy,max_energy,plant1.getMaxEnergy());
	}
	
	@Test
	public void absorptionRateSetCorrect()
	{
		assertEquals(absorption_rate,absorption_rate,plant1.getAbsorptionRate());
	}

	@Test
	public void getBaseReproductionCost()
	{
		assertEquals(base_reproduction_cost,base_reproduction_cost,plant1.getBaseReproductionCost());
	}
	
}
