package jCompute.Scenario.SAPP.Plant;

import jCompute.Datastruct.knn.KNNInf;
import jCompute.Datastruct.knn.thirdGenKDWrapper;
import jCompute.Gui.View.GUISimulationView;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.SAPP.SAPPScenario;
import jCompute.Scenario.SAPP.World.World;
import jCompute.Scenario.SAPP.World.WorldInf;
import jCompute.Scenario.SAPP.World.WorldSetupSettings;
import jCompute.Stats.SingleStat;
import jCompute.Stats.StatGroup;
import jCompute.Stats.StatManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;
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
	
	/** The Total number of plants */
	// plantTotal is the variable use to create a sample
	// statPlantTotal is the stat object used to record all samples
	private SingleStat statPlantTotal;	
	private int plantTotal;

	/** The size of the world, needed for correctly placing new plants */
	private int worldSize;
		
	private WorldInf world;
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
	
	private KNNInf<GenericPlant> plantKDTree;
	
	/*
	 * Internal
	 */
	private int add_calls = 0;
	
	
	/**
	 * Creates a plant manager.
	 * @param world 
	 * 	
	 * @param worldSize
	 * @param initalNumber
	 * @param plantRegenRate
	 * @param plantStartingEnergy
	 * @param plantEnergyAbsorptionRate
	 * @param barrierManager BarrierManager
	 * @param scenario 
	 */
	public GenericPlantManager(WorldInf world, GenericPlantSetupSettings plantSettings)
	{
		setUpStats();
		
		this.world = world;
		
		this.initalNumber = plantSettings.getInitialPlantNumbers();

		this.worldSize = world.getWorldBoundingSquareSize();

		this.plantRegenRate = plantSettings.getPlantRegenRate();

		this.plantRegenerationNSteps = plantSettings.getPlantRegenerationNSteps();
		
		this.plantStartingEnergy = plantSettings.getPlantStartingEnergy();

		this.basePlantEnergyAbsorptionRate = plantSettings.getPlantEnergyAbsorptionRate();

		setUpLists();
		
		plantKDTree = new thirdGenKDWrapper<GenericPlant>(2);		
		
		addPlants(initalNumber,true);

	}

	private void setUpStats()
	{
		statPlantTotal = new SingleStat("Plants");
		statPlantTotal.setColor(Color.green);
		plantTotal = 0;
	}
	
	public List<SingleStat> getPopulationStats()
	{
		List<SingleStat> stat = new LinkedList<SingleStat>();
		
		stat.add(statPlantTotal);
		
		return stat;
	}

	/** Draws all the plants with a toggle for body type
	 *  
	 * @param g Graphics	
	 * @param trueDrawing boolean
	 */
	public void draw(GUISimulationView simView)
	{
		for (GenericPlant tPlantDraw : doneList) 
		{
			tPlantDraw.body.draw(simView);
		}

	}

	public KNNInf<GenericPlant> doStep()
	{
		setUpLists();
		
		plantTotal = 0;
		
		/* 2d - KD-Tree */
		plantKDTree = new thirdGenKDWrapper<GenericPlant>(2);		

		int size = doList.size();
		
		for(GenericPlant currentPlant : doList)
		{
			if (!currentPlant.body.stats.isDead())
			{
				/* This Section adds each plant and its coordinates to the kd tree */
				plantKDTree.add(currentPlant.body.getBodyPosKD(), currentPlant);
				
				// Do the plants step
				currentPlant.body.stats.increment();

				/** Plant is not dead add it to the done list */
				doneList.add(currentPlant);
				plantTotal++;
			}
		}

		/* Add a sample to the stat recorder */
		statPlantTotal.addSample(plantTotal);

		/* Plant Growth per Step - adds this many plants per step */
		addPlants(plantRegenRate,false);	// log2(512) - +9... log2(1024)+10...
		
		return plantKDTree;
	}

	/*public KNNInf<GenericPlant> doStep()
	{
		setUpLists();
		
		plantTotal = 0;
		
		// 2d - KD-Tree 
		plantKDTree = new thirdGenKDWrapper<GenericPlant>(2);		

		int size = doList.size();
		
		for (int p=0;p<size;p++) 
		{	
			GenericPlant currentPlant = doList.get(p);

			if (!currentPlant.body.stats.isDead())
			{
				// This Section adds each plant and its coordinates to the kd tree 
				plantKDTree.add(currentPlant.body.getBodyPosKD(), currentPlant);
				
				// Do the plants step
				currentPlant.body.stats.increment();

				// Plant is not dead add it to the done list 
				doneList.add(currentPlant);
				plantTotal++;
			}

		}

		// Add a sample to the stat recorder 
		statPlantTotal.addSample(plantTotal);

		// Plant Growth per Step - adds this many plants per step 
		addPlants(plantRegenRate,false);	// log2(512) - +9... log2(1024)+10...
		
		return plantKDTree;
	}*/
	
	/** Updates the Done list. 
	 * This is effectively handling the death of plants in the simulation and if later implemented the reproduction of plants. 
	 */
	private void updateDoneList()
	{
		/* Recount all the plants - since some will have died... */
		plantTotal = 0;
		
		for (GenericPlant temp : doList) 
		{
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
				doneList.add(temp);
				plantTotal++;
			}
		}
		
		/* Add a sample to the stat recorder */
		statPlantTotal.addSample(plantTotal);
		
		//System.out.println("Stat Size" + statPlantTotal.sampleCount());
		
	}

	/** 
	 * Adds (n) number of plants randomly in the world
	 * 
	 * @param worldSize int
	 * @param plantNumber int
	 */
	private void addPlants(int plantNumber, boolean initial)
	{
		add_calls++;

		if( (add_calls%plantRegenerationNSteps) == 0 || initial)
		{
			
			/* Random Starting Position */
			int x, y;

			for (int i = 0; i < plantNumber; i++)
			{
				x = ThreadLocalRandom.current().nextInt(worldSize) + 1;
				y = ThreadLocalRandom.current().nextInt(worldSize) + 1;

				while(world.isInvalidPosition(x, y))
				{
					x = ThreadLocalRandom.current().nextInt(worldSize) + 1;
					y = ThreadLocalRandom.current().nextInt(worldSize) + 1;				
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
		plantKDTree.add(plant.body.getBodyPosKD(), plant);

		doneList.add(plant);
		plantTotal++;
	}

	/** 
	 * Sets up the safe starting position for the lists 
	 * Any plant not moved out of the done list has been marked as dead and will not be in the do list.
	 * */
	private void setUpLists()
	{
		doList = doneList;
		doneList = new ArrayList<GenericPlant>(plantTotal*2);		
	}

	/**
	 * Added for Unit tests
	 * 	
	 * @return int */
	public int getPlantNo()
	{
		return plantTotal;
	}

}
