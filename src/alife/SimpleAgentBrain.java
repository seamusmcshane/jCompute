package alife;

import java.util.Random;

import alife.SimpleAgentEnum.AgentEval;
import alife.SimpleAgentEnum.AgentState;

/**
 * Agent Brain Class.
 * This class contains the main method "think()" for each agent.
 * Through think() the finite state machine is processed.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentBrain
{

	/* Agent is aware of its own body */
	private SimpleAgentBody myBody;

	// Current State of View
	public SimpleAgentView view;

	/* State Logic */
	private AgentState state = AgentState.ROAM;

	/* Movement */
	private float direction;
	private Random r;

	/* Move counters */
	private int roamMoves = 40; // Starts at the exit of roaming state
	private int roamMaxMoves = 40;

	private int huntMoves = 0;
	private int huntMaxMoves = 30;

	private int huntExitWait = 0;
	private int huntExitMaxWait = 0;

	private int learnToMoveMax = 20;
	private int learnToMoveCount = 0;

	/**
	 * Constructor for SimpleAgentBrain.
	 * @param body SimpleAgentBody
	 */
	public SimpleAgentBrain(SimpleAgentBody body)
	{
		/* Agents own Body */
		myBody = body;

		view = new SimpleAgentView(body);

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

		// Sets the state based on the view...
		evaulateViewState();

		// Overrides state based on circumstance
		reEvaluateState();

		if (!learnToMoveCounterPassed())
		{
			direction = r.nextInt(360);
		}

		myBody.move(direction);

	}

	/**
	 * Simulates the time it would take to learn to move.
	 *
	 * @return boolean */
	public boolean learnToMoveCounterPassed()
	{
		if (learnToMoveCount < learnToMoveMax)
		{
			learnToMoveCount++;
			return false;
		}
		else
		{
			return true;
		}
	}

	/** Reevaluates the evaluated state to avoid some static behavior */
	private void reEvaluateState()
	{
		switch (state)
		{
			case ROAM :

				roamState();

				break;
			case HUNT :

				huntState();

				break;
			case EVADE :

				evadeState();

				break;
			case GRAZE :

				grazeState();

				break;
		}
	}

	/** Evaluates the world state - ignores agent circumstance (no memory ) */
	private void evaulateViewState()
	{
		// Agents take precedence over plants in the evaluation - so prey will run way first even if a food source is nearby
		if (view.hasAgentInView()) // Agents in View
		{

			if (myBody.stats.getType().strongerThan(view.agentType()) == AgentEval.STRONGER) // I am predator to this prey!
			{
				if (myBody.stats.isHungry())
				{
					// Hunt this agent down
					state = AgentState.HUNT;
				}
				else
				{
					// Explore...
					state = AgentState.ROAM;
				}

			}
			else
				if (myBody.stats.getType().strongerThan(view.agentType()) == AgentEval.SAME) // This agent is equal in strength to me (same type)
				{
					if (view.hasPlantInView() && myBody.stats.getType().eatsPlants()) // Do we eat plants?
					{
						// move towards 
						state = AgentState.GRAZE;
					}
					else
					// Nope we eat other agents - but there are none...
					{
						// Roam - no food for us
						state = AgentState.ROAM;
					}
				}
				else
				// this agent is my natural predator (AgentEval.WEAKER)
				{
					// Evade - this agent will hunt me down
					state = AgentState.EVADE;
				}

		}
		else
		// No Agents in View
		{

			if (view.hasPlantInView() && myBody.stats.getType().eatsPlants()) // Do we eat plants?
			{

				if (myBody.stats.isHungry())
				{
					// move towards 
					state = AgentState.GRAZE;
				}
				else
				{
					// Explore...
					state = AgentState.ROAM;
				}

			}
			else
			// Nope we eat other agents - but there are none...
			{
				// Roam - no food for us
				state = AgentState.ROAM;
			}

		}

	}

	/* Roam State */
	private void roamState()
	{
		roamMoves++;

		if (roamMoves > roamMaxMoves) // Have i been roaming for a while...  change direction..
		{
			direction = r.nextInt(360);
			roamMoves = 0;
		}

	}

	/* Hunt State */
	private void huntState()
	{
		huntMoves++;

		if (huntMoves > huntMaxMoves) // have i been chasing for a while... maybe giveup as i dont seem to be catching this prey. -> Back to Roam mode
		{
			huntExitSubState();
		}
		else
		{
			// Set the set the direction as towards the agent
			direction = view.towardsAgentDirection(myBody);

			// Try to eat the agent
			myBody.eatAgent(view);
		}

	}

	/* Simulates tiredness */
	private void huntExitSubState()
	{
		if (huntExitWait > huntExitMaxWait)
		{
			direction = r.nextInt(360);
			huntMoves = 0;
			huntExitWait = 0;

			state = AgentState.ROAM; // Back to roaming state
		}
		else
		{
			huntExitWait++;
		}

	}

	// -- No transition... view needs to be more complex.. is there more food around...
	private void grazeState()
	{
		// -- No transition... view needs to be more complex.. is there more food around...
		direction = view.towardsPlantDirection(myBody);

		// Try to eat the plant
		myBody.eatPlant(view);

	}

	// Evade State -- No transition... view needs to be more complex.. are there other prey i could hide with... etc
	private void evadeState()
	{
		direction = view.awayfromAgentDirection(myBody);
	}

}
