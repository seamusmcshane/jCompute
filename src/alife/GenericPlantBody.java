package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
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

	private Rectangle body;

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

		initBody();

		setIntialPos(pos);
	}

	/** 
	 * Initializes The body. 
	 */
	private void initBody()
	{
		body = new Rectangle(0, 0, stats.getSize(), stats.getSize());

		trueSize = body.getBoundingCircleRadius();

		trueBody = new Circle(0, 0, trueSize);

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
