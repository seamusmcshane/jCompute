package alifeSim.World;

import java.util.ArrayList;
import alifeSim.Gui.View.GUISimulationView;
import alifeSimGeom.A2DRectangle;
import alifeSimGeom.A2RGBA;

/**
 * World Class.
 * This class contains the generator and draw methods for the world.
 * It also contains boundary checks that are used to enforce world movement limits.
 * @author Seamus McShane
 */
public class World implements WorldInf
{
	/* The world grid background */
	private WorldGrid grid;

	/** The world boundary */
	private A2DRectangle worldBound;

	/** The size of the world */
	private int worldSize;

	/** Holds the barriers */
	private ArrayList<A2DRectangle> barriers;
	
	private A2RGBA barrierColor;
	
	/**
	 * Constructor for World.
	 * @param size int
	 * @param barrierMode int
	 * @param barrierScenario int
	 */
	public World(int size, int barrierNum,int barrierScenario)
	{
		barrierColor = new A2RGBA(0,0,1,0);
		
		this.worldSize = size;

		worldBound = new A2DRectangle(0, 0, worldSize, worldSize,barrierColor);
		
		setUpBarriers(barrierNum,barrierScenario);
		
		createGrid();
	}

	private void setUpBarriers(int barrierNum, int barrierScenario)
	{
		float barrierThickness = worldSize*0.10f;

		barriers = new ArrayList<A2DRectangle>(barrierNum);
		
		int barrierSetting = barrierNum-1;
		
		/*
		 * 	Default is scenario is the first in each case if we get sent anything invalid.
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
						
						barriers.add( new A2DRectangle(worldSize*0.25f,worldSize/2-(barrierThickness/2),worldSize*0.50f,barrierThickness,barrierColor) );
						
						break;
					case 1:
						
						barriers.add( new A2DRectangle(0,worldSize/2-(barrierThickness/2),worldSize*0.90f,barrierThickness,barrierColor) );

						break;
					default:
						System.out.println("Selected " + barrierNum + " Barriers but with Invalid Barrier Scenario : " + barrierScenario);
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
						barriers.add( new A2DRectangle(worldSize*0.25f,worldSize*0.25f-(barrierThickness/2),worldSize*0.50f,barrierThickness,barrierColor) );
						barriers.add( new A2DRectangle(worldSize*0.25f,worldSize*0.75f-(barrierThickness/2),worldSize*0.50f,barrierThickness) );						
					break;
					case 1:
						barriers.add( new A2DRectangle(0,worldSize*0.25f-(barrierThickness/2),worldSize*0.90f,barrierThickness,barrierColor) );
						barriers.add( new A2DRectangle(0,worldSize*0.75f-(barrierThickness/2),worldSize*0.90f,barrierThickness,barrierColor) );						
					break;
					default:
						System.out.println("Selected " + barrierNum + " Barriers but with Invalid Barrier Scenario : " + barrierScenario);						
					break;
				}
		
			break;
			case 2:
				switch(barrierScenario)
				{
					case 0:
						barriers.add( new A2DRectangle(worldSize*0.25f,worldSize*0.16f*1-(barrierThickness/2),worldSize*0.50f,barrierThickness,barrierColor) );
						barriers.add( new A2DRectangle(worldSize*0.25f,worldSize*0.16f*3-(barrierThickness/2),worldSize*0.50f,barrierThickness,barrierColor) );	
						barriers.add( new A2DRectangle(worldSize*0.25f,worldSize*0.16f*5-(barrierThickness/2),worldSize*0.50f,barrierThickness,barrierColor) );	
											
					break;
					case 1:
						barriers.add( new A2DRectangle(0,worldSize*0.16f*1-(barrierThickness/2),worldSize*0.90f,barrierThickness,barrierColor) );
						barriers.add( new A2DRectangle(0,worldSize*0.16f*3-(barrierThickness/2),worldSize*0.90f,barrierThickness,barrierColor) );	
						barriers.add( new A2DRectangle(0,worldSize*0.16f*5-(barrierThickness/2),worldSize*0.90f,barrierThickness,barrierColor) );					
					break;
					case 2:
						barriers.add( new A2DRectangle(0,worldSize*0.16f*1-(barrierThickness/2),worldSize*0.90f,barrierThickness,barrierColor) );
						barriers.add( new A2DRectangle(worldSize*0.10f,worldSize*0.16f*3-(barrierThickness/2),worldSize*0.90f,barrierThickness,barrierColor) );
						barriers.add( new A2DRectangle(0,worldSize*0.16f*5-(barrierThickness/2),worldSize*0.90f,barrierThickness,barrierColor) );
						
					break;
					default:
						System.out.println("Selected " + barrierNum + " Barriers but with Invalid Barrier Scenario : " + barrierScenario);							
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
		/* Check Barriers */
		if(isBarrierWall(x,y))
		{
			return true;
		}
		
		return false;
	}
	
	/* Is this position a valid location in the world */
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
		for(A2DRectangle barrier : barriers)
		{
			if(barrier.contains(x,y))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 *  Generate the world grid object
	 */
	private void createGrid()
	{
		// 8 = grid step size
		grid = new WorldGrid(worldSize, 8);
	}

	/**
	 *  Draw method for the world 
	 * @param g Graphics
	 */
	public void drawWorld(GUISimulationView simView)
	{
		grid.draw(simView);
		
		for(A2DRectangle barrier : barriers)
		{
			simView.drawRectangle(barrier);
		}
		
		simView.drawRectangle(worldBound,2f);

	}
	
	@Override
	public int getWorldBoundingSquareSize()
	{
		return worldSize;
	}
	
}
