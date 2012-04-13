package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import alife.SimpleAgentEnum.AgentType;

/**
 * Agent Body Class
 * - This Class performs the world movement checks and contains the draw code of this visual representation of the body.
 * - This is the only agent class that other agents brains "should" interact with.
 */
public class SimpleAgentBody
{
	/** Agent Body */
	private Rectangle body;
	
	/** The stats of this agent body */
	final SimpleAgentStats stats;
		
	/** The circular body and size */
	private Circle true_body;
	private float true_size;
		
	/** Color of this agent */
	private Color color;
	
	/** Current Body Pos */
	private Vector2f body_pos;
	
	/** Calculated body pos */
	private Vector2f new_body_pos = new Vector2f(0,0);
		
	/** Forward Vector */
	private Vector2f forward_vector;
	
	/** Calculated forward vector */
	private Vector2f new_forward_vector = new Vector2f(0,0);		  /* Latched 	  */

	/** Direction of movement of the Agent */
	private float direction;
	
	/** Is this agent still alive */
	private boolean alive=true;
	
	/** has this agent ate a plant */
	private boolean ate_plant=false;
	
	/** has this agent ate an agent */
	private boolean ate_agent=false;
		
	/**
	 * Creates a new agents body centred on the pos with the set stats.
	 * @param pos
	 * @param stats
	 */
	public SimpleAgentBody(Vector2f pos, SimpleAgentStats stats)
	{
		
		this.stats = stats;
		
		forward_vector = new Vector2f(0,-stats.getMaxSpeed()); 	  /* Forward 1 up */
			
		initBody();
		
		setIntialPos(pos);
	}
	
	/** Initialises the two body representations */
	private void initBody()
	{
		body = new Rectangle(0,0,stats.getSize(),stats.getSize());

		true_size = body.getBoundingCircleRadius();
		
		true_body = new Circle(0,0,true_size);
					
		setColor();
	}
	
	/** Sets the Body Color */
	private void setColor()
	{
		if(stats.getType().getType() == AgentType.PREY)
		{
			color = Color.blue;
		}
		else // Predator
		{
			color = Color.red;
		}
	}
	
	/** Polar Movement - Entry Move Statement - World Physics Will be Checked and Enforced, Physics can still deny the movement*/
	public boolean move(float req_direction)
	{		
		Vector2f new_pos = newPosition(req_direction);

		/* If physics says yes then move the agent */
		if( !World.isBoundaryWall(new_pos.getX(),new_pos.getY()) ) 
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
	
	/** Like above but does't move - can be called by the agent brain to check if the move is valid */
	public boolean move_possible(float req_direction)
	{
		Vector2f new_pos = newPosition(req_direction);

		if( !World.isBoundaryWall(new_pos.getX(),new_pos.getY()) ) 
		{					
			return true;
		}
	
		return false;		
	}
	
	/**
	 * Used to turn a direction of movement into a new XY coordinate.
	 * @param req_direction
	 * @return new_body_pos
	 */
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
	
	/** Initial Cartesian X/Y Position */
	private void setIntialPos(Vector2f pos)
	{	
		body_pos = pos;
	}
	
	/** Internal Movement - decrements move energy */
	private void updateBodyPosition(Vector2f pos)
	{
			stats.decrementMoveEnergy();
			body_pos.set(pos);					
	}

	/** External Getter */
	public Vector2f getBodyPos()
	{
		return body_pos;
	}
	
	/**
	 * The eat agent Action, attempts to eat the agent in view.
	 * @param view
	 * @return
	 */
	public boolean eatAgent(SimpleAgentView view)
	{		
		if(view.getOriginalAgentRef()!= null )
		{

			if(!view.getOriginalAgentRef().body.stats.isDead())
			{
				if(isAgentCloseEnoughToEat(view))
				{
					// Kill Agent
					stats.addEnergy(view.getOriginalAgentRef().body.stats.killAgent()); // 100% energy
					
					return true;
				}
			}
			
		}		
		return false;		
	}
	
	/**
	 * Checks if the agent in view is close enough to eat.
	 * @param view
	 * @return
	 */
	private boolean isAgentCloseEnoughToEat(SimpleAgentView view)
	{
		// If the distance between the position of the agent and other agent is less than the "true size" of the two bodies... ie are the agent and other agent touching
		if( (view.distanceTo(getBodyPos(), view.getOriginalAgentRef().body.getBodyPos()) ) < ( this.getTrueSizeSQRD() + view.getOriginalAgentRef().body.getTrueSizeSQRD()))
		{
			return true;
		}		
		
		return false;		
	}
	
	/**
	 * The eat plant Action, attempts to eat the plant in view.
	 * @param view
	 * @return
	 */
	public boolean eatPlant(SimpleAgentView view)
	{		
		if(view.getOriginalPlantRef()!= null )
		{

			if(!view.getOriginalPlantRef().body.stats.isDead())
			{
				if(isPlantCloseEnoughToEat(view))
				{
					// Remove plant energy (decrements the energy_consumption_rate amount from the plant each time)
					stats.addEnergy(view.getOriginalPlantRef().body.stats.decrementEnergy(stats.getEnergyConsumptionRate()));
					
					return true;
				}
			}
			
		}		
		return false;		
	}

	/**
	 * Checks if the plant in view is close enough to eat.
	 * @param view
	 * @return
	 */	
	private boolean isPlantCloseEnoughToEat(SimpleAgentView view)
	{
		// If the distance between the position of the agent and plant is less than the size of the two bodies... ie are the agent and plant touching
		if( (view.distanceTo(getBodyPos(), view.getOriginalPlantRef().body.getBodyPos()) ) < ( this.getTrueSizeSQRD() + view.getOriginalPlantRef().body.getTrueSizeSQRD()))
		{
			return true;
		}		
		
		return false;		
	}
		
	/** Returns the agents direction of movement */
	public float getDirection()
	{
		return this.direction;
	}

	/** Fast Body Draw Method - rectangles */
	public void drawRectBody(Graphics g)
	{
		body.setLocation(body_pos.getX()-(stats.getSize()/2), body_pos.getY()-(stats.getSize()/2));

		g.setColor(color);

		g.fill(body);			
	}
	
	/** Slow Draw method - circles */
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
		return (true_size*true_size)*2;
	}
	
}
