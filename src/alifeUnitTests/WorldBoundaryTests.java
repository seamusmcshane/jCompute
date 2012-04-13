package alifeUnitTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import alife.World;
/**
 * This is the JUnit Test for the world class.
 */

public class WorldBoundaryTests
{
	World world;
	int world_size=512;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		
	}

	@Before
	public void setUp() throws Exception
	{
		world = new World(world_size);
	}

	@Test
	public void topLeftCornerBoundary()
	{				
		assertEquals(true,world.isBoundaryWall(0, 0));
	}
	
	@Test
	public void topRightCornerBoundary()
	{			
		assertEquals(true,world.isBoundaryWall(world_size, 0));
	}
	
	@Test
	public void bottomLeftCornerBoundary()
	{		
		assertEquals(true,world.isBoundaryWall(0, world_size));
	}
	
	@Test
	public void bottomRightCornerBoundary()
	{				
		assertEquals(true,world.isBoundaryWall(world_size,world_size));
	}	
	
	@Test
	public void middleOfWorld()
	{				
		assertEquals(false,world.isBoundaryWall(world_size/2,world_size/2));
	}	
	
	@Test
	public void outSideLowestBoundary()
	{				
		assertEquals(true,world.isBoundaryWall(-1,-1));
	}	
	
	@Test
	public void outSideHighestBoundary()
	{				
		assertEquals(true,world.isBoundaryWall(world_size+1,world_size+1));
	}	

}
