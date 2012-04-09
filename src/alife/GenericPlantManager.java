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
	ViewGeneratorManager viewGenerator;
	
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
	public GenericPlantManager(ViewGeneratorManager viewGenerator,int world_size,int inital_number, int plant_regen_rate, int plantstartingenergy, int plant_energy_absorption_rate)
	{		
		this.inital_number = inital_number;
		
		this.viewGenerator = viewGenerator;
		
		this.world_size = world_size;
		
		setUpLists();
		
		this.plant_regen_rate = plant_regen_rate;
		
		this.plantstartingenergy = plantstartingenergy;
		
		this.base_plant_energy_absorption_rate = plant_energy_absorption_rate;
		
		addPlants(world_size,inital_number);		
	}

	/* Draws all the plants */
	public void drawPlants(Graphics g,boolean true_drawing)
	{
		
		itrDrawPlant = doneList.listIterator();

		while (itrDrawPlant.hasNext())
		{
			
			tPlantDraw = itrDrawPlant.next();
			
			/* Set the current status of the view drawing */
			//tPlantDraw.setViewDrawing(view_drawing);
			
			// Optimization - Only draw visible agents that are inside the camera_boundarie
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
	
	// List prepare
	public void stage1()
	{
		setUpLists();
		
		randomizeListOrder();		
	}
	
	// View update
	public void stage2()
	{
		viewGenerator.setBarrierPlantTask(doList,plantCount);
	}
	
	// List update
	public void stage3()
	{
		updateDoneList();
				
		/* Plant Growth per Step - adds this many plants per step */
		addPlants(world_size,plant_regen_rate);	// log2(512) - +9... log2(1024)+10...
		
		
		// Stats Counter
		StatsPanel.setPlantNo(plantCount);
		
	}
		
	/* Randomize the doList */
	private void randomizeListOrder()
	{
		Collections.shuffle(doList);
	}
	
	// Updates the Done list.
	public void updateDoneList()
	{
		/* Recount all the plants - since some will have died...*/
		plantCount=0;
		
		ListIterator<GenericPlant> itr = doList.listIterator();
		
		while (itr.hasNext())
		{

			/* Remove this Agent from the List */
			GenericPlant temp = itr.next();

			/* remove from the doList */
			itr.remove();
			
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
				
				doneList.add(temp);
				plantCount++;
				
			}
			
		}
	
	}
	
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

	/* Being born counts as an Action thus all new agents start in the done list */
	public void addNewPlant(GenericPlant plant)
	{		
		doneList.add(plant);
		plantCount++;		
	}	
	
	/* Sets up the safe starting position for the lists */
	private void setUpLists()
	{
		doList = doneList;
		doneList = new LinkedList<GenericPlant>();
	}
			
}
