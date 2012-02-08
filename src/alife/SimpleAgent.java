package alife;
import java.util.Iterator;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;


public class SimpleAgent
{
	// Agent Body
	public SimpleAgentBody body;
	
	
	private int id;
	
	private Iterable<SimpleAgent> viewList; /* List of Agents in view */
	private Iterator iterator;
	

	/* Agent View Range */
	private int range=5;
	private float range_limit=0; /* Limit = size + range */
	private Circle field;	
	
	/* Agent Type */
	private int type;
		
	private float speed;
		
	private boolean draw_view = false;
	private boolean collision=false;
	private boolean visible = false;
	private int viewCount=0;
	
	/* Movement */
	float direction;
	Random r;
	private int moves=0;
	private int max_moves;
	
	private SimpleAgent nearestAgent=null;
	
	public SimpleAgent(int id,float x,float y,float size,int type)
	{
		this.id = id;
			
		this.speed = (1/size)+1;
		
		this.range = (int) (this.range * size)/2;
				
		this.type = type;
					
		
		r = new Random();
		
		//direction = r.nextInt(360)+1;
		
		direction = 0;
		
		max_moves = 50;
		
		
		
		createBody(new Vector2f(x,y),size);	
		
		setUpView();			
	}

	
	/*
	 * 
	 *  AI
	 */
	public void think()
	{
		
		if(this.id==-1)
		{
			return;
		}
		
		/* TODO if nothing in range and traveled for a while */		
		if(moves > max_moves && (nearestAgent == null) )
		{
			moves=0;
			direction = r.nextInt(360)+1;
		}

		/* Debug */
		// this.collision=false;
		
		if(nearestAgent!=null)
		{
			/* Debug */
			// this.collision=true;		

		}

		
		/* Reverse if stuck against wall */
		if(!body.move(direction))
		{		
			reverseDirection();
			body.move(direction);
		}
		
		moves++;

		upDateViewLocation();
	}

	/* Reverses the angle of the current direction  */
	private void reverseDirection()
	{

		direction = direction-180;

		if(direction<0)
		{
			direction=direction+360;
		}
	}
	
	/* View Range */
	private void setUpView()
	{
		range_limit =  range;
				
		field = new Circle(body.getBodyPos().getX(),body.getBodyPos().getY(),range_limit);
	}

	/* Debug - Representation of View position */
	private void upDateViewLocation()
	{
		field.setLocation(body.getBodyPos().getX()-(range_limit),body.getBodyPos().getY()-(range_limit));
	}
		
	/*
	 * 
	 *  World Physics
	 * 
	 */
	
	/* World Physics checks */
	private boolean canIMoveHere(float x,float y)
	{
		
		/* Check World Boundaries */
		if(World.isBondaryWall(x, y))
		{
			return false;
		}
		
		return true;
	}
	
	/*
	 * 
	 * Init 
	 * 
	 */
	private void createBody(Vector2f pos,float size)
	{
		body = new SimpleAgentBody(pos,size,type);
	}
	
	/*
	 * 
	 * Graphics 
	 * 
	 */
	public void setVisible(boolean status)
	{
		visible = status;
	}

	public Circle getFieldofView()
	{
		return field;
	}
	
	/* KNN */
	public Vector2f getPos()
	{
		return body.getBodyPos();
	}
		
	public int getType()
	{
		return type;
	}

	public double getRange()
	{
		return range_limit;
	}

	public void updateNearestAgentKD(SimpleAgent nearestAgent)
	{
		this.nearestAgent = nearestAgent;
	}
	
	/* Debug */	
	public void setViewDrawing(boolean setting)
	{
		draw_view = setting;
	}
	
	public void drawViewRange(Graphics g)
	{
		if(draw_view && !collision)
		{ 
			g.setColor(Color.white);
			g.draw(field);
		}
		else if(collision)
		{
			g.setColor(Color.yellow);
			g.draw(field);

		}
		
	}
	
	public void setDebugPos(Vector2f pos)
	{
		body.setDebugPos(pos);
		upDateViewLocation();
	}

	public int getId()
	{
		return id;
	}
	
}
