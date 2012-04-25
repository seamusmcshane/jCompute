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
	int worldSize=512;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		
	}

	@Before
	public void setUp() throws Exception
	{
		world = new World(worldSize);
		System.out.println("=====================================================");		

	}

	@Test
	public void topLeftCornerBoundary()
	{			
		System.out.println("Test - topLeftCornerBoundary");
		System.out.println("----------------------------------------------------");		
		System.out.println("topLeftCornerBoundary : " + world.isBoundaryWall(0, 0) + " should be: true");		
		assertEquals(true,world.isBoundaryWall(0, 0));
	}
	
	@Test
	public void topRightCornerBoundary()
	{		
		System.out.println("Test - topRightCornerBoundary");
		System.out.println("----------------------------------------------------");		
		System.out.println("topRightCornerBoundary : " + world.isBoundaryWall(worldSize, 0) + " should be: true");			
		assertEquals(true,world.isBoundaryWall(worldSize, 0));
		System.out.println("=====================================================");		

	}
	
	@Test
	public void bottomLeftCornerBoundary()
	{	
		System.out.println("Test - bottomLeftCornerBoundary");
		System.out.println("----------------------------------------------------");		
		System.out.println("bottomLeftCornerBoundary : " + world.isBoundaryWall(0, worldSize) + " should be: true");			
		assertEquals(true,world.isBoundaryWall(0, worldSize));
	}
	
	@Test
	public void bottomRightCornerBoundary()
	{				
		System.out.println("Test - bottomRightCornerBoundary");
		System.out.println("----------------------------------------------------");		
		System.out.println("bottomRightCornerBoundary : " + world.isBoundaryWall(worldSize,worldSize) + " should be: true");	
		assertEquals(true,world.isBoundaryWall(worldSize,worldSize));
	}	
	
	@Test
	public void middleOfWorld()
	{				
		System.out.println("Test - middleOfWorld");
		System.out.println("----------------------------------------------------");		
		System.out.println("middleOfWorld : " + world.isBoundaryWall(worldSize/2,worldSize/2) + " should be: false");	
		assertEquals(false,world.isBoundaryWall(worldSize/2,worldSize/2));
	}	
	
	@Test
	public void outSideLowestBoundary()
	{				
		System.out.println("Test - outSideLowestBoundary");
		System.out.println("----------------------------------------------------");		
		System.out.println("outSideLowestBoundary : " + world.isBoundaryWall(-1,-1) + " should be: true");	
		assertEquals(true,world.isBoundaryWall(-1,-1));
	}	
	
	@Test
	public void outSideHighestBoundary()
	{				
		System.out.println("Test - outSideHighestBoundary");
		System.out.println("----------------------------------------------------");		
		System.out.println("outSideHighestBoundary : " + world.isBoundaryWall(worldSize+1,worldSize+1) + " should be: true");	
		assertEquals(true,world.isBoundaryWall(worldSize+1,worldSize+1));
	}	

}
