package alife;

import java.util.Iterator;
import java.util.Random;

import org.newdawn.slick.geom.Circle;

public class SimpleAgentBrain
{

	private Iterable<SimpleAgent> viewList; /* List of Agents in view */
	private Iterator iterator;	

	/* Agent is aware of its own body */
	private SimpleAgentBody myBody;
	
	public SimpleAgentView view;
	
	/* Nearest Agent */
	//private SimpleAgent nearestAgent=null;
	
	/* Movement */
	float direction;
	Random r;
	private int moves=0;
		
	public SimpleAgentBrain(SimpleAgentBody body)
	{
		/* Agents own Body */
		myBody = body;
		
		view = new SimpleAgentView();
		
		r = new Random();
		
		direction = r.nextInt(360)+1;

	}

	/**
	 * Think.
	 */
	public void think()
	{
				
		/* Enforce 1 think/move per agent */
		boolean has_thought=false;
				
		// Random Walk
		myBody.move(r.nextInt(360)+1);

	}
	

	
}
