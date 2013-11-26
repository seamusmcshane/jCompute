package alifeSim.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import alifeSim.Alife.GenericPlant.GenericPlant;
import alifeSim.Alife.SimpleAgent.SimpleAgent;
import alifeSim.datastruct.knn.KNNInf;
import alifeSim.datastruct.knn.thirdGenKDWrapper;
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
	private List<SimpleAgent>[] agentTaskLists;

	/** References for Plant Tasks */
	private KNNInf<GenericPlant> plantKDTree;
	private ArrayList<GenericPlant> plantList;
	private List<GenericPlant>[] plantTaskLists;
	
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
		agentTaskLists = new List[numThreads];
		plantTaskLists = new List[numThreads];

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

		for (GenericPlant temp : plantList) 
		{	
			/* This Section adds each plant and its coordinates to the kd tree */
			{
				double[] pos = new double[2];
				pos[0] = temp.body.getBodyPos().getX();
				pos[1] = temp.body.getBodyPos().getY();
				plantKDTree.add(pos, temp);
			}
		}
		
		int i = 0;

		/* Create a list for each thread and the thread */
		int start=0;
		int div = plantList.size() / numThreads;
		int end=0;		
		
		
		if(div>numThreads)
		{
			//System.out.println("Div " + div + " numThreads " + numThreads);

			for (i = 0; i < numThreads; i++)
			{
				end=(div*i)+div-1;
				start=(div*i);
				
				//System.out.println("Start : " + start);
				//System.out.println("End : " + end);
				
				if(i != (numThreads -1))
				{
					plantTaskLists[i] = plantList.subList(start,end);
				}
				else	// To account for rounding error which can leave us one short of the list length
				{
					plantTaskLists[i] = plantList.subList(start,plantList.size());
				}
				
				
			}
		}
		else // Drop to single threaded if the list is small
		{
			plantTaskLists[0] = plantList.subList(0,plantList.size());
			
			for (i = 1; i < numThreads; i++)
			{
				plantTaskLists[i]=null;
			}
		}		
		
		
		
	}

	private void splitAgentList()
	{
		/* 2d - KD-Tree */
		agentKDTree = new thirdGenKDWrapper<SimpleAgent>(2);
		//plantKDTree = new KDTree<GenericPlant>(2);

		for (SimpleAgent temp : agentList) 
		{				
			/* This Section adds each plant and its coordinates to the kd tree */
			{
				double[] pos = new double[2];
				pos[0] = temp.body.getBodyPos().getX();
				pos[1] = temp.body.getBodyPos().getY();
				agentKDTree.add(temp.body.getBodyPosKD(),temp);
			}
		}
		
		int i = 0;

		/* Create a list for each thread and the thread */
		int start=0;
		int div = agentList.size() / numThreads;
		int end=0;		
		
		if(div>numThreads)
		{
			for (i = 0; i < numThreads; i++)
			{
				end=(div*i)+div-1;
				start=(div*i);
				
				//System.out.println("Start : " + start);
				//System.out.println("End : " + end);
				if(i != (numThreads -1))
				{
					agentTaskLists[i] = agentList.subList(start,end);
				}
				else	// To account for rounding error which can leave us one short of the list length
				{
					agentTaskLists[i] = agentList.subList(start,agentList.size());
				}
			}
		}
		else // Drop to single threaded if the list is small
		{
			agentTaskLists[0] = agentList.subList(0,agentList.size());
			
			for (i = 1; i < numThreads; i++)
			{
				agentTaskLists[i]=null;
			}
		}

	}
	
	public void displayBarrierTaskDebugStats()
	{
		for (int i = 0; i < numThreads; i++)
		{
			barrierThreads[i].printDebugStats();
		}
	}
}
