package alifeSim.World;

import java.awt.Color;

import alifeSim.Gui.NewSimView;
import alifeSimGeom.A2DRectangle;
import alifeSimGeom.A2RGBA;

/**
 * World Class.
 * This class contains the generator and draw methods for the world.
 * It also contains boundary checks that are used to enforce world movement limits.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class World implements WorldInf
{
	/* The world grid background */
	private WorldGrid grid;

	/** The world boundary */
	private A2DRectangle worldBound;

	/** The size of the world */
	private int worldSize;

	/** The Grid Steps */
	private int gridSteps = 8;
	
	private A2DRectangle[] barriers;
	
	private int barrierNum;
	
	private int barrierScenario;
	
	private float barrierThicknessMulti = 0.10f;

	private float barrierThickness;

	/**
	 * Constructor for World.
	 * @param size int
	 * @param barrierMode int
	 * @param barrierScenario int
	 */
	public World(int size, int barrierNum,int barrierScenario)
	{
		this.worldSize = size;

		worldBound = new A2DRectangle(0, 0, worldSize, worldSize);
		
		this.barrierNum = barrierNum;
				
		/* if barriers are enabled */
		if(this.barrierNum>0)
		{
			barrierThickness = worldSize*0.10f;
			
			this.barrierScenario = barrierScenario;
			
			setUpBarriers();		
		}
		
		createGrid();
	}

	private void setUpBarriers()
	{

		barriers = new A2DRectangle[barrierNum+1];
		
		int barrierSetting = barrierNum-1;
		
		/*
		 * 
		 * 	Default is scenario is the first in each case if we get sent anything invalid

		 */
		switch(barrierSetting)
		{
			case 0:			
				/* Single Barriers 
				 * 0 - One Barrier Centred
				 * 1 - One Barrier Left aligned with small channel on the right
				 */			
				switch(barrierScenario)
				{
					case 0:
						
						barriers[0] = new A2DRectangle(worldSize*0.25f,worldSize/2-(barrierThickness/2),worldSize*0.50f,barrierThickness);
						
						break;
					case 1:

						barriers[0] = new A2DRectangle(0,worldSize/2-(barrierThickness/2),worldSize*0.90f,barrierThickness);

						break;
					default:
						System.out.println("Selected " + barrierNum + " Barriers but with Invalid Barrier Scenario : " + barrierScenario);
						barriers[0] = new A2DRectangle(worldSize*0.25f,worldSize/2-(barrierThickness/2),worldSize*0.50f,barrierThickness);
					break;		
				}
								
			break;
			case 1:	
				/* Dual Barriers 
				 * 0 - Two Barrier Centred in each Hemisphere
				 * 1 - Two Barrier Left aligned with small channel on the right
				 */	
				switch(barrierScenario)
				{
					case 0:
						barriers[0] = new A2DRectangle(worldSize*0.25f,worldSize*0.25f-(barrierThickness/2),worldSize*0.50f,barrierThickness);
						barriers[1] = new A2DRectangle(worldSize*0.25f,worldSize*0.75f-(barrierThickness/2),worldSize*0.50f,barrierThickness);						
					break;
					case 1:
						barriers[0] = new A2DRectangle(0,worldSize*0.25f-(barrierThickness/2),worldSize*0.90f,barrierThickness);
						barriers[1] = new A2DRectangle(0,worldSize*0.75f-(barrierThickness/2),worldSize*0.90f,barrierThickness);						
					break;
					default:
						System.out.println("Selected " + barrierNum + " Barriers but with Invalid Barrier Scenario : " + barrierScenario);
						barriers[0] = new A2DRectangle(worldSize*0.25f,worldSize*0.25f-(barrierThickness/2),worldSize*0.50f,barrierThickness);
						barriers[1] = new A2DRectangle(worldSize*0.25f,worldSize*0.75f-(barrierThickness/2),worldSize*0.50f,barrierThickness);							
					break;
				}
		
			break;
			case 2:
				switch(barrierScenario)
				{
					case 0:
						barriers[0] = new A2DRectangle(worldSize*0.25f,worldSize*0.16f*1-(barrierThickness/2),worldSize*0.50f,barrierThickness);
						barriers[1] = new A2DRectangle(worldSize*0.25f,worldSize*0.16f*3-(barrierThickness/2),worldSize*0.50f,barrierThickness);	
						barriers[2] = new A2DRectangle(worldSize*0.25f,worldSize*0.16f*5-(barrierThickness/2),worldSize*0.50f,barrierThickness);	
											
					break;
					case 1:
						barriers[0] = new A2DRectangle(0,worldSize*0.16f*1-(barrierThickness/2),worldSize*0.90f,barrierThickness);
						barriers[1] = new A2DRectangle(0,worldSize*0.16f*3-(barrierThickness/2),worldSize*0.90f,barrierThickness);	
						barriers[2] = new A2DRectangle(0,worldSize*0.16f*5-(barrierThickness/2),worldSize*0.90f,barrierThickness);					
					break;
					case 2:
						barriers[0] = new A2DRectangle(0,worldSize*0.16f*1-(barrierThickness/2),worldSize*0.90f,barrierThickness);
						barriers[1] = new A2DRectangle(worldSize*0.10f,worldSize*0.16f*3-(barrierThickness/2),worldSize*0.90f,barrierThickness);	
						barriers[2] = new A2DRectangle(0,worldSize*0.16f*5-(barrierThickness/2),worldSize*0.90f,barrierThickness);								
					break;
					
					default:
						System.out.println("Selected " + barrierNum + " Barriers but with Invalid Barrier Scenario : " + barrierScenario);
						barriers[0] = new A2DRectangle(worldSize*0.25f,worldSize*0.16f*1-(barrierThickness/2),worldSize*0.50f,barrierThickness);
						barriers[1] = new A2DRectangle(worldSize*0.25f,worldSize*0.16f*3-(barrierThickness/2),worldSize*0.50f,barrierThickness);	
						barriers[2] = new A2DRectangle(worldSize*0.25f,worldSize*0.16f*5-(barrierThickness/2),worldSize*0.50f,barrierThickness);								
					break;
					
				}
			
			break;
		}
	}
	
	/**
	 * Checks if the Coordinate is a World Boundary
	 * @param x
	 * @param y
	 * @return boolean */
	private boolean isPointOutSideWorldBoundary(float x, float y)
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
	
	/* Does any of the barriers contain the point */
	private boolean doBarriersContain(float x,float y)
	{
		/* if Barriers are enabled */
		if(barrierNum>0)
		{
			/* Check Barriers */
			if(isBarrierWall(x,y))
			{
				return true;
			}			
		}
		
		return false;
	}
	
	/* Is this positon a valid location */
	@Override
	public boolean isInvalidPosition(float x, float y)
	{
		return (isPointOutSideWorldBoundary(x,y) ||  doBarriersContain(x,y));
	}

	@Override
	public boolean isValidPosition(float x, float y)
	{
		return !isInvalidPosition(x,y);
	}
	
	/**
	 * Method isBarrierWall.
	 * @param x float
	 * @param y float
	 * @return boolean
	 */
	private boolean isBarrierWall(float x, float y)
	{
		
		for(int i=0;i<(barrierNum);i++)
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
	private boolean checkXBoundary(float x)
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
	private boolean checkYBoundary(float y)
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
	public void drawWorld(NewSimView simView)
	{
		grid.draw(simView);
				
		/* if Barriers are enabled */
		if(barrierNum>0)
		{
			for(int i=0;i<(barrierNum);i++)
			{
				simView.drawRectangle(barriers[i],new A2RGBA(0,0,1f,0));
			}		
		}
				
		simView.drawRectangle(worldBound,new A2RGBA(0,0,1f,0),2f);

	}
	
	@Override
	public int getWorldBoundingSquareSize()
	{
		return worldSize;
	}
	
}
