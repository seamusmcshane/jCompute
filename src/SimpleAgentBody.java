import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

public class SimpleAgentBody
{

	/* Agent Body */
	Rectangle body;
	Rectangle body_center;
		
	Color color;

	Vector2f body_pos;
	

	float size;
	int type;
	
	float speed=1;
	float dir=0;
	public float direction;
	
	public SimpleAgentBody(Vector2f pos,float size,int type)
	{
		this.type = type;
		
		this.size = size;		
		
		body_pos = pos;
		
		initBody();
	}
	
	/*
	 * 
	 * Init 
	 */
	
	private void initBody()
	{
			
		body = new Rectangle(0,0,this.size,this.size);
			
		body_center = new Rectangle(0,0,0,0);
		
		color();
	}
	
	private void color()
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
				color = color.black
				;
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

	private void setIntialPos(Vector2f pos)
	{
		body_pos.set(body_pos.getX()+pos.getX(), body_pos.getY()+body_pos.getY());
			
		body.setLocation(body_pos.getX()-(size/2), body_pos.getY()-(size/2));
		
		body_center.setLocation((body_pos.getX()-(size/2))+(size/4), (body_pos.getY()-(size/2))+(size/4));		
	}
	
	private void updateBodyPosition(Vector2f pos)
	{
		body_pos.set(body_pos.getX()+pos.getX(), body_pos.getY()+pos.getY());
				
		body.setLocation(body_pos.getX()-(size/2), body_pos.getY()-(size/2));
		
		body_center.setLocation((body_pos.getX()-(size/2))+(size/4), (body_pos.getY()-(size/2))+(size/4));
	}

	public Vector2f getBodyPos()
	{
		return body_pos;
	}
	
	
	public float getDirection()
	{
		return this.direction;
	}
	/*
	 * 
	 * Graphics
	 * 
	 */
	
	public Rectangle getBodyBounds()
	{
		return body;
	}
	
	public void drawBody(Graphics g)
	{
		g.setColor(color);
		g.fill(body);
		
		//g.setColor(Color.white);
		//g.fill(body_center);
	
	}
	

}
