package alife;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.newdawn.slick.Graphics;

public class GenericPlantManager
{

	/* Plant Action Linked Lists */
	LinkedList<GenericPlant> doList;	
	LinkedList<GenericPlant> doneList;
	
	GenericPlant tPlantDraw; 
	
	int plantCount=0;

	private ListIterator<GenericPlant> itrDrawPlant;
	
	private boolean true_drawing = true;
	
	private int world_size;
	private int inital_number;
	
	private int plant_regen_rate=0;
	
	/* Reference for setting task */
	ViewGeneratorManager viewGenerator;
	
	private GenericPlantStats default_stats;	
	
	public GenericPlantManager(ViewGeneratorManager viewGenerator,int world_size,int inital_number, GenericPlantStats stats, int plant_regen_rate)
	{		
		this.inital_number = inital_number;
		
		this.viewGenerator = viewGenerator;
		
		this.world_size = world_size;
				
		this.default_stats = stats;
		
		this.plant_regen_rate = plant_regen_rate;
		
		setUpLists();
		
		addPlants(world_size,inital_number);		
	}

	/* Draws all the plants */
	public void drawPlants(Graphics g)
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
				
				temp.body.stats.increment();
				
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
		
		// Clone the stats so they are all unique (not the same reference)
		GenericPlantStats plant_stats = default_stats.clone();
						
		//GenericPlantStats(float starting_energy, float max_energy, float absorption_rate,String renergy_div, int base_reproduction_cost)		
		int x, y;
		
		for (int i = 0; i < plant_number; i++)
		{
			x = xr.nextInt(world_size) + 1;
			y = yr.nextInt(world_size) + 1;
			
			addNewPlant(new GenericPlant(x,y,plant_stats));
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

	public void setTrueDrawing(Boolean true_body_drawing)
	{
		this.true_drawing = true_body_drawing;		
	}
	
}
