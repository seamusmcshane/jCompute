package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

/**
 * World Class.
 * This class contains the generator and draw methods for the world.
 * It also contains boundary checks that are used to enforce world movement limits.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class World
{
	/* The world grid background */
	WorldGrid grid;

	/** The world boundary */
	Rectangle worldBound;

	/** The size of the world */
	private static int worldSize;

	/** The Grid Steps */
	int gridSteps = 8;

	/**
	 * Constructor for World.
	 * @param size int
	 */
	@SuppressWarnings("static-access")
	public World(int size)
	{
		this.worldSize = size;

		worldBound = new Rectangle(0, 0, worldSize + 1, worldSize + 1);

		createGrid();
	}

	/**
	 * Checks if the Coordinate is a World Boundary - Called by agent body
	 * @param x
	 * @param y
	 * @return boolean */
	public static boolean isBoundaryWall(float x, float y)
	{
		/* Top */
		if (y <= 0)
		{
			return true;
		}

		/* Bottom */
		if (y >= worldSize)
		{
			return true;
		}

		/* Left */
		if (x <= 0)
		{
			return true;
		}

		/* Right */
		if (x >= worldSize)
		{
			return true;
		}

		return false;

	}

	/**
	 * Is this the left or right of the world.
	 * @param x
	 * @return boolean */
	private static boolean checkXBoundary(float x)
	{
		/* Left */
		if (x <= 0)
		{
			return true;
		}

		/* Right */
		if (x >= worldSize)
		{
			return true;
		}
		return false;
	}

	/**
	 * Is this the top or bottom of this world.
	 * @param y
	 * @return boolean */
	private static boolean checkYBoundary(float y)
	{
		/* Left */
		if (y <= 0)
		{
			return true;
		}

		/* Right */
		if (y >= worldSize)
		{
			return true;
		}
		return false;
	}

	/**
	 *  Generate the world grid object
	 */
	private void createGrid()
	{
		grid = new WorldGrid(worldSize, gridSteps);
	}

	/**
	 *  Draw method for the world 
	 * @param g Graphics
	 */
	public void drawWorld(Graphics g)
	{
		grid.drawGrid(g);

		g.setColor(Color.blue);

		g.draw(worldBound);

	}
}
