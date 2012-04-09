package alife;

import org.newdawn.slick.geom.Vector2f;

import alife.SimpleAgentEnum.AgentType;


/**
 * This Class holds the representation of a view for the agent in the current simulation step.
 */
public class SimpleAgentView
{

	/** States of View */
	private boolean agentInView=false;
	private boolean plantInView=false;	
	
	/** Stats of plants and agents in view */
	private SimpleAgentViewStats inViewAgentStats;
	private GenericPlantViewStats inViewPlantStats;

	/**
	 * Creates a view.
	 */
	 public SimpleAgentView()
	 {
		 inViewAgentStats = new SimpleAgentViewStats();
		 
		 inViewPlantStats = new GenericPlantViewStats();

	 }
	 
/*
 * View Update
 * 
 */
	 /**
	  * Sets the agent in view.
	  * @param agent
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

	 /**
	  * Sets the plant in view.
	  * @param plant
	  */
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
			 return null; // To stop external code calling this explicitly with out checking if its been updated
		 }
		 return inViewAgentStats.getAgentPos();
	 }
	 
	 /** Nearest Agent Position */
	 public Vector2f getNearestPlantPos()
	 {
		 if(plantInView == false)
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

	 public SimpleAgentType agentType()
	 {
		 return inViewAgentStats.getAgentType();
	 }
	 
/* Following functions may fail if not checked by calling the above function helpers */	 
	/** Returns the direction to move in to go away from the nearest agent */ 
	public float awayfromAgentDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestAgentPos().getX() - myBody.getBodyPos().getX() ;

		float dy =  myBody.getBodyPos().getY() - getNearestAgentPos().getY();
		
		return (float) Math.toDegrees(Math.atan2(dy,dx));
	}
	
	/** Returns the direction to move in to go towards the nearest agent */
	public float towardsAgentDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestAgentPos().getX() - myBody.getBodyPos().getX() ;

		float dy =  myBody.getBodyPos().getY() - getNearestAgentPos().getY();
		
		return (float) Math.toDegrees(Math.atan2(dx,dy));
	}

	/** Returns the direction to move in to go away from the nearest plant */ 
	public float awayfromPlantDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestPlantPos().getX() - myBody.getBodyPos().getX();

		float dy =  myBody.getBodyPos().getY() - getNearestPlantPos().getY();
		
		return (float) Math.toDegrees(Math.atan2(dy,dx));
	}
	
	/** Returns the direction to move in to go towards the nearest plant */
	public float towardsPlantDirection(SimpleAgentBody myBody)
	{
		float dx = getNearestPlantPos().getX() - myBody.getBodyPos().getX();

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
	
	/** Returns the squared distances between two vectors */
	public float distanceTo(Vector2f from,Vector2f posTo)
	{	
		return from.distanceSquared(posTo);
	}
	
	/** Not to be called by agents directly */
	public GenericPlant getOriginalPlantRef()
	{
		return inViewPlantStats.getOriginalPlantRef();
	}
	
	public SimpleAgent getOriginalAgentRef()
	{
		return inViewAgentStats.getOriginalAgentRef();
	}
}
