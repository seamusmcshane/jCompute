package alifeUnitTests;

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
	
	@Test
	public void notDead()
	{
		assertEquals(false,plant1.isDead());
	}

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
