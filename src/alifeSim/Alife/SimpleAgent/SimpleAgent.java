package alifeSim.Alife.SimpleAgent;

import org.newdawn.slick.geom.Vector2f;
/**
 * This Class is an instantiation of an Agent.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgent
{
	/** Agent Body */
	public SimpleAgentBody body;

	/** Agent Brain */
	public SimpleAgentBrain brain;

	/** Agent Unique ID */
	private int uid;

	/**
	 * Creates a new agent.
	 * @param uid
	 * @param x
	 * @param y
	 * @param stats
	 */
	public SimpleAgent(int uid, float x, float y, SimpleAgentStats stats)
	{
		this.uid = uid;

		addAgentBody(new Vector2f(x, y), stats);

		addAgentBrain();

	}

	/** 
	 * Gives this agent a body with the set stats 
	 * @param pos Vector2f
	 * @param stats SimpleAgentStats
	 */
	private void addAgentBody(Vector2f pos, SimpleAgentStats stats)
	{
		body = new SimpleAgentBody(pos, stats);
	}

	/** 
	 * Gives this agent a brain 
	 */
	private void addAgentBrain()
	{
		brain = new SimpleAgentBrain(body);
	}

	/** 
	 * Sets a unique agent id for debug 
	 * @param id int
	 */
	public void setId(int id)
	{
		this.uid = id;
	}

	/**
	 * Returns the agent id
	 * @return int */
	public int getAgentId()
	{
		return uid;
	}

}
