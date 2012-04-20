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
 */
public class GenericPlantManager
{

	/** Plant Action Linked Lists */
	private LinkedList<GenericPlant> doList;	
	private LinkedList<GenericPlant> doneList;
	
	/** The Total number of plants managed by this class */
	private int plantCount=0;

	/** The interator used to draw the plants */
	private ListIterator<GenericPlant> itrDrawPlant;
	
	/** A re-used reference in the draw method */
	private GenericPlant tPlantDraw; 	
	
	/** The size of the world, needed for correctly placing new plants */
	private int world_size;
	
	/** The initial Number of plants */
	@SuppressWarnings("unused")
	private int inital_number;
	
	/** The reproduction cost for plants */
	private float base_plant_reproduction_cost=0.99f; // Disabled
	
	/** A reference to the plant absorption rate, so new plants can have the same value */
	private float base_plant_energy_absorption_rate=1f;
	
	/** The default value for the plants starting energy */
	private float plantstartingenergy;
	
	/** The amount of plants that are to be regenerated each step */
	private int plant_regen_rate;
		
	/** Reference for setting task in the */
	BarrierManager barrierManager;
	
	/**
	 * Creates a plant manager.
	 * 
	 * @param viewGenerator
	 * @param world_size
	 * @param inital_number
	 * @param plant_regen_rate
	 * @param plantstartingenergy
	 * @param plant_energy_absorption_rate
	 */
	public GenericPlantManager(BarrierManager barrierManager,int world_size,int inital_number, int plant_regen_rate, int plantstartingenergy, int plant_energy_absorption_rate)
	{		
		this.inital_number = inital_number;
		
		this.barrierManager = barrierManager;
		
		this.world_size = world_size;
				
		this.plant_regen_rate = plant_regen_rate;
		
		this.plantstartingenergy = plantstartingenergy;
		
		this.base_plant_energy_absorption_rate = plant_energy_absorption_rate;
		
		setUpLists();
		
		addPlants(world_size,inital_number);		
	}

	/** Draws all the plants with a toggle for body type */
	public void drawPlants(Graphics g,boolean true_drawing)
	{
		
		itrDrawPlant = doneList.listIterator();

		while (itrDrawPlant.hasNext())
		{
			
			tPlantDraw = itrDrawPlant.next();
						
			/* Optimization - Only draw visible plants that are inside the camera_boundaries */
			if (tPlantDraw.body.getBodyPos().getX() > (SimulationView.camera_bound.getX() - SimulationView.global_translate.getX()) && tPlantDraw.body.getBodyPos().getX() < (SimulationView.camera_bound.getMaxX() - SimulationView.global_translate.getX()) && tPlantDraw.body.getBodyPos().getY() > (SimulationView.camera_bound.getY() - SimulationView.global_translate.getY()) && tPlantDraw.body.getBodyPos().getY() < (SimulationView.camera_bound.getMaxY() - SimulationView.global_translate.getY()))
			{
				/* Optimization - draw correct circular bodies or faster rectangular bodies */
				if(true_drawing==true)
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
	
	/** Plant List preparation for the barrier */
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
		addPlants(world_size,plant_regen_rate);	// log2(512) - +9... log2(1024)+10...
		
		// Stats Counter
		StatsPanel.setPlantNo(plantCount);
		
	}
		
	/** Randomize the doList - to reduce list position bias */
	private void randomizeListOrder()
	{
		Collections.shuffle(doList);
	}
	
	/** Updates the Done list. 
	 * This is effectively handling the death of plants in the simulation and if later implemented the reproduction of plants. 
	 * */
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
					
					int x = rPos.nextInt(world_size) + 1;
					int y = rPos.nextInt(world_size) + 1;
										
					addNewPlant(new GenericPlant(x,y,50f, 100f, base_plant_energy_absorption_rate,base_plant_reproduction_cost));										
					
				}*/
				//
				
				/** Plant is not dead add it to the done list */
				doneList.add(temp);
				plantCount++;
				
			}
			
		}
	
	}
	
	/** Adds (n) number of plants randomly in the world */
	private void addPlants(int world_size,int plant_number)
	{	
		/* Random Starting Position */
		Random xr = new Random();
		Random yr = new Random();
		
		int x, y;
		
		for (int i = 0; i < plant_number; i++)
		{
			x = xr.nextInt(world_size) + 1;
			y = yr.nextInt(world_size) + 1;

			addNewPlant(new GenericPlant(x,y,plantstartingenergy, 100f, base_plant_energy_absorption_rate,base_plant_reproduction_cost));
		}		
	}

	/** Being born counts as an Action thus all new plants start in the done list */
	private void addNewPlant(GenericPlant plant)
	{		
		doneList.add(plant);
		plantCount++;		
	}	
	
	/** Sets up the safe starting position for the lists 
	 * Any plant not moved out of the done list has been marked as dead and will not be in the do list.
	 * */
	private void setUpLists()
	{
		doList = doneList;
		doneList = new LinkedList<GenericPlant>();
	}
	
	/**
	 * Added for Unit tests
	 * @return
	 */
	public int getPlantNo()
	{
		return plantCount;
	}
			
}
