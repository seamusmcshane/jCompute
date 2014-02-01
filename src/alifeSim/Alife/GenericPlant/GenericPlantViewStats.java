package alifeSim.Alife.GenericPlant;

import alifeSimGeom.A2DVector2f;

/** 
 * Used to store the "visible" statistics of the inViewPlant  for agents.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class GenericPlantViewStats
{
	/** Energy Value of Plant in view */
	private float energy;

	/** Position of the Plant in view (Cartesian) */
	private A2DVector2f plantPos;

	/** Original plant that this view refers to */
	private GenericPlant originalPlant = null;

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
	 * @param plant GenericPlant
	 */
	public void updateStats(GenericPlant plant)
	{
		originalPlant = plant;

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

		originalPlant = null;

		/** Energy of Plant in view */
		this.energy = 0;

		/** Position of the Plant in view (Cartesian) */
		plantPos = new A2DVector2f();

	}

	/**
	 * Clear Statistics
	 * 
	 */
	public void clearStats()
	{
		originalPlant = null;

		/** Energy of Plant in view */
		this.energy = 0;

		/** Position of the Plant in view (Cartesian) */
		plantPos.set(0, 0);

	}

	/**
	 * Position of the Plant in view (Cartesian)
	 * 
	 * @return Vector2f */
	public A2DVector2f getPlantPos()
	{
		return plantPos;
	}

	/** 
	 * Size of Plant in view
	 * @return float */
	public float getEnergy()
	{
		return energy;
	}

	/**
	 * Returns a reference to the plant object.
	 * 
	 * @return GenericPlant */
	public GenericPlant getOriginalPlantRef()
	{
		return originalPlant;
	}

}
