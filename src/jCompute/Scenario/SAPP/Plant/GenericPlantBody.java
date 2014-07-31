package jCompute.Scenario.SAPP.Plant;


import jCompute.Gui.View.GUISimulationView;
import jCompute.Gui.View.Graphics.A2DCircle;
import jCompute.Gui.View.Graphics.A2DRectangle;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Gui.View.Graphics.A2RGBA;
import jCompute.Scenario.SAPP.BodyInf;
/**
 * 
 * This Class is the body of a plant.
 *
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class GenericPlantBody implements BodyInf
{
	public final GenericPlantStats stats;

	/** Agent Body */
	protected A2DCircle body;
	
	/** Color of this agent */
	protected A2RGBA color;
	
	/** Current Body Pos */
	protected A2DVector2f bodyPos;	

	/** The size of this body */
	private float size;
	
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
	 * Initial Cartesian X/Y Position 
	 * @param pos Vector2f
	 */
	private void setIntialPos(A2DVector2f pos)
	{
		bodyPos = pos;
	}	
	
	private void initBody(float size)
	{
		this.size = size;

		body = new A2DCircle(0, 0, size);
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
	
	public void setColor(A2RGBA color)
	{
		this.color = color;
	}	

	public void draw(GUISimulationView simView)
	{
		body.setLocation(bodyPos.getX(), bodyPos.getY());

		simView.drawFilledCircle(body, color);
	}
	
	public A2DRectangle getBoundingRectangle()
	{
		return new A2DRectangle(0,0,0,0);
	}

}
