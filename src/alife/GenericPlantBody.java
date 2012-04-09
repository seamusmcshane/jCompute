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
 */
public class GenericPlantBody
{
	public GenericPlantStats stats;

	private Rectangle body;
	
	private Vector2f body_pos;
	
	private Circle true_body;
	
	private float true_size;
	
	private Color color;
	
	public GenericPlantBody(Vector2f pos,float starting_energy, float max_energy, float absorption_rate, float base_plant_reproduction_cost)
	{
		stats = new GenericPlantStats(starting_energy, max_energy, absorption_rate, base_plant_reproduction_cost);
				
		initBody();
		
		setIntialPos(pos);
	}	
	
	/** Initialises The body. */
	private void initBody()
	{
		body = new Rectangle(0,0,stats.getSize(),stats.getSize());

		true_size = body.getBoundingCircleRadius();
		
		true_body = new Circle(0,0,true_size);
					
		setColor();
	}
	
	/** Sets the Initial Cartesian X/Y Position */
	private void setIntialPos(Vector2f pos)
	{	
		body_pos = pos;
	}	
	
	/** Returns a Vector2f representing the position of the plants body */
	public Vector2f getBodyPos()
	{
		return body_pos;
	}
	
	/** Draws the Faster Rectangle version of the plant Body */
	public void drawRectBody(Graphics g)
	{
		body.setLocation(body_pos.getX()-(stats.getSize()/2), body_pos.getY()-(stats.getSize()/2));

		g.setColor(color);

		g.fill(body);			
	}
	
	/** Draws the true circular body of the plant */
	public void drawTrueBody(Graphics g)
	{
		true_body.setLocation(body_pos.getX()-(true_size), body_pos.getY()-(true_size));

		g.setColor(color);
		
		g.fill(true_body);	
		
		drawRectBody(g);
	}

	/** Returns the true size squared for use in collision detection */
	public float getTrueSizeSQRD()
	{
		return (true_size*true_size)*2; //radius > diameter
	}
	
	/** Sets the Color of the Body of this plant */
	private void setColor()
	{
		color = Color.green;
	}
	
}
