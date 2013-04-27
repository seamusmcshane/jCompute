package alifeUnitTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import alifeSim.Alife.GenericPlant.GenericPlantManager;
import alifeSim.Alife.GenericPlant.GenericPlantSetupSettings;
import alifeSim.Simulation.BarrierManager;
import alifeSim.World.World;
import alifeSim.World.WorldSetupSettings;

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

	GenericPlantSetupSettings plantSettings;
	
	World world;
	WorldSetupSettings worldSettings;
	
	int worldSize = 1024;
	
	int plantRegenRate = 8;
	int plantStartingEnergy = 100;
	int plantEnergyAbsorptionRate = 10;

	int initalNumber = 100;

	@Before
	public void setUp() throws Exception
	{
		worldSettings = new WorldSetupSettings();
		worldSettings.setWorldSize(worldSize);
		
		world = new World(worldSettings.getWorldSize(), worldSettings.getBarrierMode(), worldSettings.getBarrierScenario());
		
		plantSettings= new GenericPlantSetupSettings();
		
		plantSettings.setInitialPlantNumbers(initalNumber);
		plantSettings.setPlantEnergyAbsorptionRate(plantEnergyAbsorptionRate);
		plantSettings.setPlantRegenRate(plantRegenRate);
		plantSettings.setPlantStartingEnergy(plantStartingEnergy);
				
		System.out.println("Create a plant manager.");
		plantManager = new GenericPlantManager(barrierManager, plantSettings, worldSettings);
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
