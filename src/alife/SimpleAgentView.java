package alife;

import org.newdawn.slick.geom.Vector2f;


/*
 * Class will hold all the possible stats and info an agent can see about another Agent
 * -Template-
 */
public class SimpleAgentView
{

	boolean agentInView=false;
	
	 Vector2f body_pos;
	
	 public SimpleAgentView()
	 {
		 body_pos = new Vector2f();
	 }
	 
	 public void setView(SimpleAgent agent)
	 {
		 if(agent != null)  // Agent is in view
		 {
			 agentInView=true;
			 
			 // Copy stats
		 }
		 else  // No agent in View
		 {
			 agentInView=false; 
			 
			 // clear stats
		 }
	 }
	 
	 public Vector2f getNearestAgentPos()
	 {
		 if(agentInView == false)
		 {
			 return null;
		 }
		 return body_pos;
	 }
}
