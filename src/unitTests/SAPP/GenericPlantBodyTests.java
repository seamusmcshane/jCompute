package unitTests.SAPP;

import static org.junit.Assert.*;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Scenario.SAPP.Plant.GenericPlantBody;

import org.junit.Before;
import org.junit.Test;

public class GenericPlantBodyTests
{
	GenericPlantBody plant;

	A2DVector2f pos = new A2DVector2f(0, 0);
	float startingEnergy = 50f;
	float maxEnergy = 100f;
	float absorptionRate = 10f;
	float basePlantReproductionCost = 50f;

	@Before
	public void setUp() throws Exception
	{
		plant = new GenericPlantBody(pos, startingEnergy, maxEnergy, absorptionRate, basePlantReproductionCost);
	}

	@Test
	public void initialPositionTest()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - initialPositionTest");
		System.out.println("----------------------------------------------------");
		System.out.println("plant pos : " + plant.getBodyPos().toString() + " Vector should be:" + pos.toString());
		assertEquals(true, plant.getBodyPos().equals(pos));
	}
	
}
