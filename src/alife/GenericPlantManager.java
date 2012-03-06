package alife;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.newdawn.slick.Graphics;

public class GenericPlantManager
{

	// Used to prevent dual Access to the done list - which would cause an exception
	Semaphore lock = new Semaphore(1,true);
	
	/* Plant Action Linked Lists */
	LinkedList<GenericPlant> doList;	
	LinkedList<GenericPlant> doneList;
	
	GenericPlant tPlantDraw; 
	
	int plantCount=0;

	private ListIterator<GenericPlant> itrDrawPlant;
	
	private boolean true_drawing = false;
	
	private int world_size;
	
	public GenericPlantManager(int world_size,int inital_number)
	{		
		this.world_size = world_size;
		
		setUpLists();
		
		addPlants(world_size,inital_number);		
	}

	/* Draws all the plants */
	public void drawPlants(Graphics g)
	{
		
		lock.acquireUninterruptibly();
		
		itrDrawPlant = doneList.listIterator();

		while (itrDrawPlant.hasNext())
		{

			tPlantDraw = itrDrawPlant.next();

			/* Set the current status of the view drawing */
			//tPlantDraw.setViewDrawing(view_drawing);
			
			// Optimization - Only draw visible agents that are inside the camera_boundarie
			//if (tPlantDraw.getPos().getX() > (View.camera_bound.getX() - View.global_translate.getX()) && tPlantDraw.getPos().getX() < (View.camera_bound.getMaxX() - View.global_translate.getX()) && tPlantDraw.getPos().getY() > (View.camera_bound.getY() - View.global_translate.getY()) && tPlantDraw.getPos().getY() < (View.camera_bound.getMaxY() - View.global_translate.getY()))
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
		
		lock.release();
		
	}
	
	/* Plant Update Loop */
	public void updatePlants()
	{
		lock.acquireUninterruptibly();
		
		setUpLists();
		
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
		
		/* Plant Growth per Step - adds this many plants per step */
		addPlants(world_size,growth_num);
		
		lock.release();
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
