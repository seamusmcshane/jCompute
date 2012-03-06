package alife;

import org.newdawn.slick.geom.Vector2f;


/*
 * Manages State of the view
 */
public class SimpleAgentView
{

	/** States of View */
	private boolean agentInView;
	private boolean plantInView;
	
	
	/** Stats of agent in view */
	private SimpleAgentViewStats inViewAgentStats;
	private GenericPlantViewStats inViewPlantStats;
	
	/** A class that manages the view of the agent */
	 public SimpleAgentView()
	 {
		 inViewAgentStats = new SimpleAgentViewStats();
		 
		 inViewPlantStats = new GenericPlantViewStats();
		 
		 agentInView=false;
		 
		 plantInView=false;
	 }
	 
/**
 * View Update
 * 
 * Decides State of View
 */
	 public void setAgentView(SimpleAgent agent)
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

	 public void setPlantView(GenericPlant plant)
	 {
	 
		 if(plant != null)
		 {
			 plantInView=true;
	 		 
			 // Copy stats
			 inViewPlantStats.updateStats(plant);			 
		 }
		 else
		 {
			 plantInView=false; 

			 // clear stats
			 inViewPlantStats.clearStats();				 
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
	 
	 /** Nearest Agent Position */
	 public Vector2f getNearestPlantPos()
	 {
		 if(agentInView == false)
		 {
			 return null;
		 }
		 return inViewPlantStats.getPlantPos();
	 }
	 
	 /** Status of View */
	 public boolean hasAgentInView()
	 {
		 return agentInView;
	 }

	 /** Status of View */
	 public boolean hasPlantInView()
	 {
		 return plantInView;
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

	/** Returns the direction to move in to go away from the nearest plant */ 
	public float awayfromPlantDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestPlantPos().getX()-myBody.getBodyPos().getX() ;

		float dy =  myBody.getBodyPos().getY() - getNearestPlantPos().getY();
		
		return (float) Math.toDegrees(Math.atan2(dy,dx));
	}
	
	/** Returns the direction to move in to go towards the nearest plant */
	public float towardsPlantDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestPlantPos().getX()-myBody.getBodyPos().getX() ;

		float dy =  myBody.getBodyPos().getY() - getNearestPlantPos().getY();
		
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
