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
public class GenericPlantBody
{
	public GenericPlantStats stats;

	private Rectangle body;

	private Vector2f bodyPos;

	private Circle trueBody;

	private float trueSize;

	private Color color;

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

		setColor();
	}

	/** 
	 * Sets the Initial Cartesian X/Y Position.
	 *
	 * @param pos Vector2f
	 */
	private void setIntialPos(Vector2f pos)
	{
		bodyPos = pos;
	}

	/** 
	 * Returns a Vector2f representing the position of the plants body.
	 * 
	 * @return Vector2f */
	public Vector2f getBodyPos()
	{
		return bodyPos;
	}

	/** 
	 * Draws the Faster Rectangle version of the plant Body.
	 * 
	 * @param g Graphics
	 */
	public void drawRectBody(Graphics g)
	{
		body.setLocation(bodyPos.getX() - (stats.getSize() / 2), bodyPos.getY() - (stats.getSize() / 2));

		g.setColor(color);

		g.fill(body);
	}

	/** 
	 * Draws the true circular body of the plant.
	 *  
	 * @param g Graphics
	 */
	public void drawTrueBody(Graphics g)
	{
		trueBody.setLocation(bodyPos.getX() - (trueSize), bodyPos.getY() - (trueSize));

		g.setColor(color);

		g.fill(trueBody);

		drawRectBody(g);
	}

	/** 
	 * Returns the true size squared for use in collision detection.
	 * 
	 * @return float */
	public float getTrueSizeSQRDiameter()
	{
		return (trueSize * trueSize) * 2; //radius > diameter
	}

	/** 
	 * Sets the Color of the Body of this plant.
	 * 
	 */
	private void setColor()
	{
		color = Color.green;
	}

}
