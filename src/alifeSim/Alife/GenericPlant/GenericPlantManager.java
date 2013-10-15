package alifeSim.Alife.GenericPlant;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import org.newdawn.slick.Graphics;

import alifeSim.Gui.SimulationView;
import alifeSim.Gui.StatsPanel;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.BarrierManager;
import alifeSim.Simulation.SimulationStats;
import alifeSim.World.World;
import alifeSim.World.WorldSetupSettings;
import alifeSim.datastruct.list.ArrayList;
/**
 * This class manages the plants in the simulation.
 * Drawing, adding, removing and regeneration.
 * 
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("unused")
public class GenericPlantManager
{

	/** Plant Action Linked Lists */
	private ArrayList<GenericPlant> doList;
	private ArrayList<GenericPlant> doneList;
	
	/** The Total number of plants managed by this class */
	private int plantCount = 0;

	/** The iterator used to draw the plants */
	//private ListIterator<GenericPlant> itrDrawPlant;

	/** A re-used reference in the draw method */
	private GenericPlant tPlantDraw;

	/** The size of the world, needed for correctly placing new plants */
	private int worldSize;

	/** The initial Number of plants */
	private int initalNumber;

	/** The reproduction cost for plants */
	private float basePlantReproductionCost = 0.99f; // Disabled

	/** A reference to the plant absorption rate, so new plants can have the same value */
	private float basePlantEnergyAbsorptionRate = 1f;

	/** The default value for the plants starting energy */
	private float plantStartingEnergy;

	/** The amount of plants that are to be regenerated */
	private int plantRegenRate;
	
	/** Regern plants every n step */
	private int plantRegenerationNSteps;

	/** Reference for setting task in the */
	private BarrierManager barrierManager;

	
	/*
	 * Internal
	 */
	private int add_calls = 0;
	
	
	/**
	 * Creates a plant manager.
	 * 	
	 * @param worldSize
	 * @param initalNumber
	 * @param plantRegenRate
	 * @param plantStartingEnergy
	 * @param plantEnergyAbsorptionRate
	 * @param barrierManager BarrierManager
	 * @param worldSettings 
	 */
	public GenericPlantManager(BarrierManager barrierManager, GenericPlantSetupSettings plantSettings, WorldSetupSettings worldSettings)
	{
		this.initalNumber = plantSettings.getInitialPlantNumbers();

		this.barrierManager = barrierManager;

		this.worldSize = worldSettings.getWorldSize();

		this.plantRegenRate = plantSettings.getPlantRegenRate();

		this.plantRegenerationNSteps = plantSettings.getPlantRegenerationNSteps();
		
		this.plantStartingEnergy = plantSettings.getPlantStartingEnergy();

		this.basePlantEnergyAbsorptionRate = plantSettings.getPlantEnergyAbsorptionRate();

		setUpLists();
		
		addPlants(worldSize, initalNumber,true);

	}

	/** Draws all the plants with a toggle for body type
	 *  
	 * @param g Graphics	
	 * @param trueDrawing boolean
	 */
	public void drawPlants(Graphics g, boolean simpleDrawing)
	{

		doneList.resetHead();

		while (doneList.hasNext())
		{

			tPlantDraw = doneList.getNext();

			/*
			 * Optimization - Only draw visible plants that are inside the
			 * cameraBoundaries
			 */
			if (tPlantDraw.body.getBodyPos().getX() > (SimulationView.cameraBound.getX() - SimulationView.globalTranslate.getX()) && tPlantDraw.body.getBodyPos().getX() < (SimulationView.cameraBound.getMaxX() - SimulationView.globalTranslate.getX()) && tPlantDraw.body.getBodyPos().getY() > (SimulationView.cameraBound.getY() - SimulationView.globalTranslate.getY()) && tPlantDraw.body.getBodyPos().getY() < (SimulationView.cameraBound.getMaxY() - SimulationView.globalTranslate.getY()))
			{
				/*
				 * Optimization - draw correct circular bodies or faster
				 * rectangular bodies
				 */
				if (simpleDrawing)
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
	}

	/** Sets the barrier task for plants */
	public void stage2()
	{
		barrierManager.setBarrierPlantTask(doList, plantCount);
	}

	/** This stage performs the list updating, addition of new plants and stats updates. */
	public void stage3()
	{
		// The removal of dead plants
		updateDoneList();

		/* Plant Growth per Step - adds this many plants per step */
		addPlants(worldSize, plantRegenRate,false);	// log2(512) - +9... log2(1024)+10...

		// Stats Counter
		StatsPanel.setPlantNo(plantCount);

	}

	/** Updates the Done list. 
	 * This is effectively handling the death of plants in the simulation and if later implemented the reproduction of plants. 
	 */
	private void updateDoneList()
	{
		/* Recount all the plants - since some will have died... */
		plantCount = 0;

		doList.resetHead();
		

		/* temp var */
		GenericPlant temp;
		
		while (doList.hasNext())
		{
			/* Remove this Plant from the List */
			temp = doList.getNext();

			/** Is plant dead? */
			if (!temp.body.stats.isDead())
			{

				/* This results in a unstable growth rate for plants - DISABLED */
				/*
				 * if(temp.body.stats.canReproduce()) {
				 * temp.body.stats.decrementReproductionCost();
				 * 
				 * int x = rPos.nextInt(worldSize) + 1; int y =
				 * rPos.nextInt(worldSize) + 1;
				 * 
				 * addNewPlant(new GenericPlant(x,y,50f, 100f,
				 * basePlantEnergyAbsorptionRate,basePlantReproductionCost));
				 * 
				 * }
				 */
				//

				/** Plant is not dead add it to the done list */
				doneList.add(temp,temp.body.getBodyPos().getX());
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
	private void addPlants(int worldSize, int plantNumber, boolean initial)
	{
		add_calls++;

		if( (add_calls%plantRegenerationNSteps) == 0 || initial)
		{
			
			/* Random Starting Position */
			Random xr = new Random();
			Random yr = new Random();

			int x, y;

			for (int i = 0; i < plantNumber; i++)
			{
				x = xr.nextInt(worldSize) + 1;
				y = yr.nextInt(worldSize) + 1;

				while(World.isBoundaryWall(x, y))
				{
					x = xr.nextInt(worldSize) + 1;
					y = yr.nextInt(worldSize) + 1;				
				}
				
				addNewPlant(new GenericPlant(x, y, plantStartingEnergy, 100f, basePlantEnergyAbsorptionRate, basePlantReproductionCost));
			}						
		}
	}

	/** 
	 * Being born counts as an Action thus all new plants start in the done list
	 *  
	 * @param plant GenericPlant
	 */
	private void addNewPlant(GenericPlant plant)
	{
		doneList.add(plant,plant.body.getBodyPos().getX());
		plantCount++;
	}

	/** 
	 * Sets up the safe starting position for the lists 
	 * Any plant not moved out of the done list has been marked as dead and will not be in the do list.
	 * */
	private void setUpLists()
	{
		doList = doneList;
		doneList = new ArrayList<GenericPlant>();		
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
