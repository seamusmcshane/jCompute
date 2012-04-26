package alifeUnitTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import alife.GenericPlantBody;

public class GenericPlantBodyTests
{
	GenericPlantBody plant;

	Vector2f pos = new Vector2f(0, 0);
	float startingEnergy = 50f;
	float maxEnergy = 100f;
	float absorptionRate = 10f;
	float base_plantReproductionCost = 50f;

	@Before
	public void setUp() throws Exception
	{
		plant = new GenericPlantBody(pos, startingEnergy, maxEnergy, absorptionRate, base_plantReproductionCost);
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

	@Test
	public void plantTrueSizeTest()
	{
		int size = 1; // This is the default size

		Rectangle body = new Rectangle(0, 0, size, size);
		float trueSize = body.getBoundingCircleRadius();

		// *2 for Diameter
		float trueSizeSQRD = (trueSize * trueSize) * 2;

		/* ASSUMES size is 1 as its hardcoded for now */
		System.out.println("----------------------------------------------------");
		System.out.println("Test - plantTrueSizeTest");
		System.out.println("----------------------------------------------------");
		System.out.println("Plant Bounding Circle Size Squared :" + plant.getTrueSizeSQRD() + " trueSize should be : " + trueSizeSQRD);
		assertEquals(true, plant.getTrueSizeSQRD() == trueSizeSQRD);
	}

}
