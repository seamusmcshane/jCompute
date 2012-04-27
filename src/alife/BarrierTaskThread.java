package alife;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;

/**
 *  This thread object will iterate through a linked list of agents passed to it, 
 *  finding the nearest neighbor for each agent in the linked list using the KdTree pass to it.
 *  It also performs the agents step.
 *  
 *  This thread object will also iterate through a linked list of plants.
 *  This performs the plant step. ie photosynthesis 
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class BarrierTaskThread extends Thread
{

	/** The Agent list. */
	LinkedList<SimpleAgent> agentList;

	/** The Plant list. */
	LinkedList<GenericPlant> plantList;

	/** The Entire World View. (Both Trees) */
	KdTree<SimpleAgent> agentKDTree;
	KdTree<GenericPlant> plantKDTree;

	/** The Agent List Iterator. */
	ListIterator<SimpleAgent> agentListItr;

	/** The Plant List Iterator. */
	ListIterator<GenericPlant> plantListItr;

	/** The distance function object. */
	SquareEuclideanDistanceFunction distanceKD = new SquareEuclideanDistanceFunction();

	/** The agents and plants neighbor list. */
	private MaxHeap<SimpleAgent> agentNeighborList;
	private MaxHeap<GenericPlant> plantNeighborList;

	/** Reference to the current agent. */
	private SimpleAgent currentAgent;
	private GenericPlant currentPlant;

	/** Reference to the nearest agent. */
	private SimpleAgent nearestAgent;
	private GenericPlant nearestPlant;

	/** Reused Position Vector */
	private double[] pos;

	/** The start and end semaphores for this thread */
	private Semaphore start;
	private Semaphore end;

	//private int myId; // Debug

	/**
	 * Instantiates a new barrier task thread.
	 * @param id
	
	
	 * @param startSem Semaphore
	 * @param endSem Semaphore
	 */
	public BarrierTaskThread(int id, Semaphore startSem, Semaphore endSem)
	{
		//	this.myId =id;
		start = startSem;
		end = endSem;
		pos = new double[2];
	}

	/**
	 * Sets the task for this thread.
	 * @param agentList
	 * @param agentKDTree
	 * @param plantList
	 * @param plantKDTree
	 */
	public void setTask(LinkedList<SimpleAgent> agentList, KdTree<SimpleAgent> agentKDTree, LinkedList<GenericPlant> plantList, KdTree<GenericPlant> plantKDTree)
	{
		this.agentList = agentList;
		this.plantList = plantList;

		agentListItr = agentList.listIterator();
		plantListItr = plantList.listIterator();

		/* KD Trees */
		this.agentKDTree = agentKDTree;
		this.plantKDTree = plantKDTree;
	}

	/**
	 * Section 1 - Iterate over the plants.
	 * Section 2 - Iterate over the agents - generate their  views.
	 * Section 3 - Reiterate over the agents - allow them to think.
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{

		while (true)
		{

			start.acquireUninterruptibly();

			/** Section 1 - Process Plants*/
			while (plantListItr.hasNext())
			{

				currentPlant = plantListItr.next();

				if (!currentPlant.body.stats.isDead())
				{
					// Parallel plant calculations
					currentPlant.body.stats.increment();
				}

			}

			/** Section 2 */
			while (agentListItr.hasNext())
			{

				currentAgent = agentListItr.next();

				// Convert our vector to the format for the tree
				pos[0] = currentAgent.body.getBodyPos().getX();
				pos[1] = currentAgent.body.getBodyPos().getY();

				// Get two agents - due to the closest agent to its self being its self, but one plant
				agentNeighborList = agentKDTree.findNearestNeighbors(pos, 2, distanceKD);

				// Max is the next closest - Self is 0
				nearestAgent = agentNeighborList.getMax();

				/*
				 * calculate if the Nearest Agents are in the view Range of the
				 * current agent
				 */
				agentViewRangeKDSQAgents();

				if (plantKDTree.size() > 0) // Plants can die out and thus tree can be empty.. (Heap exception avoidance)
				{
					plantNeighborList = plantKDTree.findNearestNeighbors(pos, 1, distanceKD);
					nearestPlant = plantNeighborList.getMax();
					/*
					 * calculate if the Nearest Plants are in the view Range of
					 * the current agent
					 */
					agentViewRangeKDSQPlants();
				}

			}

			/* Reset the iterator reference to start form the start of the list */
			agentListItr = agentList.listIterator();

			/** Section 3 - Processing the Agent Step */
			while (agentListItr.hasNext())
			{

				currentAgent = agentListItr.next();

				// Parallel Agent Thinking
				currentAgent.brain.think();

			}

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
	private void agentViewRangeKDSQAgents()
	{
		/* Agent alone in the world */
		if (currentAgent.equals(nearestAgent))
		{
			currentAgent.brain.view.setAgentView(null);

			return;
		}

		if ((currentAgent.body.getBodyPos().distanceSquared(nearestAgent.body.getBodyPos()) - (nearestAgent.body.getTrueSizeSQRDiameter() + currentAgent.body.getTrueSizeSQRDiameter())) < currentAgent.brain.view.getFovDiameterSquared())																	// Part 3			
		{
			currentAgent.brain.view.setAgentView(nearestAgent);
			// Highlight the View Type the state machines will react similarly to this. 
			currentAgent.brain.view.setViewDrawMode(currentAgent.body.stats.getType().strongerThan(nearestAgent.body.stats.getType()));
		}
		else
		// Clear the view 
		{
			currentAgent.brain.view.setAgentView(null);

			currentAgent.brain.view.setViewDrawMode(null);
		}
	}

	/** 
	 * As above but for plants - with the logical exception that plants can be extinct and thus the list empty.
	 */
	private void agentViewRangeKDSQPlants()
	{
		if (nearestPlant == null) // All plants are extinct..
		{
			currentAgent.brain.view.setPlantView(null); // Cannot see plants
			return;
		}

		if ((currentAgent.body.getBodyPos().distanceSquared(nearestPlant.body.getBodyPos()) - (nearestPlant.body.getTrueSizeSQRDiameter() + currentAgent.body.getTrueSizeSQRDiameter())) < currentAgent.brain.view.getFovDiameterSquared())
		{
			currentAgent.brain.view.setPlantView(nearestPlant);
		}
		else
		// Clear the view 
		{
			currentAgent.brain.view.setPlantView(null); // not in range
		}
	}

}
