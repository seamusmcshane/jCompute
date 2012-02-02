package alife;
import org.newdawn.slick.Graphics;


public class World
{

	WorldGrid grid;
		
	private static int world_size;
	
	@SuppressWarnings("static-access")
	public World(int size)
	{
		this.world_size = size;
		
		createGrid();
	}

	/* Checks if the Coordinate is a World Boundary */
	public static boolean isBondaryWall(float x,float y)
	{		
		/* Top */
		if(y<=0)
		{
			return true;
		}
		

		/* Bottom */
		if(y>=world_size)
		{
			return true;
		}
		
		/* Left */
		if(x<=0)
		{
			return true;
		}
		
		/* Right */
		if(x>=world_size)
		{
			return true;
		}
		
		return false;

	}
	
	public static boolean checkXBoundary(float x)
	{
		/* Left */
		if(x<=0)
		{
			return true;
		}
		
		/* Right */
		if(x>=world_size)
		{
			return true;
		}
		return false;
	}

	public static boolean checkYBoundary(float y)
	{
		/* Left */
		if(y<=0)
		{
			return true;
		}
		
		/* Right */
		if(y>=world_size)
		{
			return true;
		}
		return false;
	}
	
	private void createGrid()
	{
		int grid_steps=10;
		grid = new WorldGrid(world_size,grid_steps);
	}
	
	
	public void drawWorld(Graphics g)
	{
		grid.drawGrid(g);
	}
	
	
}
