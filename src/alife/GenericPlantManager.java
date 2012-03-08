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
	
	private boolean true_drawing = false;
	
	private int world_size;
	
	/* Reference for setting task */
	ViewGeneratorManager viewGenerator;
	
	public GenericPlantManager(ViewGeneratorManager viewGenerator,int world_size,int inital_number)
	{		
		
		this.viewGenerator = viewGenerator;
		
		this.world_size = world_size;
		
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
		int growth_num=updateDoneList();
		
		/* Plant Growth per Step - adds this many plants per step */
		addPlants(world_size,growth_num);		
	}
		
	/* Randomize the doList */
	private void randomizeListOrder()
	{
		Collections.shuffle(doList);
	}
	
	// Updates the Done list, calculates the cost and counts the of number of plants to add (reproduction)
	public int updateDoneList()
	{
		int growth_num=0;
		
		ListIterator<GenericPlant> itr = doList.listIterator();
		
		while (itr.hasNext())
		{

			/* Remove this Agent from the List */
			GenericPlant temp = itr.next();

			/* remove from the doList */
			itr.remove();
			
			if(!temp.body.stats.isDead())
			{
				temp.body.stats.increamentEnergy();
				
				if(temp.body.stats.getEnergy() > 90)
				{
					growth_num++;
					temp.body.stats.decrementEnergy(80);
				}
				
				doneList.add(temp);
			}
			
		}
		
		return growth_num;		
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

			addNewPlant(new GenericPlant(x,y,10, 100, 0.1f));
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
