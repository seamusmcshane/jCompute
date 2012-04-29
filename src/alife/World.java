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
	
	private static Rectangle[] barriers;
	
	private static int barrierMode=0;
	
	private static int barrierScenario=0;
	
	private float barrierThickness = 0.10f;

	/**
	 * Constructor for World.
	 * @param size int
	 * @param barrierMode int
	 * @param barrierScenario int
	 */
	@SuppressWarnings("static-access")
	public World(int size, int barrierMode,int barrierScenario)
	{
		this.worldSize = size;

		worldBound = new Rectangle(0, 0, worldSize + 1, worldSize + 1);
		
		this.barrierMode = barrierMode;
				
		/* if barriers are enabled */
		if(this.barrierMode>0)
		{
			this.barrierScenario = barrierScenario;
			
			setUpBarriers();		
		}
		
		createGrid();
	}

	private void setUpBarriers()
	{

		barriers = new Rectangle[barrierScenario+1];
		
		switch(barrierScenario)
		{
			case 0:
				if(barrierMode==1)
				{
					barriers[0] = new Rectangle(worldSize*0.25f,worldSize*0.45f,worldSize*0.50f,worldSize*barrierThickness);
				}
				else
				{
					barriers[0] = new Rectangle(0,worldSize*0.45f,worldSize*0.90f,worldSize*barrierThickness);					
				}
			break;
			case 1:				
				if(barrierMode==1)
				{
					barriers[0] = new Rectangle(worldSize*0.25f,worldSize*0.15f,worldSize*0.50f,worldSize*barrierThickness);
					barriers[1] = new Rectangle(worldSize*0.25f,worldSize*0.65f,worldSize*0.50f,worldSize*barrierThickness);
				}
				else
				{
					barriers[0] = new Rectangle(0,worldSize*0.15f,worldSize*0.90f,worldSize*barrierThickness);
					barriers[1] = new Rectangle(0,worldSize*0.65f,worldSize*0.90f,worldSize*barrierThickness);				
				}				
			break;
			case 2:
				if(barrierMode==1)
				{
					barriers[0] = new Rectangle(worldSize*0.25f,worldSize*0.15f,worldSize*0.50f,worldSize*barrierThickness);
					barriers[1] = new Rectangle(worldSize*0.25f,worldSize*0.45f,worldSize*0.50f,worldSize*barrierThickness);
					barriers[2] = new Rectangle(worldSize*0.25f,worldSize*0.70f,worldSize*0.50f,worldSize*barrierThickness);
				}
				else
				{
					barriers[0] = new Rectangle(0,worldSize*0.15f,worldSize*0.90f,worldSize*barrierThickness);
					barriers[1] = new Rectangle(worldSize*0.20f,worldSize*0.45f,worldSize*0.80f,worldSize*barrierThickness);
					barriers[2] = new Rectangle(0,worldSize*0.75f,worldSize*0.90f,worldSize*barrierThickness);		
				}				
			break;
		}
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

		/* if Barriers are enabled */
		if(barrierMode>0)
		{
			/* Check Barriers */
			if(isBarrierWall(x,y))
			{
				return true;
			}			
		}
		
		return false;

	}

	/**
	 * Method isBarrierWall.
	 * @param x float
	 * @param y float
	 * @return boolean
	 */
	private static boolean isBarrierWall(float x, float y)
	{
		
		for(int i=0;i<(barrierScenario+1);i++)
		{
			if(barriers[i].contains(x,y))
			{
				return true;
			}
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
		
		/* if Barriers are enabled */
		if(barrierMode>0)
		{
			for(int i=0;i<(barrierScenario+1);i++)
			{
				g.draw(barriers[i]);	
			}		
		}

	}
}
