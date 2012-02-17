package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

/*
 * Agent Body Class
 * - This Class performs the world movement checks and contains the draw code of this visual representation of the body.
 * - This is the only agent class that other agents "should"interact with.
 */

public class SimpleAgentBody
{
	/* Agent Body */
	private Rectangle body;
	private float size;

	private Circle true_body;
	private float true_size;
		
	private Color color;
	private Vector2f body_pos;
	private Vector2f new_body_pos = new Vector2f(0,0);
	
	private int type;
	
	private float max_speed=1;
	private float direction;
	
	private Vector2f forward_vector = new Vector2f(0,-max_speed); 	  /* Forward 1 up */
	private Vector2f new_forward_vector = new Vector2f(0,0);		  /* Latched 	  */
	
	public SimpleAgentBody(Vector2f pos,float size)
	{
		this.type = type;
		
		this.size = size;			
		
		initBody();
		
		setIntialPos(pos);
	}
	
	/* Init */
	private void initBody()
	{
		body = new Rectangle(0,0,this.size,this.size);

		true_size = body.getBoundingCircleRadius();
		
		true_body = new Circle(0,0,true_size);
					
		setColor();
	}
	
	/* Body Color */
	@SuppressWarnings("static-access")
	private void setColor()
	{
		color = Color.yellow;
	}
	
	/* TODO Polar Movement - Entry Move Statement - World Physics Will be Checked and Enforced, Physics can still deny the movement*/
	public boolean move(float req_direction)
	{		
		Vector2f new_pos = newPosition(req_direction);

		/* If physics says yes then move the agent */
		if( !World.isBondaryWall(new_pos.getX(),new_pos.getY()) ) 
		{
			//System.out.println("Safe - new_pos : X | " + new_pos.getX() + " Y |" + new_pos.getY());
			
			updateBodyPosition(new_pos);
			
			return true;
		}
		else
		{
			//System.out.println("Wall - new_pos : X | " + new_pos.getX() + " Y |" + new_pos.getY());
		}
		
		/* Agent is trying to move into a wall - move denied */
		return false;		
	}

	
	/* Like above but does't move - can be called by the agent brain to check if the move is valid */
	public boolean move_possible(float req_direction)
	{
		Vector2f new_pos = newPosition(req_direction);

		if( !World.isBondaryWall(new_pos.getX(),new_pos.getY()) ) 
		{					
			return true;
		}
	
		return false;		
	}
	
	private Vector2f newPosition(float req_direction)
	{
		//System.out.println("req_direction : " + req_direction);
		
		/* Get out current forward direction */
		new_forward_vector.set(forward_vector);
		
		/* Change it by the new direction */
		new_forward_vector.add(req_direction);
		
		/* Get our current Cartesian X and Y */
		new_body_pos.set(body_pos);
				
		/* Add our new forward vector */
		new_body_pos.add(new_forward_vector);
	
		//req_pos.set(body_pos.getX()+req_pos.getX(),body_pos.getY()+req_pos.getY());
		
		//System.out.println("vector  : X | " + new_body_pos.getX() + " Y |" + new_body_pos.getY());
		
		return new_body_pos;
				
	}
	
	/* Initial Cartesian X/Y Position */
	private void setIntialPos(Vector2f pos)
	{	
		body_pos = pos;
	}
	
	/* Internal Movement */
	private void updateBodyPosition(Vector2f pos)
	{
		body_pos.set(pos);				
	}

	/* External Getter */
	public Vector2f getBodyPos()
	{
		return body_pos;
	}
	
	/* 
	 * 
	 * Helpers 
	 * 
	 */
	/*private Vector2f polarToCar(float r,float theta)
	{
		float x = (float) (r * Math.cos(theta));
		float y = (float) (r * Math.sin(theta));
		
		return new Vector2f(y,-x);
	}
	
	private Vector2f carToPolar(float x, float y)
	{
		float r = (float) Math.sqrt((x*x)+(y*y));
		
		float theta = (float) Math.atan2(y, x);
		
		// Polar Vector 
		return new Vector2f(r,theta);
	}*/
	
	/* Returns the agents direction */
	public float getDirection()
	{
		return this.direction;
	}

	/* Fast Body Draw Method */
	public void drawRectBody(Graphics g)
	{
		body.setLocation(body_pos.getX()-(size/2), body_pos.getY()-(size/2));

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
	
	public void setDebugPos(Vector2f pos)
	{
		body_pos.set(pos.getX(), pos.getY());		
	}
	
	public float getSize()
	{
		return size;
	}
	
}
