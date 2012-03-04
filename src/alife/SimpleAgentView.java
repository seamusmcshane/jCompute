package alife;

import org.newdawn.slick.geom.Vector2f;


/*
 * Manages State of the view
 */
public class SimpleAgentView
{

	/** State of View */
	private boolean agentInView;
	
	/** Stats of agent in view */
	private SimpleAgentViewStats inViewAgentStats;
	
	/** A class that manages the view of the agent */
	 public SimpleAgentView()
	 {
		 inViewAgentStats = new SimpleAgentViewStats();
		 agentInView=false;
	 }
	 
/**
 * View Update
 * 
 * Decides State of View
 */
	 public void setView(SimpleAgent agent)
	 {
		 if(agent != null)  // Agent is in view
		 {
			 agentInView=true;
			 		 
			 // Copy stats
			 inViewAgentStats.updateStats(agent);
			 
		 }
		 else  // No agent in View
		 {
			 agentInView=false; 

			 // clear stats
			 inViewAgentStats.clearStats();			 
		 }
	 }

/*
 * View Getters
 * 	 
 */
	 /** Nearest Agent Position */
	 public Vector2f getNearestAgentPos()
	 {
		 if(agentInView == false)
		 {
			 return null;
		 }
		 return inViewAgentStats.getAgentPos();
	 }
	 
	 /** Status of View */
	 public boolean hasAgentInView()
	 {
		 return agentInView;
	 }
	 
	/** Returns the direction to move in to go away from the nearest agent */ 
	public float awayfromAgentDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestAgentPos().getX()-myBody.getBodyPos().getX() ;

		float dy =  myBody.getBodyPos().getY() - getNearestAgentPos().getY();
		
		return (float) Math.toDegrees(Math.atan2(dy,dx));
	}
	
	/** Returns the direction to move in to go towards the nearest agent */
	public float towardsAgentDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestAgentPos().getX()-myBody.getBodyPos().getX() ;

		float dy =  myBody.getBodyPos().getY() - getNearestAgentPos().getY();
		
		return (float) Math.toDegrees(Math.atan2(dx,dy));
	}
	
	/** Reverses the angle of the direction  */
	public float reverseDirection(float direction)
	{
		direction = direction-180;

		if(direction<0)
		{
			direction=direction+360;
		}
		return direction;
	}
}
