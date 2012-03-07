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
	
    boolean hasThought=false;
	
	/* Movement */
	float direction;
	Random r;
	private int moves=0;
	private int chase_moves;
	private int chase_max_moves;
	
	/* Move counters */		
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
		hasThought=false;
				
		myBody.move(r.nextInt(360)+1);

	}

	private void chaseState()
	{
		
	}
	
	private void restState()
	{
		
	}
	
	private void roamState()
	{
		
	}
	
	
}
