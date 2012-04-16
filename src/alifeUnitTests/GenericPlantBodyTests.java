package alifeUnitTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.newdawn.slick.geom.Vector2f;

import alife.GenericPlantBody;

public class GenericPlantBodyTests
{
	GenericPlantBody plant;
	
	Vector2f pos = new Vector2f(0,0);
	float starting_energy=50f;
	float max_energy=100f;
	float absorption_rate=10f;
	float base_plant_reproduction_cost=50f;
	
	@Before
	public void setUp() throws Exception
	{
		plant = new GenericPlantBody(pos,starting_energy,max_energy,absorption_rate,base_plant_reproduction_cost);
	}

	@Test
	public void initialPositionTest()
	{
		assertEquals(true,plant.getBodyPos().equals(pos));
	}
	
	@Test
	public void trueSizeTest()
	{
		/* ASSUMES default size is 1 and true size is the bounding circles squared diameter */
		assertEquals(true,plant.getTrueSizeSQRD() == 0.99999994f);
	}

}
