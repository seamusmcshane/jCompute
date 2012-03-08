package alife;

import org.newdawn.slick.geom.Vector2f;

import alife.SimulationEnums.AgentType;

/** Used to store the "visible" statistics of the inViewAgent */
public class SimpleAgentViewStats
{
	/** Size of Agent in view */
	private float size;	
	
	/** Position of the Agent in view (Cartesian) */
	private Vector2f agentPos;
	
	/** Type of Agent in view */
	private SimpleAgentType type;
	
	private SimpleAgent original_agent;
	
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
		original_agent = agent;
		
		// Copies the Agent Positon
		agentPos.set(agent.body.getBodyPos());
		
		this.size = agent.body.stats.getSize();	
		
		this.type = agent.body.stats.getType();
	}	
	
/**
 * 
 * Initialization 
 */
	public void initStats()
	{
		original_agent = null;
		
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
		original_agent = null;
		
		/** Size of Agent in view */
		this.size=0;

		/** Position of the Agent in view (Cartesian) */
		agentPos.set(0, 0);
		
		this.type = null;

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
	
	public SimpleAgentType getAgentType()
	{
		return type;
	}
	
	/** A reference to the original agent that this view is about - Must not be used by agents themselves */
	public SimpleAgent getOriginalAgentRef()
	{
		return original_agent;
	}
	
}
