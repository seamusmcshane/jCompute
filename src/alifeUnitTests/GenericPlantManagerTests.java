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
	
	int world_size = 1024;	
	int plant_regen_rate = 8;
	int plantstartingenergy = 100;
	int plant_energy_absorption_rate = 10;
	
	int inital_number = 100;
	
	@Before
	public void setUp() throws Exception
	{
		plantManager = new GenericPlantManager(barrierManager,world_size,inital_number, plant_regen_rate, plantstartingenergy, plant_energy_absorption_rate);
	}

	/*
	 * Tests that the inital plant numbers are correctly set.
	 */
	@Test
	public void intialPlantNumbersTest()
	{		
		assertEquals(inital_number,inital_number,plantManager.getPlantNo());
	}

}
