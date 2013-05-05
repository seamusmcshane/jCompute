package alifeSim.Simulation;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import alifeSim.Alife.GenericPlant.GenericPlant;
import alifeSim.Alife.SimpleAgent.SimpleAgent;
import alifeSim.datastruct.knn.KDTree;
import alifeSim.datastruct.knn.KNNInf;
import alifeSim.datastruct.knn.thirdGenKDWrapper;
import alifeSim.datastruct.list.ArrayList;
/**
 * 
 * This class instantiates a barrier manager.
 * Its task is to generate plant and agent kd trees.
 * Then divide the lists of agents and plants in to smaller lists to be processed in threads.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class BarrierManager extends Thread
{
	/** Number of threads used in this barrier */
	private final int numThreads;

	/* The lock for the entire barrier */
	private Semaphore barrierControllerSemaphore;

	/** The start semaphores for the threads */
	private Semaphore barrierManagerStartSemaphores[];

	/** The end semaphores for the threads */
	private Semaphore barrierManagerEndSemaphores[];

	/** The array of threads */
	private BarrierTaskThread barrierThreads[];

	/** References for Agent Tasks */
	//private KdTree<SimpleAgent> agentKDTree;
	private KNNInf<SimpleAgent> agentKDTree;
	
	private ArrayList<SimpleAgent>agentList;
	private ArrayList<SimpleAgent>[] agentTaskLists;

	/** References for Plant Tasks */
	private KNNInf<GenericPlant> plantKDTree;
	private ArrayList<GenericPlant> plantList;
	private ArrayList<GenericPlant>[] plantTaskLists;
	
	/** Counts used in list division */
	private int agentCount;
	private int plantCount;
	
	private boolean running=true;
	
	/** List Split iterator */
	ListIterator<GenericPlant> splitItr;

	/**
	 * Creates a new barrier manager.
	 * @param barrierControllerSemaphore
	 * @param numThreads
	 */
	public BarrierManager(Semaphore barrierControllerSemaphore, int numThreads)
	{
		this.barrierControllerSemaphore = barrierControllerSemaphore;

		this.numThreads = numThreads;
		
		//agentKDTree = new KDTree<SimpleAgent>();
		//plantKDTree = new KDTree<GenericPlant>(2);
		//agentKDTree = new thirdGenKDWrapper<SimpleAgent>(2);
		//plantKDTree = new thirdGenKDWrapper<GenericPlant>(2);		
		
		setUpTaskLists();

		setUpSemaphores();

		setUpThreads();

	}

	/**
	 * The barrier manager runs in its own thread. 
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		System.out.println("Barrier Manager Started");
		
		while (running)
		{

			barrierControllerSemaphore.acquireUninterruptibly();
			
			if(running)
			{
				//System.out.println("Start Barrier");
				
				// Creates the plant kd tree and divides plant list in to sub lists
				splitPlantList();
	
				// Creates the agent kd tree and divides plant list in to sub lists
				splitAgentList();
	
				// set the tasks
				setbarrierThreadsTasks();
	
				// Pull down the barrier / Release the threads
				releaseThreadBarrier();
	
				/* Prepare the barrier */
				waitThreadsEnd();
	
				//System.out.println("End Barrier");			
			}			

			barrierControllerSemaphore.release();

		}
		System.out.println("Exited Barrier");			

	}

	/**
	 * Initates the thread clean up and exits the barrier.
	 */
	public void cleanUp()
	{

		
		for(int i=0;i<barrierThreads.length;i++)
		{
			/* Exit the threads */
			barrierThreads[i].exitThread();
			
			/* Set them to null so the garbage collector can get to work */
			barrierThreads[i]=null;
		}		
		
		/* Trigger the barrier exit flag */
		running=false;
		
		/* Let the thread run for the final time */
		barrierControllerSemaphore.release();

	}
	
	/**
	 * Updates the barrier with a new list of agents to process.
	 * @param inList
	 * @param numAgents int
	 */
	public void setBarrierAgentTask(ArrayList<SimpleAgent> inList, int numAgents)
	{
		agentList = inList;
		agentCount = numAgents;
	}

	/** 
	 * Updates the barrier with a new list of plants to process.
	 * @param inList
	 * @param numPlants int
	 */
	public void setBarrierPlantTask(ArrayList<GenericPlant> inList, int numPlants)
	{
		plantList = inList;
		plantCount = numPlants;
	}

	/**
	 * Releases the threads to begin processing
	 */
	private void releaseThreadBarrier()
	{
		// Ensure all threads are at the barrier
		int i;

		for (i = 0; i < numThreads; i++)
		{
			barrierManagerStartSemaphores[i].release();
		}

	}

	/**
	 * The Barrier will wait in this method until all threads are finished
	 */
	private void waitThreadsEnd()
	{
		// Ensure all threads are at the barrier
		int i;
		for (i = 0; i < numThreads; i++)
		{
			barrierManagerEndSemaphores[i].acquireUninterruptibly();
		}

	}

	/**
	 * Sets the references in the threads to the sub lists they need to process.
	 */
	private void setbarrierThreadsTasks()
	{
		for (int i = 0; i < numThreads; i++)
		{
			barrierThreads[i].setTask(agentTaskLists[i], agentKDTree, plantTaskLists[i], plantKDTree);
		}
	}

	/**
	 * Initializes the Linked Lists.
	 */
	@SuppressWarnings("unchecked")
	private void setUpTaskLists()
	{
		agentTaskLists = new ArrayList[numThreads];
		plantTaskLists = new ArrayList[numThreads];

	}

	/** 
	 * Initializes the internal barrier semaphores.
	 */
	private void setUpSemaphores()
	{
		barrierManagerStartSemaphores = new Semaphore[numThreads];

		barrierManagerEndSemaphores = new Semaphore[numThreads];

		for (int i = 0; i < numThreads; i++)
		{
			barrierManagerStartSemaphores[i] = new Semaphore(0, true);
			barrierManagerEndSemaphores[i] = new Semaphore(0, true);
		}
	}

	/**
	 * Initializes the barrier threads with their semaphores.
	 */
	private void setUpThreads()
	{
		barrierThreads = new BarrierTaskThread[numThreads];

		for (int i = 0; i < numThreads; i++)
		{
			barrierThreads[i] = new BarrierTaskThread(i, barrierManagerStartSemaphores[i], barrierManagerEndSemaphores[i]);
			barrierThreads[i].start();
		}
	}

	/**
	 * 1) Generates the plant KD tree using the list.
	 * 2) Splits the large linked list into (n) smaller linked list.
	 */
	private void splitPlantList()
	{
		/* 2d - KD-Tree */
		plantKDTree = new thirdGenKDWrapper<GenericPlant>(2);		
		//plantKDTree = new KDTree<GenericPlant>(2);

		int i = 0;

		/* Create a list for each thread and the thread */
		for (i = 0; i < numThreads; i++)
		{
			plantTaskLists[i] = new ArrayList<GenericPlant>();
		}

		//splitItr = plantList.listIterator();
		
		plantList.resetHead();
		


		/* Calculate the Splits */
		int div = plantCount / numThreads;
		int split = div;

		int thread_num = 0;
		int tPlantCount = 0;

		/* Temp Variable */
		GenericPlant temp;
						
		/* Vector */
		double[] pos;
		
		/*GenericPlant median = plantList.getMedianNode();
		
		// Add Median to Tree
		pos = new double[2];
		pos[0] = median.body.getBodyPos().getX();
		pos[1] = median.body.getBodyPos().getY();
		plantKDTree.add(pos, median);		*/
		
		/* Split the lists */
		while (plantList.hasNext())
		{
			/* Get a plant */
			temp = plantList.getNext();

			/*if(temp == median)
			{ // dont do this iteration
				continue;
			}*/
			
			/* This Section adds each plant and its coordinates to the kd tree */
			{
				pos = new double[2];
				pos[0] = temp.body.getBodyPos().getX();
				pos[1] = temp.body.getBodyPos().getY();
				plantKDTree.add(pos, temp);
			}

			/* This section does the decision boundaries for splitting the list */
			if (tPlantCount == split)
			{
				if (thread_num < (numThreads - 1))
				{
					split = split + div;
					thread_num++;
				}
			}

			/* Add the plant to the smaller list */
			plantTaskLists[thread_num].add(temp,temp.body.getBodyPos().getX());

			tPlantCount++;
		}
		/* Lists are now Split */

	}

	/**
	 * 1) Generates the agent KD tree using the list.
	 * 2) Splits the large linked list into (n) smaller linked list.
	 */
	private void splitAgentList()
	{
		/* 2d - KD-Tree */
		agentKDTree = new thirdGenKDWrapper<SimpleAgent>(2);

		int i = 0;

		/* Calculate the Splits */
		int div = agentCount / numThreads;
		int rem = agentCount % numThreads;
		int split = div;		
		
		/* Create a list for each thread and the thread */
		for (i = 0; i < numThreads; i++)
		{
			agentTaskLists[i] = new ArrayList<SimpleAgent>( (split+rem)+1);
		}

		//ListIterator<SimpleAgent> itr = agentList.listIterator();



		int thread_num = 0;
		int tAgentCount = 0;

		/* Temp Var */
		SimpleAgent temp;
				
		/* Split the lists */
			
		agentList.resetHead();
		
		while (agentList.hasNext())
		{
			/* Get an agent */
			temp = agentList.getNext();

			/* This Section adds each agent and its coordinates to the kd tree */
			{
				//pos = new double[2];
				//pos[0] = temp.body.getBodyPos().getX();
				//pos[1] = temp.body.getBodyPos().getY();
				agentKDTree.add(temp.body.getBodyPosKD(),temp);
			}

			/* This section does the decision boundaries for splitting the list */
			if (tAgentCount == split)
			{
				if (thread_num < (numThreads - 1))
				{				
					split = split + div;
					thread_num++;
					
				}
			}

			/* Add the agent to the smaller list */
			agentTaskLists[thread_num].add(temp,temp.body.getBodyPos().getX());

			tAgentCount++;
		}
		/* Lists are now Split */

	}
}
