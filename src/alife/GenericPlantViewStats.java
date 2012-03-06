package alife;

import org.newdawn.slick.geom.Vector2f;

/** Used to store the "visible" statistics of the inViewPlant */
public class GenericPlantViewStats
{
	/** Energy Value of Plant in view */
	private float energy;	
	
	/** Position of the Plant in view (Cartesian) */
	Vector2f plantPos;
	
	/**
	 * A class that represents the statistics on a view of a GenericPlant 
	 */
	public GenericPlantViewStats()
	{
		initStats();
	}
	
/**
 * Update Statistics
 * 
 */
	public void updateStats(GenericPlant plant)
	{
		// Copies the Plant Position
		plantPos.set(plant.body.getBodyPos());
		
		this.energy = plant.body.stats.getEnergy();	
	}	
	
/**
 * 
 * Initialization 
 */
	public void initStats()
	{
		/** Energy of Plant in view */
		this.energy=0;

		/** Position of the Plant in view (Cartesian) */
		plantPos = new Vector2f();		
		
	}

/**
 * Clear Statistics
 * 
 */
	public void clearStats()
	{
		/** Energy of Plant in view */
		this.energy=0;

		/** Position of the Plant in view (Cartesian) */
		plantPos.set(0, 0);

	}
	
/**
 * Statistics Getters
 * 
 */
	/** Position of the Plant in view (Cartesian) */
	public Vector2f getPlantPos()
	{
		return plantPos;
	}

	/** Size of Plant in view */
	public float getEnergy()
	{
		return energy;
	}
	
}
