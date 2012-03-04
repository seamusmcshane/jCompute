package alife;

import org.newdawn.slick.geom.Vector2f;

/** Used to store the "visible" statistics of the inViewAgent */
public class SimpleAgentViewStats
{
	/** Size of Agent in view */
	private float size;	
	
	/** Position of the Agent in view (Cartesian) */
	Vector2f agentPos;
	
	/**
	 * A class that represents the statistics on a view of a SimpleAgent 
	 */
	public SimpleAgentViewStats()
	{
		initStats();
	}
	
/**
 * Update Statistics
 * 
 */
	public void updateStats(SimpleAgent agent)
	{
		// Copies the Agent Positon
		agentPos.set(agent.body.getBodyPos());
		
		this.size = agent.body.getSize();	
	}	
	
/**
 * 
 * Initialization 
 */
	public void initStats()
	{
		/** Size of Agent in view */
		this.size=0;

		/** Position of the Agent in view (Cartesian) */
		agentPos = new Vector2f();		
		
	}

/**
 * Clear Statistics
 * 
 */
	public void clearStats()
	{
		/** Size of Agent in view */
		this.size=0;

		/** Position of the Agent in view (Cartesian) */
		agentPos.set(0, 0);

	}
	
/**
 * Statistics Getters
 * 
 */
	/** Position of the Agent in view (Cartesian) */
	public Vector2f getAgentPos()
	{
		return agentPos;
	}

	/** Size of Agent in view */
	public float getAgentSize()
	{
		return size;
	}
	
}
