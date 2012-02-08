package alife;
import java.util.Iterator;
import java.util.Random;

import org.lwjgl.input.Mouse;
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
	private int range=10;
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

	
	private int chase_moves=0;
	private int chase_max_moves;

	private int rest_moves=0;
	private int rest_max_moves;
	
	private int wall_count=0;
	private int moves_since_wall=0;
	
	private SimpleAgent nearestAgent=null;
	
	public SimpleAgent(int id,float x,float y,float size,int type)
	{
		this.id = id;
			
		this.speed = (1/size)+1;
		
		this.range = (int) (this.range * size)/2;
				
		this.type = type;					
		
		r = new Random();
		
		direction = r.nextInt(360)+1;
		
		//direction = 0;
		
		max_moves = 100;
		
		chase_max_moves = max_moves/2;
		
		rest_max_moves=chase_max_moves/2;
		
		createBody(new Vector2f(x,y),size);	
		
		setUpView();			
	}

	
	/*
	 * 
	 *  AI
	 */
	public void think()
	{
		/* Debug */
		// this.collision=false;
		
		if(this.id==-1)
		{
			return;
		}
		
		/* Enforce 1 think/move per agent */
		boolean has_thought=false;
				
		if(chase_moves < chase_max_moves && nearestAgent!=null && has_thought==false)
		{
			/* Debug */
			// this.collision=true;
			
			/* If i am a predator and my prey is near by - move towards it */
			if(type == Type.Predator && nearestAgent.getType() == Type.Prey )
			{
				direction = towardsAgentDirection();
			}
			
			/* if i am a prey and there is a pred near */
			if(type == Type.Prey && nearestAgent.getType() == Type.Predator )
			{
				direction = awayfromAgentDirection();
			}

			has_thought=true;	
			
			chase_moves++;		
			
			System.out.println("C : " + chase_moves);
		}
		
		/* We have move too much - rest */
		if( ( (chase_moves > chase_max_moves) || ( moves > max_moves ) ) && has_thought==false  )
		{	
			if( rest_moves < rest_max_moves ) /* Rest for this mant moves */
			{
				//System.out.println("R : " + rest_moves);
				
				rest_moves++;
				
				has_thought=true;			
			}
			else /* Rested enough - last rest move */
			{
				//System.out.println("R : " + rest_moves);
				
				rest_moves=0;
				
				moves=0;
				
				chase_moves=0;
				
				
				has_thought=true;					
			}
		}
		else /* We dont need to rest - lets move */
		{
		
			/* Can we move in the current direction */		
			if(!body.move_possible(direction))
			{		
				/* Count the walls we hit */
				wall_count++;
				
				if(wall_count>2) /* We have move from one edge to another - assumes rectangular world */
				{
					direction = r.nextInt(360)+1;
					wall_count = 0;
				}	
				else /* We have reached one edge */
				{				
					reverseDirection(); 
				}
							
				has_thought=true;
			}
			
			/* Move is ok - move */
			body.move(direction);

			moves++;
			
			System.out.println("ID : " + id + " M :" + moves);

			upDateViewLocation();
			
		}

	}

	private float awayfromAgentDirection()
	{
		float dx = nearestAgent.body.getBodyPos().getX()-body.getBodyPos().getX() ;

		float dy =  body.getBodyPos().getY() - nearestAgent.body.getBodyPos().getY();
		
		return (float) Math.toDegrees(Math.atan2(dy,dx));
	}
	
	private float towardsAgentDirection()
	{
		float dx = nearestAgent.body.getBodyPos().getX()-body.getBodyPos().getX() ;

		float dy =  body.getBodyPos().getY() - nearestAgent.body.getBodyPos().getY();
		
		return (float) Math.toDegrees(Math.atan2(dx,dy));
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
