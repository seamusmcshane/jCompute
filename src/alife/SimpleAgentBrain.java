package alife;

import java.util.Iterator;
import java.util.Random;

import org.newdawn.slick.geom.Circle;

import alife.SimulationEnums.AgentState;

public class SimpleAgentBrain
{

	/* Agent is aware of its own body */
	private SimpleAgentBody myBody;
	
	// Current State of View
	public SimpleAgentView view;
	
	/* State Logic */
    private boolean hasThought=false;
    private AgentState state = AgentState.ROAM;
	    
    /* View evaluation */
    private boolean plantIsFood=false;
    private boolean agentIsFood=false;
    private boolean agentIsTheat=false;

	/* Movement */
	private float direction;
	private Random r;
	
	private int moves=0;
	
	private int roam_moves=0;
	private int roam_max_moves=20;
	
	private int hunt_moves=0;
	private int hunt_max_moves=20;
	
	private int hunt_exit_wait=0;
	private int hunt_exit_max_wait=40;
	
	private boolean rest=false;
	
	/* Move counters */		
	public SimpleAgentBrain(SimpleAgentBody body)
	{
		/* Agents own Body */
		myBody = body;
		
		view = new SimpleAgentView();
		
		r = new Random();		
		
		/* Agent starts moving in a random direction */
		direction = r.nextInt(360);

	}

	/**
	 * Think - threes steps 
	 * Step 1 - An instant evaluation based on view alone. (Rule)
	 * Step 2 - A reevaluation based on circumstance of the agent before performing the state. (Exception then Execute)
	 */
	public void think()
	{
		
		/* Enforce 1 think/move per agent */
		hasThought=false;
				
		// Sets the state based on the view...
		evaulateViewState();
		
		// Overrides set based on circumstance
		reEvalateState();
				
		if(!rest)
		{
			myBody.move(direction);
			moves++;
		}

	}
	
	/** Reevaluates the evaluated state to avoid some static behavior */
	private void reEvalateState()
	{
		switch(state)
		{
			case ROAM:
		
				roamState();
				
				break;
			case HUNT:				
				
				huntState();
				
				break;
			case EVADE:		
				
				evadeState();

				break;								
			case GRAZE:				

				grazeState();
				
				break;			
		}		
	}
	
	/** Evaluates the world state - ignores agent circumstance */
	private void evaulateViewState()
	{
		// Agents take precedence over plants in the evaluation - so prey will run way first even if a food source is nearby
		if(view.hasAgentInView()) // Agents in View
		{

			if(myBody.stats.getType().strongerThan(view.agentType()) == 1) // I am predator to this prey!
			{
				// Hunt this agent down
				state = AgentState.HUNT;
			}
			else if(myBody.stats.getType().strongerThan(view.agentType()) == 0) // This agent is equal in strength to me (same type)
			{
				if(view.hasPlantInView() && myBody.stats.getType().eatsPlants()) // Do we eat plants?
				{
					// move towards 
					state = AgentState.GRAZE;
				}
				else // Nope we eat other agents - but there are none...
				{
					// Roam - no food for us
					state = AgentState.ROAM;
				}
			}
			else // -1 - this agent is my natural predator
			{
				// Evade - this agent will hunt me down
				state = AgentState.EVADE;
			}
			
		}
		else // No Agents in View
		{
			
			if(view.hasPlantInView() && myBody.stats.getType().eatsPlants()) // Do we eat plants?
			{
				// move towards 
				state = AgentState.GRAZE;
			}
			else // Nope we eat other agents - but there are none...
			{
				// Roam - no food for us
				state = AgentState.ROAM;
			}
			
		}
		
	}
	
	/* Roam State */
	private void roamState()
	{	
		roam_moves++;
		
		if(roam_moves>roam_max_moves) // Have i been roaming for a while...  change direction..
		{
			direction = r.nextInt(360);
			roam_moves=0;
		}
		
		direction = direction;	

	}
	
	/* Hunt State */
	private void huntState()
	{
		hunt_moves++;
		
		if(hunt_moves>hunt_max_moves) // have i been chasing for a while... maybe giveup as i dont seem to be catching this prey. -> Back to Roam mode
		{
			huntExitSubState();	
		}
		else
		{
			direction = view.towardsAgentDirection(myBody);	
			
			// Check if can eat agent...
			eatAgentSubState();
			
		}
						
	}
	
	private void eatAgentSubState()
	{
		
	}
	
	private boolean eatPlantSubState()
	{
		if(myBody.eatPlant(view))
		{
			return true;
		}
		else
		{
			return false;		
		}
	}
	
	private void huntExitSubState()
	{
		hunt_exit_wait++;
		
		if(hunt_exit_wait>hunt_exit_max_wait)
		{
			direction = r.nextInt(360);
			hunt_moves=0;
			rest=false;
			
			state = AgentState.ROAM; // Back to roaming state
		}
		else
		{
			rest=true;
		}
		
	}
	
	private void grazeState()
	{
		// -- No transition... view needs to be more complex.. is there more food around...
		direction = view.towardsPlantDirection(myBody);
		
		if(eatPlantSubState())
		{
			state = AgentState.ROAM; // Back to roaming state
		}
				
	}
	
	// Evade State -- No transition... view needs to be more complex.. are there other prey i could hide with... etc
	private void evadeState()
	{
		direction = view.awayfromAgentDirection(myBody);				
	}

}
