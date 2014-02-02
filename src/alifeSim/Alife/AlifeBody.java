package alifeSim.Alife;

import java.awt.Color;

import alifeSim.Gui.NewSimView;
import alifeSimGeom.A2DCircle;
import alifeSimGeom.A2DRectangle;
import alifeSimGeom.A2DVector2f;
import alifeSimGeom.A2RGBA;

public class AlifeBody
{
	/** Agent Body */
	protected A2DCircle body;
	
	/** Color of this agent */
	protected A2RGBA color;
	
	/** Current Body Pos */
	protected A2DVector2f bodyPos;	

	/** The size of this body */
	private float size;
	
	public AlifeBody()
	{
		super();
				
		bodyPos = new A2DVector2f(0,0);
	}

/*
 * 
 *    
 *  position 
 *  
 */	
	private void initBody(float size)
	{
		this.size = size;

		body = new A2DCircle(0, 0, size);
	}
	
	public void setIntialPos(double pos[])
	{
		bodyPos = new A2DVector2f((float)pos[0], (float)pos[1]);
	}
	
	/** 
	 * Initial Cartesian X/Y Position 
	 * @param pos Vector2f
	 */
	public void setIntialPos(A2DVector2f pos)
	{
		bodyPos = pos;
	}	
	
	/** 
	 * Returns the position of the body
	 * @return Vector2f  */
	public A2DVector2f getBodyPos()
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
		return size * size;
	}
	
	/** 
	 * Returns the true size squared as a diameter for use in collision detection. 
	 * @return float */
	public float getTrueSizeSQRDiameter()
	{
		return (size * size) * 2;
	}		
	
/*
 * 
 * Drawing	
 */
	/**
	 * Set Color
	 * 
	 */
	public void setColor(A2RGBA color)
	{
		this.color = color;
	}
	
	/** 
	 * Slow Draw method - circles
	 * @param g Graphics
	 */
	public void draw(NewSimView simView)
	{
		body.setLocation(bodyPos.getX(), bodyPos.getY());

		simView.drawCircle(body, color);
	}	
}
