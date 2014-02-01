package alifeSim.Alife.SimpleAgent;

import alifeSimGeom.A2DVector2f;

/** Used to store the "visible" statistics of the inViewAgent
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentViewStats
{
	/** Size of Agent in view */
	private float size;

	/** Position of the Agent in view (Cartesian) */
	private A2DVector2f agentPos;

	/** Type of Agent in view */
	private SimpleAgentType type;

	private SimpleAgent originalAgent;

	/**
	 * A class that represents the statistics on a view of a SimpleAgent 
	 */
	public SimpleAgentViewStats()
	{
		initStats();
	}

	/**
	 * Update Statistics
	 * @param agent SimpleAgent
	 */
	public void updateStats(SimpleAgent agent)
	{
		originalAgent = agent;

		// Copies the Agent Positon
		agentPos.set(agent.body.getBodyPos());

		this.size = agent.body.stats.getSize();

		this.type = agent.body.stats.getType();
	}

	/**
	 * Initialization 
	 */
	public void initStats()
	{
		originalAgent = null;

		/** Size of Agent in view */
		this.size = 0;

		/** Position of the Agent in view (Cartesian) */
		agentPos = new A2DVector2f();

	}

	/**
	 * Clear Statistics
	 */
	public void clearStats()
	{
		originalAgent = null;

		/** Size of Agent in view */
		this.size = 0;

		/** Position of the Agent in view (Cartesian) */
		agentPos.set(0, 0);

		this.type = null;

	}

	/** Position of the Agent in view (Cartesian) 
	 * * @return Vector2f */
	public A2DVector2f getAgentPos()
	{
		return agentPos;
	}

	/** Size of Agent in view 
	 * @return float */
	public float getAgentSize()
	{
		return size;
	}

	/**
	 * Method getAgentType.
	 * @return SimpleAgentType */
	public SimpleAgentType getAgentType()
	{
		return type;
	}

	/** A reference to the original agent that this view is about - Must not be used by agents themselves 
	 * @return SimpleAgent */
	public SimpleAgent getOriginalAgentRef()
	{
		return originalAgent;
	}

}
