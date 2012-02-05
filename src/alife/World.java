package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

/*
 * World Class.
 * This class contains the generator and draw methods for the world.
 * It also contains boundary checks that are used to enforce world movement limits.
 */

public class World
{

	WorldGrid grid;
	Rectangle world_bound;

	private static int world_size;
	
	@SuppressWarnings("static-access")
	public World(int size)
	{
		this.world_size = size;
		
		world_bound = new Rectangle(0,0,world_size+1,world_size+1);
		
		createGrid();
	}

	/* Checks if the Coordinate is a World Boundary - Called by agent body */
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
	
	/* This this the left or right of the world */
	private static boolean checkXBoundary(float x)
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

	/* Is this the top or bottom of this world */
	private static boolean checkYBoundary(float y)
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
	
	/* Generate the world grid object - TODO add select step size */
	private void createGrid()
	{
		int grid_steps=10;
		grid = new WorldGrid(world_size,grid_steps);
	}
	
	/* Draw method */
	public void drawWorld(Graphics g)
	{
		g.translate(-world_size/2, -world_size/2);

		grid.drawGrid(g);
		
		g.setColor(Color.blue);
		
		g.draw(world_bound);
	}	
}
