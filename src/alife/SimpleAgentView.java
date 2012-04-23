package alife;

import org.newdawn.slick.geom.Vector2f;

/**
 * This Class holds the representation of a view for the agent in the current simulation step.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentView
{

	/** States of View */
	private boolean agentInView = false;
	private boolean plantInView = false;

	/** Statistics of plants and agents in view */
	private SimpleAgentViewStats inViewAgentStats;
	private GenericPlantViewStats inViewPlantStats;

	/**
	 * Creates an agent view.
	 */
	public SimpleAgentView()
	{
		inViewAgentStats = new SimpleAgentViewStats();

		inViewPlantStats = new GenericPlantViewStats();

	}

	/**
	 * Sets the agent in view.
	 * @param agent
	 */
	public void setAgentView(SimpleAgent agent)
	{
		if (agent != null)  // Agent is in view
		{
			agentInView = true;

			// Copy stats
			inViewAgentStats.updateStats(agent);

		}
		else
		// No agent in View
		{
			agentInView = false;

			// clear stats
			inViewAgentStats.clearStats();
		}
	}

	/**
	 * Sets the plant in view.
	 * @param plant
	 */
	public void setPlantView(GenericPlant plant)
	{

		if (plant != null)
		{
			plantInView = true;

			// Copy stats
			inViewPlantStats.updateStats(plant);
		}
		else
		{
			plantInView = false;

			// clear stats
			inViewPlantStats.clearStats();
		}

	}

	/** 
	* Nearest Agent Position 
	* @return Vector2f */
	public Vector2f getNearestAgentPos()
	{
		if (!agentInView)
		{
			return null; // To stop external code calling this explicitly with out checking if its been updated
		}
		return inViewAgentStats.getAgentPos();
	}

	/** Nearest Agent Position 
	* @return Vector2f
	*/
	public Vector2f getNearestPlantPos()
	{
		if (!plantInView)
		{
			return null;
		}
		return inViewPlantStats.getPlantPos();
	}

	/** 
	* Status of View 
	* @return boolean */
	public boolean hasAgentInView()
	{
		return agentInView;
	}

	/** 
	* Status of View
	* @return boolean
	*/
	public boolean hasPlantInView()
	{
		return plantInView;
	}

	/**
	 * Method agentType.
	 * @return SimpleAgentType */
	public SimpleAgentType agentType()
	{
		return inViewAgentStats.getAgentType();
	}

	/*
	 * Following functions may fail if not checked by calling the above function
	 * helpers
	 */

	/** Returns the direction to move in to go away from the nearest agent
	* @param myBody SimpleAgentBody
	* @return float */
	public float awayfromAgentDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestAgentPos().getX() - myBody.getBodyPos().getX();

		float dy = myBody.getBodyPos().getY() - getNearestAgentPos().getY();

		return (float) Math.toDegrees(Math.atan2(dy, dx));
	}

	/** Returns the direction to move in to go towards the nearest agent
	 * @param myBody SimpleAgentBody
	 * @return float */
	public float towardsAgentDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestAgentPos().getX() - myBody.getBodyPos().getX();

		float dy = myBody.getBodyPos().getY() - getNearestAgentPos().getY();

		return (float) Math.toDegrees(Math.atan2(dx, dy));
	}

	/** Returns the direction to move in to go away from the nearest plant
	 * @param myBody SimpleAgentBody
	 * @return float */
	public float awayfromPlantDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestPlantPos().getX() - myBody.getBodyPos().getX();

		float dy = myBody.getBodyPos().getY() - getNearestPlantPos().getY();

		return (float) Math.toDegrees(Math.atan2(dy, dx));
	}

	/** Returns the direction to move in to go towards the nearest plant
	 * @param myBody SimpleAgentBody
	 * @return float */
	public float towardsPlantDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestPlantPos().getX() - myBody.getBodyPos().getX();

		float dy = myBody.getBodyPos().getY() - getNearestPlantPos().getY();

		return (float) Math.toDegrees(Math.atan2(dx, dy));
	}

	/** Reverses the angle of the direction  
	 * @param direction float
	 * @return float */
	public float reverseDirection(float direction)
	{
		direction = direction - 180;

		if (direction < 0)
		{
			direction = direction + 360;
		}
		return direction;
	}

	/** Returns the squared distances between two vectors
	 * @param from Vector2f
	 * @param posTo Vector2f
	 * @return float */
	public float distanceTo(Vector2f from, Vector2f posTo)
	{
		return from.distanceSquared(posTo);
	}

	/** Not to be called by agents directly
	 * @return GenericPlant
	 */
	public GenericPlant getOriginalPlantRef()
	{
		return inViewPlantStats.getOriginalPlantRef();
	}

	/**
	 * Method getOriginalAgentRef.
	 * @return SimpleAgent
	 */
	public SimpleAgent getOriginalAgentRef()
	{
		return inViewAgentStats.getOriginalAgentRef();
	}
}
