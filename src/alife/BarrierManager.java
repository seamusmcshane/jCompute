package alife;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
/**
 * 
 * This class instantiates a barrier manager.
 * Its task is to generate plant and agent kd trees.
 * Then divide the lists of agents and trees in to smaller lists to be processed in threads.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class BarrierManager extends Thread
{
	/** Number of threads used in this barrier */
	private int numThreads;
	
	/* The lock for the entire barrier */
	private Semaphore barrierControlerSemaphore;
	
	/** The start semaphores for the threads */
	private Semaphore barrierManagerStartSemaphores[];
	
	/** The end semaphores for the threads */
	private Semaphore barrierManagerEndSemaphores[];
		
	/** The array of threads */
	private BarrierTaskThread barrierThreads[];

	/** References for Agent Tasks */
	private KdTree<SimpleAgent> agentKDTree;	
	private LinkedList<SimpleAgent> agentList;
	private LinkedList<SimpleAgent>[] agentTaskLists;
	
	/** References for Plant Tasks */
	private KdTree<GenericPlant> plantKDTree;
	private LinkedList<GenericPlant> plantList;
	private LinkedList<GenericPlant>[] plantTaskLists;
	
	/** Counts used in list division */
	private int agentCount;
	private int plantCount;

	/**
	 * Creates a new barrier manager.
	 * @param barrierControlerSemaphore
	 * @param numThreads
	 */
	public BarrierManager(Semaphore barrierControlerSemaphore, int numThreads)
	{
		this.barrierControlerSemaphore = barrierControlerSemaphore;
		
		this.numThreads = numThreads;
		
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
		while(true)
		{
			
			barrierControlerSemaphore.acquireUninterruptibly();		
						
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
			
			barrierControlerSemaphore.release();
						
		}		
	}
	
	/**
	 * Updates the barrier with a new list of agents to process.
	 * @param inList
	 * @param numAgents int
	 */
	public void setBarrierAgentTask(LinkedList<SimpleAgent> inList, int numAgents)
	{
		agentList = inList;		
		agentCount = numAgents;		
	}
	
	/** 
	 * Updates the barrier with a new list of plants to process.
	 * @param inList
	 * @param numPlants int
	 */
	public void setBarrierPlantTask(LinkedList<GenericPlant> inList, int numPlants)
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
		
		for(i=0;i<numThreads;i++)
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
		for(i=0;i<numThreads;i++)
		{
			barrierManagerEndSemaphores[i].acquireUninterruptibly();
		}
		
	}
	
	/**
	 * Sets the references in the threads to the sub lists they need to process.
	 */
	private void setbarrierThreadsTasks()
	{
		for(int i = 0;i<numThreads;i++)
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
		agentTaskLists = new LinkedList[numThreads];
		plantTaskLists = new LinkedList[numThreads];		
	}
	
	/** 
	 * Initializes the internal barrier semaphores.
	 */
	private void setUpSemaphores()
	{
		barrierManagerStartSemaphores = new Semaphore[numThreads];
		
		barrierManagerEndSemaphores = new Semaphore[numThreads];

		for(int i=0;i<numThreads;i++)
		{
			barrierManagerStartSemaphores[i] = new Semaphore(0,true);			
			barrierManagerEndSemaphores[i] = new Semaphore(0,true);
		}				
	}
	
	/**
	 * Initializes the barrier threads with their semaphores.
	 */
	private void setUpThreads()
	{
		barrierThreads = new BarrierTaskThread[numThreads];

		for(int i = 0;i<numThreads;i++)
		{
			barrierThreads[i] = new BarrierTaskThread(i,barrierManagerStartSemaphores[i],barrierManagerEndSemaphores[i]);
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
		plantKDTree = new KdTree<GenericPlant>(2);
		
		int i = 0;

		/* Create a list for each thread and the thread */
		for (i = 0; i < numThreads; i++)
		{
			plantTaskLists[i] = new LinkedList<GenericPlant>();
		}

		ListIterator<GenericPlant> itr = plantList.listIterator();

		/* Calculate the Splits */
		int div = plantCount / numThreads;
		int split = div;
		
		int thread_num = 0;
		int tPlantCount = 0;

		/* Split the lists */
		while (itr.hasNext())
		{
			/* Get an agent */
			GenericPlant temp = itr.next();

			/* This Section adds each plant and its coordinates to the kd tree */  
			{
				double[] pos = new double[2];
				pos[0] = temp.body.getBodyPos().getX();
				pos[1] = temp.body.getBodyPos().getY();
				plantKDTree.addPoint(pos, temp);			
			}		
			
			/* This section does the decision boundaries for splitting the list */
			if (tPlantCount == split)
			{
				if(thread_num < (numThreads-1))
				{
					split = split + div;
					thread_num++;						
				}			
			}

			/* Add the plant to the smaller list */
			plantTaskLists[thread_num].add(temp);	

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
		agentKDTree = new KdTree<SimpleAgent>(2);
		
		int i = 0;

		/* Create a list for each thread and the thread */
		for (i = 0; i < numThreads; i++)
		{
			agentTaskLists[i] = new LinkedList<SimpleAgent>();
		}

		ListIterator<SimpleAgent> itr = agentList.listIterator();

		/* Calculate the Splits */
		int div = agentCount / numThreads;
		int split = div;
		
		int thread_num = 0;
		int tAgentCount = 0;

		/* Split the lists */
		while (itr.hasNext())
		{
			/* Get an agent */
			SimpleAgent temp = itr.next();

			/* This Section adds each agent and its coordinates to the kd tree */  
			{
				double[] pos = new double[2];
				pos[0] = temp.body.getBodyPos().getX();
				pos[1] = temp.body.getBodyPos().getY();
				agentKDTree.addPoint(pos, temp);			
			}		
			
			/* This section does the decision boundaries for splitting the list */
			if (tAgentCount == split)
			{
				if(thread_num < (numThreads-1))
				{
					split = split + div;
					thread_num++;						
				}			
			}

			/* Add the agent to the smaller list */
			agentTaskLists[thread_num].add(temp);	

			tAgentCount++;
		}
		/* Lists are now Split */

	}
}
