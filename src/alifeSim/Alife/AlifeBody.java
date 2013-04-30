package alifeSim.Alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

public class AlifeBody
{
	/** Agent Body */
	protected Rectangle body;
	
	/** The circular body and size */
	protected Circle trueBody;
	
	protected float trueSize;
	
	/** Color of this agent */
	protected Color color;
	
	/** Current Body Pos */
	protected Vector2f bodyPos;	

	/** The size of this body */
	private float size;
	
	public AlifeBody()
	{
		super();
				
		bodyPos = new Vector2f(0,0);
	}

/*
 * 
 *    
 *  position 
 *  
 */	
	private void initBody(float size)
	{
		body = new Rectangle(0, 0, getSize(), getSize());

		trueSize = body.getBoundingCircleRadius();

		trueBody = new Circle(0, 0, trueSize);
	}
	
	
	/** 
	 * Initial Cartesian X/Y Position 
	 * @param pos Vector2f
	 */
	protected void setIntialPos(Vector2f pos)
	{
		bodyPos = pos;
	}	
	
	/** 
	 * Returns the position of the body
	 * @return Vector2f  */
	public Vector2f getBodyPos()
	{
		return bodyPos;
	}
	
	/** 
	 * Returns the position of the body
	 * */
	public double[] getBodyPosKD()
	{		
		return new double[] {(double) bodyPos.getX(),(double) bodyPos.getY()};
	}	
	
	public void setSize(float size)
	{
		this.size = size;
		
		initBody(size);		
	}
	
	public float getSize()
	{
		return size;
	}

	
	/** 
	 * Returns the true size squared as a radius for use in KNN. 
	 * @return float */
	public float getTrueSizeSQRRadius()
	{
		return trueSize * trueSize;
	}
	
	/** 
	 * Returns the true size squared as a diameter for use in collision detection. 
	 * @return float */
	public float getTrueSizeSQRDiameter()
	{
		return (trueSize * trueSize) * 2;
	}		
	
/*
 * 
 * Drawing	
 */
	/**
	 * Set Color
	 * 
	 */
	protected void setColor(Color color)
	{
		this.color = color;
	}
	
	/** 
	 * Slow Draw method - circles
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
	 * Fast Body Draw Method - rectangles
	 * @param g Graphics
	 */
	public void drawRectBody(Graphics g)
	{
		body.setLocation(bodyPos.getX() - (getSize() / 2), bodyPos.getY() - (getSize() / 2));

		g.setColor(color);

		g.fill(body);
	}	
}
