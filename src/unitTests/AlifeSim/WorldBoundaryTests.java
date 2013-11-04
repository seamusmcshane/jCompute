package unitTests.AlifeSim;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import alifeSim.World.World;
/**
 * This is the JUnit Test for the world class.
 */

public class WorldBoundaryTests
{
	World world;
	int worldSize = 512;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

	}

	@Before
	public void setUp() throws Exception
	{
		System.out.println("=====================================================");

	}

	@Test
	public void topLeftCornerBoundary()
	{
		world = new World(worldSize,0,0);
		System.out.println("Test - topLeftCornerBoundary");
		System.out.println("----------------------------------------------------");
		System.out.println("topLeftCornerBoundary : " + world.isInvalidPosition(0, 0) + " should be: true");
		assertEquals(true, world.isInvalidPosition(0, 0));
	}

	@Test
	public void topRightCornerBoundary()
	{
		world = new World(worldSize,0,0);
		System.out.println("Test - topRightCornerBoundary");
		System.out.println("----------------------------------------------------");
		System.out.println("topRightCornerBoundary : " + world.isInvalidPosition(worldSize, 0) + " should be: true");
		assertEquals(true, world.isInvalidPosition(worldSize, 0));
		System.out.println("=====================================================");

	}

	@Test
	public void bottomLeftCornerBoundary()
	{
		world = new World(worldSize,0,0);
		System.out.println("Test - bottomLeftCornerBoundary");
		System.out.println("----------------------------------------------------");
		System.out.println("bottomLeftCornerBoundary : " + world.isInvalidPosition(0, worldSize) + " should be: true");
		assertEquals(true, world.isInvalidPosition(0, worldSize));
	}

	@Test
	public void bottomRightCornerBoundary()
	{
		world = new World(worldSize,0,0);
		System.out.println("Test - bottomRightCornerBoundary");
		System.out.println("----------------------------------------------------");
		System.out.println("bottomRightCornerBoundary : " + world.isInvalidPosition(worldSize, worldSize) + " should be: true");
		assertEquals(true, world.isInvalidPosition(worldSize, worldSize));
	}

	@Test
	public void middleOfWorld()
	{
		world = new World(worldSize,0,0);
		System.out.println("Test - middleOfWorld");
		System.out.println("----------------------------------------------------");
		System.out.println("middleOfWorld : " + world.isInvalidPosition(worldSize / 2, worldSize / 2) + " should be: false");
		assertEquals(false, world.isInvalidPosition(worldSize / 2, worldSize / 2));
	}

	@Test
	public void outSideLowestBoundary()
	{
		world = new World(worldSize,0,0);
		System.out.println("Test - outSideLowestBoundary");
		System.out.println("----------------------------------------------------");
		System.out.println("outSideLowestBoundary : " + world.isInvalidPosition(-1, -1) + " should be: true");
		assertEquals(true, world.isInvalidPosition(-1, -1));
	}

	@Test
	public void outSideHighestBoundary()
	{
		world = new World(worldSize,0,0);
		System.out.println("Test - outSideHighestBoundary");
		System.out.println("----------------------------------------------------");
		System.out.println("outSideHighestBoundary : " + world.isInvalidPosition(worldSize + 1, worldSize + 1) + " should be: true");
		assertEquals(true, world.isInvalidPosition(worldSize + 1, worldSize + 1));
	}

}
