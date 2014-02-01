package alifeSim.Alife.GenericPlant;


import alifeSim.Alife.AlifeBody;
import alifeSimGeom.A2DVector2f;
import alifeSimGeom.A2RGBA;
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
	public GenericPlantBody(A2DVector2f pos, float startingEnergy, float maxEnergy, float absorptionRate, float basePlantReproductionCost)
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
		
		setColor(new A2RGBA(0f,1f,0f,0f));
	}

	/** 
	 * Sets the Color of the Body of this plant.
	 * 
	 */
	public void setColor(A2RGBA color)
	{
		this.color = color;
	}


}
