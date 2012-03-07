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
	
	public SimpleAgentView view;
	
	/* State Logic */
    private boolean hasThought=false;
	    
    /* View evaluation */
    private boolean plantIsFood=false;
    private boolean agentIsFood=false;
    private boolean agentIsTheat=false;
    
    
	/* Movement */
	private float direction;
	private Random r;
	private int moves=0;
	private int chase_moves;
	private int chase_max_moves;
	
	/* Move counters */		
	public SimpleAgentBrain(SimpleAgentBody body)
	{
		/* Agents own Body */
		myBody = body;
		
		view = new SimpleAgentView();
		
		r = new Random();
		
		direction = r.nextInt(360)+1;

	}

	/**
	 * Think.
	 */
	public void think()
	{
		
		/* Enforce 1 think/move per agent */
		hasThought=false;
				
		evaulateViewState();
		
		doState();
		
		myBody.move(direction);

	}

	private void doState()
	{
	 	
	}
	
	
	private void evaulateViewState()
	{
		
		if(view.hasAgentInView()) // Agents in View
		{
			if(myBody.stats.getType().compareType(view.agentType()))
			{
				agentIsTheat=false;
			}
			else // Are we prey are predator
			{
				if(myBody.stats.getType().strongerThan(view.agentType()) == 1)
				{
					// Chase
				}
				else if(myBody.stats.getType().strongerThan(view.agentType()) == 0)
				{
					// Ignore
				}
				else // -1
				{
					// RUN!
				}
			}
		}
		else // No Agents in View
		{
			if(myBody.stats.getType().eatsPlants())
			{
				// move towards 
			}
			else
			{
				// Roam - no food for us
			}
			
		}
		
	}
	
	private void evaluateState()
	{
		
	}
	
	private void chaseState()
	{
		
	}
	
	private void restState()
	{
		
	}
	
	private void roamState()
	{
		
	}
	
	
}
