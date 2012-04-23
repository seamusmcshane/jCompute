package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
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
	
	/** Agent View Range */
	private Circle fov;	
		
	/**
	 * Creates a new agent.
	 * @param uid
	 * @param x
	 * @param y
	 * @param stats
	 */
	public SimpleAgent(int uid,float x,float y,SimpleAgentStats stats)
	{
		this.uid = uid;
											
		addAgentBody(new Vector2f(x,y),stats);	
		
		addAgentBrain();
		
		setUpView();	
		
	}
	
	/** 
	 * Generates the agents View representation 
	 */
	private void setUpView()
	{				
		fov = new Circle(body.getBodyPos().getX(),body.getBodyPos().getY(),body.stats.getViewRange());
	}

	/** 
	 * Updates the location of the representation of View position 
	 */
	private void upDateViewLocation()
	{
		fov.setLocation(body.getBodyPos().getX()-(body.stats.getViewRange()),body.getBodyPos().getY()-(body.stats.getViewRange()));
	}
	
	/** 
	 * Gives this agent a body with the set stats 
	 * @param pos Vector2f
	 * @param stats SimpleAgentStats
	 */
	private void addAgentBody(Vector2f pos,SimpleAgentStats stats)
	{
		body = new SimpleAgentBody(pos,stats);
	}

	/** 
	 * Gives this agent a brain 
	 */
	private void addAgentBrain()
	{
		brain = new SimpleAgentBrain(body);
	}
	
	/**
	 * Draws the agents field of view.
	 * @param g
	 */
	public void drawViewRange(Graphics g)
	{	
		upDateViewLocation();		

		g.setColor(Color.white);
		
		g.draw(fov);		
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
