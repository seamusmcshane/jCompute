package alifeSim.Scenario.SAPP;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import alifeSim.Alife.GenericPlant.GenericPlant;
import alifeSim.Alife.SimpleAgent.SimpleAgent;
import alifeSim.datastruct.knn.KNNInf;

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
	private List<SimpleAgent> agentList;

	/** The Plant list. */
	private List<GenericPlant> plantList;

	/** The Entire World View. (Both Trees) */
	private KNNInf<SimpleAgent> agentKDTree;
	private KNNInf<GenericPlant> plantKDTree;

	/** The Plant List Iterator. */
	private ListIterator<GenericPlant> plantListItr;

	/** The distance function object. */
	private final SquareEuclideanDistanceFunction distanceKD = new SquareEuclideanDistanceFunction();

	/** The agents and plants neighbor list. */
	private MaxHeap<GenericPlant> plantNeighborList;

	/** The start and end semaphores for this thread */
	private Semaphore start;
	private Semaphore end;

	private boolean running=true;
	
	// benchmark
	private boolean debugtasks=false;
	private long taskStartTime = 0;
	private long taskEndTime = 0;
	private long taskTime = 0;
	private long taskTotalTime = 0;
	private long taskAvgTime = 0;
	private long taskCount = 0;
	
	private int myId; // Debug
	
	/**
	 * Instantiates a new barrier task thread.
	 * @param id
	
	
	 * @param startSem Semaphore
	 * @param endSem Semaphore
	 */
	public BarrierTaskThread(int id, Semaphore startSem, Semaphore endSem)
	{
		super("BarrierTaskThread");
		this.myId =id;
		this.start = startSem;
		this.end = endSem;
	}

	/**
	 * Sets the task for this thread.
	 * @param agentList
	 * @param agentKDTree
	 * @param plantList
	 * @param plantKDTree
	 */
	public void setTask(List<SimpleAgent> agentList, KNNInf<SimpleAgent> agentKDTree, List<GenericPlant> plantList, KNNInf<GenericPlant> plantKDTree)
	{
		this.agentList = agentList;
		this.plantList = plantList;
		
		/*System.out.println("plantList " + plantList.size());
		System.out.println("plantKDTree " + plantKDTree.size());*/
		
		//this.agentListItr = agentList.listIterator();
		//this.plantListItr = plantList.listIterator();

		/* KD Trees */
		this.agentKDTree = agentKDTree;
		this.plantKDTree = plantKDTree;
	}

	/** 
	 * Initiates a thread shutdown.
	 */
	public void exitThread()
	{
		/* Trigger the exit flag */
		running=false;
		
		/* Let the thread run so it exits */
		start.release();
	}
	
	/**
	 * Section 1 - Iterate over the plants.
	 * Section 2 - Iterate over the agents - generate their  views.
	 * Section 3 - Reiterate over the agents - allow them to think.
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		System.out.println("Created Task Thread : " + myId);

		while (running)
		{
			start.acquireUninterruptibly();
			
			if(running)
			{
				/* benchmarking */
				if(debugtasks)
				{
					taskStartTime = System.currentTimeMillis(); // Start time for the average step	
				}
				
				if(plantList!=null)
				{
					/** Section 1 - Process Plants*/
					for (GenericPlant currentPlant : plantList) 
					{	
						if (!currentPlant.body.stats.isDead())
						{
							// Parallel plant calculations
							currentPlant.body.stats.increment();
						}	
					}
				}
				/* Reset the iterator reference to start form the start of the list */
				//agentListItr = agentList.listIterator();				
				/** Section 2 */
				if(agentList!=null)
				{
					for (SimpleAgent currentAgent : agentList) 
					{
							
						SimpleAgent nearestAgent = agentKDTree.nearestNNeighbour(currentAgent.body.getBodyPosKD(),2);
		
						/*
						 * calculate if the Nearest Agents are in the view Range of the
						 * current agent
						 */
						agentViewRangeKDSQAgents(currentAgent,nearestAgent);
		
						if (plantKDTree.size() > 0) // Plants can die out and thus tree can be empty.. (Heap exception avoidance)
						{
							//plantNeighborList = plantKDTree.findNearestNeighbors(currentAgent.body.getBodyPosKD(), 1, distanceKD);
							//nearestPlant = plantNeighborList.getMax();
							GenericPlant nearestPlant = plantKDTree.nearestNeighbour(currentAgent.body.getBodyPosKD());
													
							/*
							 * calculate if the Nearest Plants are in the view Range of
							 * the current agent
							 */
							agentViewRangeKDSQPlants(currentAgent,nearestPlant);
						}
					}
	
					/* Reset the iterator reference to start form the start of the list */
					//agentListItr = agentList.listIterator();
					
					for (SimpleAgent currentAgent : agentList) 
					{
		
						// Parallel Agent Thinking
						currentAgent.brain.think();
					}
				}
				/* benchmarking */
				if(debugtasks)
				{
					taskEndTime = System.currentTimeMillis(); // Start time for the average step	
					taskTime = taskEndTime - taskStartTime;
					taskTotalTime +=taskTime;
					taskCount++;
					taskAvgTime = taskTotalTime/taskCount;
				}				
				
			}

			end.release();

		}
		System.out.println("Exited Thread : " + myId);
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
	private void agentViewRangeKDSQAgents(SimpleAgent currentAgent,SimpleAgent nearestAgent)
	{
		
		/*System.out.println("Current Agent :" + currentAgent );

		System.out.println("Nearest Agent :" + nearestAgent );*/

		/* Agent alone in the world */
		if (currentAgent.equals(nearestAgent))
		{
			currentAgent.brain.view.setAgentView(null);

			return;
		}
				
		/*System.out.print("Ax "+ currentAgent.body.getBodyPos().getX()+ " Ay " +currentAgent.body.getBodyPos().getY());
		System.out.print(":");
		System.out.print("Nx "+ nearestAgent.body.getBodyPos().getX()+ " Ny " +nearestAgent.body.getBodyPos().getY());
		System.out.println("");
		System.out.println("Distance SQ " + (currentAgent.body.getBodyPos().distanceSquared(nearestAgent.body.getBodyPos())));
		System.out.println("Body Sizes" + (nearestAgent.body.getTrueSizeSQRDiameter() + currentAgent.body.getTrueSizeSQRDiameter()));
		System.out.println("FOV " + currentAgent.brain.view.getFovDiameterSquared());*/		
		
		if ( (currentAgent.body.getBodyPos().distanceSquared(nearestAgent.body.getBodyPos()) 				  // Part 1
				- (nearestAgent.body.getTrueSizeSQRDiameter() + currentAgent.body.getTrueSizeSQRDiameter()) ) // Part 2
				< currentAgent.brain.view.getFovDiameterSquared())											  // Part 3			
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
	private void agentViewRangeKDSQPlants(SimpleAgent currentAgent,GenericPlant nearestPlant)
	{
		if (nearestPlant == null) // All plants are extinct..
		{
			currentAgent.brain.view.setPlantView(null); // Cannot see plants
			return;
		}

		/*System.out.print("Ax "+ currentAgent.body.getBodyPos().getX()+ " Ay " +currentAgent.body.getBodyPos().getY());
		System.out.print(":");
		System.out.print("Px "+ nearestPlant.body.getBodyPos().getX()+ " Py " +nearestPlant.body.getBodyPos().getY());
		System.out.println("");
		System.out.println("Distance SQ " + (currentAgent.body.getBodyPos().distanceSquared(nearestPlant.body.getBodyPos())));
		System.out.println("Body Sizes" + (nearestPlant.body.getTrueSizeSQRDiameter() + currentAgent.body.getTrueSizeSQRDiameter()));
		System.out.println("FOV " + currentAgent.brain.view.getFovDiameterSquared());*/
		
		if ( (currentAgent.body.getBodyPos().distanceSquared(nearestPlant.body.getBodyPos()) 				  // Part 1
				- (nearestPlant.body.getTrueSizeSQRDiameter() + currentAgent.body.getTrueSizeSQRDiameter()) ) // Part 2
				< currentAgent.brain.view.getFovDiameterSquared())											  // Part 3			
		{
			currentAgent.brain.view.setPlantView(nearestPlant);
		}
		else // Clear the view 
		{
			currentAgent.brain.view.setPlantView(null); // not in range
		}		
		
	}
	
	public void printDebugStats()
	{		
		if(debugtasks)
		{
			System.out.println("Task ID " + myId + " Total Time :" + taskTotalTime);
			System.out.println("Task ID " + myId + " Task Count :" + taskCount);
			System.out.println("Task ID " + myId + " Avg Time   :" + taskAvgTime);
		}


	}

}
