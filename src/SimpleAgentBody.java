import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

public class SimpleAgentBody
{

	Rectangle body;
	Rectangle body_center;
	
	/* Tree */
	com.infomatiq.jsi.Rectangle bodybounds;
	
	Color color;

	
	float x;
	float y;
	float size;
	int type;
	
	public SimpleAgentBody(float x,float y,float size,int type)
	{
		this.type=type;
		this.size = size;		
		initBody();
		updateBodyPosition(x,y);		
	}
	
	/*
	 * 
	 * Init 
	 */
	
	private void initBody()
	{
		
		
		body = new Rectangle(this.x-(size/2),this.y-(size/2),this.size,this.size);
		
		/* FOR TREE */
		bodybounds = new com.infomatiq.jsi.Rectangle(this.x-(size/2),this.y-(size/2),this.size,this.size);	
		
		body_center = new Rectangle(this.x-(size/2),this.y-(size/2),this.size/2,this.size/2);
		
		color();
	}
	
	private void color()
	{
		switch(type)
		{
			case 1: 
				color = Color.red;
				break;
			case 2: 
				color = Color.blue;
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
	
	/* Entry Move Statement - World Physics Will be Checked and Enforced, Physics can still deny the movement*/
	public boolean move(float request_X,float request_Y)
	{
		/* If physics says yes then move the agent */
		if(!World.isBondaryWall(request_X, request_Y))
		{
			this.x = request_X;
			this.y = request_Y;
			
			updateBodyPosition(this.x, this.y);
			
			return true;
		}
		
		/* Agent is trying to move into a wall - move denied */
		return false;		
	}
	
	private void updateBodyPosition(float x,float y)
	{
	
		this.x = x-(size/2);
		
		this.y = y-(size/2);
		
		body.setLocation(this.x, this.y);
		
		body_center.setLocation(this.x+(size/4), this.y+(size/4));
	}

	/*
	 * 
	 * Graphics
	 * 
	 */
	
	public com.infomatiq.jsi.Rectangle getBodyBounds()
	{
		return bodybounds;
	}
	
	public void drawBody(Graphics g)
	{
		g.setColor(color);
		g.fill(body);
		
		//g.setColor(Color.white);
		//g.fill(body_center);
	
	}
	

}
