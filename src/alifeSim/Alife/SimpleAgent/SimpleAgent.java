package alifeSim.Alife.SimpleAgent;

import alifeSim.World.WorldInf;
import alifeSimGeom.A2DVector2f;
/**
 * This Class is an instantiation of an Agent.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgent
{
	/** Agent Body */
	public final SimpleAgentBody body;

	/** Agent Brain */
	public final SimpleAgentBrain brain;

	/** Agent Unique ID */
	private int uid;

	/**
	 * Creates a new agent.
	 * @param uid
	 * @param x
	 * @param y
	 * @param stats
	 */
	public SimpleAgent(WorldInf world,int uid, float x, float y,SimpleAgentStats stats)
	{
		this.uid = uid;

		body = new SimpleAgentBody(world,new A2DVector2f(x, y), stats);

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
