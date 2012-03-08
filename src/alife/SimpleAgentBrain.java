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
	private int chase_moves;
	private int chase_max_moves;
	
	/* Move counters */		
	public SimpleAgentBrain(SimpleAgentBody body)
	{
		/* Agents own Body */
		myBody = body;
		
		view = new SimpleAgentView();
		
		r = new Random();		
		
		/* Agent starts moving in a random direction */
		direction = r.nextInt(360)+1;

	}

	/**
	 * Think - threes steps 
	 * Step 1 - An instant evaluation based on view alone. (Rule)
	 * Step 2 - A reevaluation based on circumstance of the agent. (Exception)
	 * Step 3 - The performing of the chosen state. (Execute)
	 */
	public void think()
	{
		
		/* Enforce 1 think/move per agent */
		hasThought=false;
				
		// Sets the state based on the view...
		evaulateViewState();
		
		// Overrides set based on circumstance
		reevalateState();
		
		// Performs the state.
		doState();
		
		myBody.move(direction);

	}

	
	// Performs the state that has been chosen.
	private void doState()
	{
	
		switch(state)
		{
			case ROAM:
				
				// Just move
				
				break;
			case HUNT:				
				// have i been chasing for a while... maybe giveup as i dont seem to be catching this prey.
				
				break;
			case EVADE:				
				// have i been hunted for a while... maybe i should move towards another prey
				
				break;								
			case GRAZE:				
				// -- No transition... view needs to be more complex.. is there more food around...
				
				break;				
		}	

		
	}
	
	
	/** Reevaluates the evaluated state to avoid some static behavior */
	private void reevalateState()
	{
		switch(state)
		{
			case ROAM:
				// have i been roaming for a while... maybe change direction..
				
				break;
			case HUNT:				
				// have i been chasing for a while... maybe giveup as i dont seem to be catching this prey. -> Back to Roam mode
				
				break;
			case EVADE:				
				// -- No transition... view needs to be more complex.. are there other prey i could hide with... etc
				
				break;								
			case GRAZE:				
				// -- No transition... view needs to be more complex.. is there more food around...
				
				break;				
		}		
	}
	
	/** Evaluates the world state - ignores agent circumstance */
	private void evaulateViewState()
	{
		// Agents take precedence over plants in the evaluation - so prey will run way first even if a food source is nearby
		if(view.hasAgentInView()) // Agents in View
		{
			if(myBody.stats.getType().isSameType(view.agentType())) // Predators do not eat other predators and logical same behavior with prey
			{
				agentIsTheat=false;
				
				// Roam - move around looking for food
				state = AgentState.ROAM;
				
			}
			else // Are we prey or predator
			{
				if(myBody.stats.getType().strongerThan(view.agentType()) == 1) // I am predator to this prey!
				{
					// Hunt this agent down
					state = AgentState.HUNT;
				}
				else if(myBody.stats.getType().strongerThan(view.agentType()) == 0) // This agent is equal in strength to me
				{
					// Roam - look for food...
					state = AgentState.ROAM;
				}
				else // -1 - this agent is my natural predator
				{
					// Evade - this agent will hunt me down
					state = AgentState.EVADE;
				}
			}
		}
		else // No Agents in View
		{
			if(myBody.stats.getType().eatsPlants()) // Do we eat plants?
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
	
	private void evaluateState()
	{
		
	}
	
	private void chaseState()
	{
		
	}
	
	private void evadeState()
	{
		
	}
		
	private void roamState()
	{
		
	}
	
	private void restState()
	{
		
	}
}
