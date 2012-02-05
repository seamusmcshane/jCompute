package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
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
		
	private Color color;
	private Vector2f body_pos;
	
	private float size;
	private int type;
	
	private float speed=1;
	private float direction;
	
	public SimpleAgentBody(Vector2f pos,float size,int type)
	{
		this.type = type;
		
		this.size = size;		
		
		body_pos = pos;
		
		initBody();
	}
	
	/* Init */
	private void initBody()
	{
		body = new Rectangle(0,0,this.size,this.size);
					
		setColor();
	}
	
	/* Body Color */
	@SuppressWarnings("static-access")
	private void setColor()
	{
		switch(type)
		{
			case 1: 
				color = Color.blue;
				break;
			case 2: 
				color = Color.red;
				break;
			case 3: 
				color = Color.green;
				break;
			case 4: 
				color = Color.yellow;
				break;
			default :
				color = color.black;
			break;
		}
	}
	
	/* TODO Polar Movement - Entry Move Statement - World Physics Will be Checked and Enforced, Physics can still deny the movement*/
	public boolean move(Vector2f req_pos)
	{
		/* If physics says yes then move the agent */
		if(!World.isBondaryWall(this.getBodyPos().getX()+req_pos.getX(), this.getBodyPos().getY()+req_pos.getY()))
		{
			
			updateBodyPosition(req_pos);
			
			return true;
		}
		
		/* Agent is trying to move into a wall - move denied */
		return false;		
	}

	/* TODO - Initial Position X/Y */
	private void setIntialPos(Vector2f pos)
	{
		body_pos.set(body_pos.getX()+pos.getX(), body_pos.getY()+body_pos.getY());
			
		body.setLocation(body_pos.getX()-(size/2), body_pos.getY()-(size/2));
	}
	
	/* Internal Movement */
	private void updateBodyPosition(Vector2f pos)
	{
		body_pos.set(body_pos.getX()+pos.getX(), body_pos.getY()+pos.getY());
				
		body.setLocation(body_pos.getX()-(size/2), body_pos.getY()-(size/2));
	}

	/* External Getter */
	public Vector2f getBodyPos()
	{
		return body_pos;
	}
	
	/* TODO polor directions */
	public float getDirection()
	{
		return this.direction;
	}

	/* Area of this Agent */
	public Rectangle getBodyBounds()
	{
		return body;
	}
	
	/* Body Draw Method */
	public void drawBody(Graphics g)
	{
		g.setColor(color);
		g.fill(body);			
	}
	
}
