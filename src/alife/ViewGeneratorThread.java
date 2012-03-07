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
	
	/** The Entire World View. (Both Trees) */
	KdTree<SimpleAgent> agentKDTree;
	KdTree<GenericPlant> plantKDTree;
	
	/** The Agent List Iterator. */
	ListIterator<SimpleAgent> agentListItr;
	    
    /** The distance function object. */
    SquareEuclideanDistanceFunction distanceKD = new SquareEuclideanDistanceFunction();

	/** The agents and plants neighbor list. */
	private MaxHeap<SimpleAgent> agentNeighborList;
	private MaxHeap<GenericPlant>plantNeighborList;
	
	/** Reference to the current agent. */
	private SimpleAgent currentAgent;
	
	/** Reference to the nearest agent. */
	private SimpleAgent nearestAgent;
	private GenericPlant nearestPlant;
		
	/** Reused Vector */
	private double[] pos;
	
	private Semaphore start;
	private Semaphore end;
	
	private int my_id;
	
	/**
	 * Instantiates a new view generator thread.
	 *
	 * @param linkedList of SimpleAgents
	 * @param prTree of SimpleAgents
	 */
	public ViewGeneratorThread(int id,Semaphore sem1,Semaphore sem2)
	{
		this.my_id =id;
		start = sem1;
		end = sem2;
		pos = new double[2];
	}
	
	
	public void setTask(LinkedList<SimpleAgent> linkedList,	KdTree<SimpleAgent> agentKDTree,KdTree<GenericPlant> plantKDTree)
	{
		this.agentList = linkedList;	
		
		agentListItr = agentList.listIterator();
		
		/* KD Trees */
		this.agentKDTree = agentKDTree;
		this.plantKDTree = plantKDTree;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
	
		while(true)
		{
			start.acquireUninterruptibly();
			
			//System.out.println("->Start : " + my_id);
			
			while(agentListItr.hasNext()) 
			{
								
				currentAgent = agentListItr.next();				
				
				// Convert our vector to the format for the tree
				pos[0]=currentAgent.body.getBodyPos().getX();
				pos[1]=currentAgent.body.getBodyPos().getY();	
				
				// Get two agents - due to the closest agent to its self being its self, but one plant
				agentNeighborList = agentKDTree.findNearestNeighbors(pos, 2, distanceKD);
				plantNeighborList = plantKDTree.findNearestNeighbors(pos, 1, distanceKD);

				
				// Max is the next closest - Self is 0
				nearestAgent = agentNeighborList.getMax();
				nearestPlant = plantNeighborList.getMax();
				
				/* calculate if the Nearest Agents and Plants are in the view Range of the current agent */ 
				agentViewRangeKDSQAgents();
				agentViewRangeKDSQPlants();
				
				// Parallel Agent Thinking
				currentAgent.brain.think(); // TODO Need to add new object or later agents will be operating on the future move rather than the current move

			}
			
			//System.out.println("->End : " + my_id);
			
			end.release();		
						
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
	public void agentViewRangeKDSQAgents() /* Public for inclusion into java doc */
	{
		/* Agent alone in the world */
		if(currentAgent.equals(nearestAgent))
		{
			currentAgent.brain.view.setAgentView(null);
			
			return;
		}
		
		if( (currentAgent.body.getBodyPos().distanceSquared(nearestAgent.body.getBodyPos()) - 																// Part 1
				( (currentAgent.body.stats.getSize()+currentAgent.body.stats.getSize()) * (nearestAgent.body.stats.getSize()+nearestAgent.body.stats.getSize()) ) ) // Part 2
				<  ((currentAgent.body.stats.getView_range()*currentAgent.body.stats.getView_range())) )																	// Part 3			
		{
			currentAgent.brain.view.setAgentView(nearestAgent);
		}
		else // Clear the view 
		{
			currentAgent.brain.view.setAgentView(null);
		}
	}	
		
	// As above but for plants - with the logical exception that plants can be extinct.
	public void agentViewRangeKDSQPlants()
	{
		if(nearestPlant == null) // All plants are extinct..
		{
			currentAgent.brain.view.setPlantView(null); // Cannot see plants
			return;
		}
		
		if( (currentAgent.body.getBodyPos().distanceSquared(nearestPlant.body.getBodyPos()) - 																// Part 1
				( (currentAgent.body.stats.getSize()+currentAgent.body.stats.getSize()) * (nearestPlant.body.stats.getSize()+nearestPlant.body.stats.getSize()) ) ) // Part 2
				<  ((currentAgent.body.stats.getView_range()*currentAgent.body.stats.getView_range())) )																	// Part 3			
		{
			currentAgent.brain.view.setPlantView(nearestPlant);
		}
		else // Clear the view 
		{
			currentAgent.brain.view.setPlantView(null); // not in range
		}	
	}
	
}
