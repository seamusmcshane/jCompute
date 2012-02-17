package alife;

import java.util.Iterator;
import java.util.Random;

import org.newdawn.slick.geom.Circle;

public class SimpleAgentBrain
{

	private Iterable<SimpleAgent> viewList; /* List of Agents in view */
	private Iterator iterator;	

	/* Agent is aware of its own body */
	private SimpleAgentBody myBody;
	
	/* Nearest Agent */
	private SimpleAgent nearestAgent=null;
	
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
	
	public SimpleAgentBrain(SimpleAgentBody body)
	{
		/* Agents own Body */
		myBody = body;
		
		r = new Random();
		
		direction = r.nextInt(360)+1;
		
		//direction = 0;
		
		max_moves = r.nextInt(50)+50;;
		
		chase_max_moves = max_moves/2;
		
		rest_max_moves=chase_max_moves/2;

	}

	/**
	 * Think.
	 */
	public void think()
	{
				
		/* Enforce 1 think/move per agent */
		boolean has_thought=false;
				
		if(chase_moves < chase_max_moves && nearestAgent!=null && has_thought==false)
		{
			/* Debug */
			// this.collision=true;
			
			/* If i am a predator and my prey is near by - move towards it */
			/*if(type == Type.Predator && nearestAgent.getType() == Type.Prey )
			{
				direction = towardsAgentDirection();
			}*/
			
			/* if i am a prey and there is a pred near */
			/*if(type == Type.Prey && nearestAgent.getType() == Type.Predator )
			{
				direction = awayfromAgentDirection();
			}*/

			has_thought=true;	
			
			chase_moves++;		
			
			//System.out.println("C : " + chase_moves);
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
				
				direction = r.nextInt(360)+1;
				
				rest_moves=0;
				
				moves=0;
				
				chase_moves=0;
				
				
				has_thought=true;					
			}
		}
		else /* We dont need to rest - lets move */
		{
		
			/* Can we move in the current direction */		
			if(!myBody.move_possible(direction))
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
			myBody.move(direction);

			moves++;
			
			//System.out.println("ID : " + id + " M :" + moves);

			
		}

	}
	
	private float awayfromAgentDirection()
	{
		float dx = nearestAgent.body.getBodyPos().getX()-myBody.getBodyPos().getX() ;

		float dy =  myBody.getBodyPos().getY() - nearestAgent.body.getBodyPos().getY();
		
		return (float) Math.toDegrees(Math.atan2(dy,dx));
	}
	
	private float towardsAgentDirection()
	{
		float dx = nearestAgent.body.getBodyPos().getX()-myBody.getBodyPos().getX() ;

		float dy =  myBody.getBodyPos().getY() - nearestAgent.body.getBodyPos().getY();
		
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

	/*
	 *  Agents View - set by ViewGeneratorThread
	 */
	public void updateNearestAgentKD(SimpleAgent nearestAgent)
	{
		this.nearestAgent = nearestAgent;
	}
	
}
