package jCompute.Scenario.SAPP.Plant;

import jCompute.Gui.View.Graphics.A2DVector2f;

/**
 * This Class is an instantiation of a plant. 
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class GenericPlant
{
	public final GenericPlantBody body;

	/**
	 * Constructor for GenericPlant.
	 * @param x float
	 * @param y float
	 * @param startingEnergy float
	 * @param maxEnergy float
	 * @param absorptionRate float
	 * @param basePlantReproductionCost float
	 */
	public GenericPlant(float x, float y, float startingEnergy, float maxEnergy, float absorptionRate, float basePlantReproductionCost)
	{
		body = new GenericPlantBody(new A2DVector2f(x, y), startingEnergy, maxEnergy, absorptionRate, basePlantReproductionCost);
	}

}
