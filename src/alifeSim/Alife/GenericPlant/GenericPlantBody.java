package alifeSim.Alife.GenericPlant;

import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Vector2f;

import alifeSim.Alife.AlifeBody;
/**
 * 
 * This Class is the body of a plant.
 *
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class GenericPlantBody extends AlifeBody
{
	public final GenericPlantStats stats;

	/**
	 * Constructor for GenericPlantBody.
	 * @param pos Vector2f
	 * @param startingEnergy float
	 * @param maxEnergy float
	 * @param absorptionRate float
	 * @param basePlantReproductionCost float
	 */
	public GenericPlantBody(Vector2f pos, float startingEnergy, float maxEnergy, float absorptionRate, float basePlantReproductionCost)
	{
		stats = new GenericPlantStats(startingEnergy, maxEnergy, absorptionRate, basePlantReproductionCost);
		
		initPlantBody(stats.getSize());

		setIntialPos(pos);
	}

	/** 
	 * Initializes The body. 
	 */
	private void initPlantBody(float size)
	{
		setSize(size);
		
		setColor(Color.green);
	}

	/** 
	 * Sets the Color of the Body of this plant.
	 * 
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}


}
