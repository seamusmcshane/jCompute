package alife;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import org.newdawn.slick.Graphics;
/**
 * This class manages the plants in the simulation.
 * Drawing, adding, removing and regeneration.
 * 
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class GenericPlantManager
{

	/** Plant Action Linked Lists */
	private LinkedList<GenericPlant> doList;	
	private LinkedList<GenericPlant> doneList;
	
	/** The Total number of plants managed by this class */
	private int plantCount=0;

	/** The iterator used to draw the plants */
	private ListIterator<GenericPlant> itrDrawPlant;
	
	/** A re-used reference in the draw method */
	private GenericPlant tPlantDraw; 	
	
	/** The size of the world, needed for correctly placing new plants */
	private int worldSize;
	
	/** The initial Number of plants */
	@SuppressWarnings("unused")
	private int initalNumber;
	
	/** The reproduction cost for plants */
	private float basePlantReproductionCost=0.99f; // Disabled
	
	/** A reference to the plant absorption rate, so new plants can have the same value */
	private float basePlantEnergyAbsorptionRate=1f;
	
	/** The default value for the plants starting energy */
	private float plantStartingEnergy;
	
	/** The amount of plants that are to be regenerated each step */
	private int plantRegenRate;
		
	/** Reference for setting task in the */
	BarrierManager barrierManager;
	
	/**
	 * Creates a plant manager.
	 * 	
	 * @param worldSize
	 * @param initalNumber
	 * @param plantRegenRate
	 * @param plantStartingEnergy
	 * @param plantEnergyAbsorptionRate
	 * @param barrierManager BarrierManager
	 */
	public GenericPlantManager(BarrierManager barrierManager,int worldSize,int initalNumber, int plantRegenRate, int plantStartingEnergy, int plantEnergyAbsorptionRate)
	{		
		this.initalNumber = initalNumber;
		
		this.barrierManager = barrierManager;
		
		this.worldSize = worldSize;
				
		this.plantRegenRate = plantRegenRate;
		
		this.plantStartingEnergy = plantStartingEnergy;
		
		this.basePlantEnergyAbsorptionRate = plantEnergyAbsorptionRate;
		
		setUpLists();
		
		addPlants(worldSize,initalNumber);		
	}

	/** Draws all the plants with a toggle for body type
	 *  
	 * @param g Graphics	
	 * @param trueDrawing boolean
	 */
	public void drawPlants(Graphics g,boolean trueDrawing)
	{
		
		itrDrawPlant = doneList.listIterator();

		while (itrDrawPlant.hasNext())
		{
			
			tPlantDraw = itrDrawPlant.next();
						
			/* Optimization - Only draw visible plants that are inside the cameraBoundaries */
			if (tPlantDraw.body.getBodyPos().getX() > (SimulationView.cameraBound.getX() - SimulationView.globalTranslate.getX()) && tPlantDraw.body.getBodyPos().getX() < (SimulationView.cameraBound.getMaxX() - SimulationView.globalTranslate.getX()) && tPlantDraw.body.getBodyPos().getY() > (SimulationView.cameraBound.getY() - SimulationView.globalTranslate.getY()) && tPlantDraw.body.getBodyPos().getY() < (SimulationView.cameraBound.getMaxY() - SimulationView.globalTranslate.getY()))
			{
				/* Optimization - draw correct circular bodies or faster rectangular bodies */
				if(trueDrawing)
				{
					tPlantDraw.body.drawTrueBody(g);	
				}
				else
				{
					tPlantDraw.body.drawRectBody(g);						
				}
				
			}
			
		}
		
	}
	
	/** 
	 * Plant List preparation for the barrier
	 */
	public void stage1()
	{
		setUpLists();
		
		randomizeListOrder();		
	}
	
	/** Sets the barrier task for plants */
	public void stage2()
	{
		barrierManager.setBarrierPlantTask(doList,plantCount);
	}
	
	/** This stage performs the list updating, addition of new plants and stats updates. */
	public void stage3()
	{
		// The removal of dead plants
		updateDoneList();
				
		/* Plant Growth per Step - adds this many plants per step */
		addPlants(worldSize,plantRegenRate);	// log2(512) - +9... log2(1024)+10...
		
		// Stats Counter
		StatsPanel.setPlantNo(plantCount);
		
	}
		
	/** 
	 * Randomize the doList - to reduce list position bias
	 */
	private void randomizeListOrder()
	{
		Collections.shuffle(doList);
	}
	
	/** Updates the Done list. 
	 * This is effectively handling the death of plants in the simulation and if later implemented the reproduction of plants. 
	 */
	private void updateDoneList()
	{
		/* Recount all the plants - since some will have died...*/
		plantCount=0;
		
		ListIterator<GenericPlant> itr = doList.listIterator();
		
		while (itr.hasNext())
		{

			/* Remove this Plant from the List */
			GenericPlant temp = itr.next();

			/* remove from the doList */
			itr.remove();
			
			/** Is plant dead? */
			if(!temp.body.stats.isDead())
			{						
				
				/* This results in a unstable growth rate for plants - DISABLED */
				/*if(temp.body.stats.canReproduce())
				{										
					temp.body.stats.decrementReproductionCost();
					
					int x = rPos.nextInt(worldSize) + 1;
					int y = rPos.nextInt(worldSize) + 1;
										
					addNewPlant(new GenericPlant(x,y,50f, 100f, basePlantEnergyAbsorptionRate,basePlantReproductionCost));										
					
				}*/
				//
				
				/** Plant is not dead add it to the done list */
				doneList.add(temp);
				plantCount++;
				
			}
			
		}
	
	}
	
	/** 
	 * Adds (n) number of plants randomly in the world
	 * 
	 * @param worldSize int
	 * @param plantNumber int
	 */
	private void addPlants(int worldSize,int plantNumber)
	{	
		/* Random Starting Position */
		Random xr = new Random();
		Random yr = new Random();
		
		int x, y;
		
		for (int i = 0; i < plantNumber; i++)
		{
			x = xr.nextInt(worldSize) + 1;
			y = yr.nextInt(worldSize) + 1;

			addNewPlant(new GenericPlant(x,y,plantStartingEnergy, 100f, basePlantEnergyAbsorptionRate,basePlantReproductionCost));
		}		
	}

	/** 
	 * Being born counts as an Action thus all new plants start in the done list
	 *  
	 * @param plant GenericPlant
	 */
	private void addNewPlant(GenericPlant plant)
	{		
		doneList.add(plant);
		plantCount++;		
	}	
	
	/** 
	 * Sets up the safe starting position for the lists 
	 * Any plant not moved out of the done list has been marked as dead and will not be in the do list.
	 * */
	private void setUpLists()
	{
		doList = doneList;
		doneList = new LinkedList<GenericPlant>();
	}
	
	/**
	 * Added for Unit tests
	 * 	
	 * @return int */
	public int getPlantNo()
	{
		return plantCount;
	}
			
}
