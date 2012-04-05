package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

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
	
	/* Initial Cartesian X/Y Position */
	private void setIntialPos(Vector2f pos)
	{	
		body_pos = pos;
	}
	
	/* Init */
	private void initBody()
	{
		body = new Rectangle(0,0,stats.getSize(),stats.getSize());

		true_size = body.getBoundingCircleRadius();
		
		true_body = new Circle(0,0,true_size);
					
		setColor();
	}
	
	public Vector2f getBodyPos()
	{
		return body_pos;
	}
	
	/* Fast Body Draw Method */
	public void drawRectBody(Graphics g)
	{
		body.setLocation(body_pos.getX()-(stats.getSize()/2), body_pos.getY()-(stats.getSize()/2));

		g.setColor(color);

		g.fill(body);			
	}
	
	public void drawTrueBody(Graphics g)
	{
		true_body.setLocation(body_pos.getX()-(true_size), body_pos.getY()-(true_size));

		g.setColor(color);
		
		g.fill(true_body);	
		
		drawRectBody(g);
	}

	/* Body Color */
	private void setColor()
	{
		color = Color.green;
	}
	
}
