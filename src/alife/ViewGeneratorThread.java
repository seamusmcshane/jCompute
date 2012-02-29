package alife;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;

/**
 *  This thread object will iterate through a linked list of agents passed to it and find the nearest neighbor for each agent in the linked list using the KdTree pass to it.
 */
public class ViewGeneratorThread extends Thread
{
	
	/** The Agent list. */
	LinkedList<SimpleAgent> agentList;
	
	/** The Entire World View. */
	KdTree<SimpleAgent> worldView;
	
	/** The Agent List Iterator. */
	ListIterator<SimpleAgent> agentListItr;
	    
    /** The distance function object. */
    SquareEuclideanDistanceFunction distanceKD = new SquareEuclideanDistanceFunction();

	/** The agents neighbor list. */
	private MaxHeap<SimpleAgent> neighborlist;
	
	/** Reference to the current agent. */
	private SimpleAgent currentAgent;
	
	/** Reference to the nearest agent. */
	private SimpleAgent nearestAgent;
	
	
	/** Reused Vector */
	private double[] pos;
	
	private Semaphore mySem;
	
	/**
	 * Instantiates a new view generator thread.
	 *
	 * @param linkedList of SimpleAgents
	 * @param prTree of SimpleAgents
	 */
	public ViewGeneratorThread(Semaphore sem)
	{
		mySem = sem;
		pos = new double[2];
	}
	
	
	public void setTask(LinkedList<SimpleAgent> linkedList,	KdTree<SimpleAgent> prTree)
	{
		this.agentList = linkedList;	
		agentListItr = agentList.listIterator();
		this.worldView = prTree;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
	
		while(true)
		{
			mySem.acquireUninterruptibly();
			
			while(agentListItr.hasNext()) 
			{
				currentAgent = agentListItr.next();	
				
							
				pos[0]=currentAgent.getPos().getX();
				pos[1]=currentAgent.getPos().getY();	
				
				// Get two - due to closest agent being its self
				neighborlist = worldView.findNearestNeighbors(pos, 2, distanceKD);
				
				// Max is the next closest - Self is 0
				nearestAgent = neighborlist.getMax();
				
				distanceCalcCompareKDSQ();
			}
			
			mySem.release();
			
		}
		
	}	
	
	/** 
	 * 
	 * Part 1 : Get Squared Distance between two agents positions
	 * This distance is from the center of the agents,  
	 * 
	 * Part 2: Square the size of the bodies -
	 * We also need to subtract the square of the body sizes, so distance is on the body edges of each agent
	 *
	 * Part 3: Square the view distance (range) of the current agent
	 * 
	 * Pseudo Code : 
	 * If the distance between the closest agents surface is less than the view range of the current agent then it is in view 
	 * True - add it to the Agent to the current agents view.
	 * False- clear the view.
	 */
	public void distanceCalcCompareKDSQ() /* Public for inclusion into java doc */
	{
		/* Agent alone in the world */
		if(currentAgent.equals(nearestAgent))
		{
			currentAgent.brain.updateNearestAgentKD(null);
			
			return;
		}
		
		if( (currentAgent.getPos().distanceSquared(nearestAgent.getPos()) - 																// Part 1
				( (currentAgent.body.getSize()+currentAgent.body.getSize()) * (nearestAgent.body.getSize()+nearestAgent.body.getSize()) ) ) // Part 2
				<  ((currentAgent.body.stats.getView_range()*currentAgent.body.stats.getView_range())) )																	// Part 3			
		{
			currentAgent.brain.updateNearestAgentKD(nearestAgent);
		}
		else // Clear the view 
		{
			currentAgent.brain.updateNearestAgentKD(null);
		}
	}	
		
}
