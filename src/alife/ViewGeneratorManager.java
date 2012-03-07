package alife;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import ags.utils.dataStructures.trees.thirdGenKD.KdTree;

public class ViewGeneratorManager extends Thread
{

	private int num_threads;
	
	private Semaphore viewControlerSemaphore;
	
	private Semaphore viewGeneratorStartSemaphores[];
	
	private Semaphore viewGeneratorEndSemaphores[];
	
	/* KD tree representations of the world space */
	private KdTree<SimpleAgent> agentKDTree;
	private KdTree<GenericPlant> plantKDTree;
	
	private ViewGeneratorThread viewThreads[];

	@SuppressWarnings("unchecked")
	private LinkedList<SimpleAgent>[] agentTaskLists;
	
	private LinkedList<SimpleAgent> agentList;
	
	private LinkedList<GenericPlant> plantList;

	private int agentCount;
	private int plantCount;

	
	public ViewGeneratorManager(Semaphore viewControlerSemaphore, int num_threads)
	{
		this.viewControlerSemaphore = viewControlerSemaphore;
		
		this.num_threads = num_threads;
		
		setUpTaskLists();
		
		setUpSemaphores();
				
		setUpThreads();
		
	}
	
	public void setBarrierAgentTask(LinkedList<SimpleAgent> inList, int num_agents)
	{
		agentList = inList;		
		agentCount = num_agents;		
	}
	
	public void setBarrierPlantTask(LinkedList<GenericPlant> inList, int num_plants)
	{
		plantList = inList;
		plantCount = num_plants;		
	}
	
	private void waitThreadsEnd()
	{
		// Ensure all threads are at the barrier
		int i;
		for(i=0;i<num_threads;i++)
		{
			viewGeneratorEndSemaphores[i].acquireUninterruptibly();
		}
		
	}
	
	private void releaseThreadBarrier()
	{
		// Ensure all threads are at the barrier
		int i;
		
		for(i=0;i<num_threads;i++)
		{
			viewGeneratorStartSemaphores[i].release();
		}		

	}	
	
	public void run()
	{
		while(true)
		{
			
			viewControlerSemaphore.acquireUninterruptibly();		
						
			//System.out.println("Start Barrier");
			
			createPlantKDTree();
			
			// Split the lists
			splitAgentList();			
			
			// set the tasks
			setViewThreadsTasks();
			
			// Pull down the barrier / Release the threads
			releaseThreadBarrier();	

			/* Prepare the barrier */
			waitThreadsEnd();
			
			//System.out.println("End Barrier");
			
			viewControlerSemaphore.release();
			
			//System.exit(1);
			
		}		
	}
	
	private void setViewThreadsTasks()
	{
		for(int i = 0;i<num_threads;i++)
		{
			viewThreads[i].setTask(agentTaskLists[i], agentKDTree,plantKDTree);
		}	
	}
	
	@SuppressWarnings("unchecked")
	private void setUpTaskLists()
	{
		agentTaskLists = new LinkedList[num_threads];
	}
	
	private void setUpSemaphores()
	{
		viewGeneratorStartSemaphores = new Semaphore[num_threads];
		
		viewGeneratorEndSemaphores = new Semaphore[num_threads];

		for(int i=0;i<num_threads;i++)
		{
			viewGeneratorStartSemaphores[i] = new Semaphore(0,true);
			
			viewGeneratorEndSemaphores[i] = new Semaphore(0,true);

		}
				
	}
	
	private void setUpThreads()
	{
		viewThreads = new ViewGeneratorThread[num_threads];

		for(int i = 0;i<num_threads;i++)
		{
			viewThreads[i] = new ViewGeneratorThread(i,viewGeneratorStartSemaphores[i],viewGeneratorEndSemaphores[i]);
			viewThreads[i].start();
		}
	}
	
	/* splits the List */
	private void splitAgentList()
	{
		/* 2d - KD-Tree */
		agentKDTree = new KdTree<SimpleAgent>(2);
		
		int i = 0;

		/* Create a list for each thread and the thread */
		for (i = 0; i < num_threads; i++)
		{
			agentTaskLists[i] = new LinkedList<SimpleAgent>();
		}

		ListIterator<SimpleAgent> itr = agentList.listIterator();

		/* Calculate the Splits */
		int div = agentCount / num_threads;
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
				if(thread_num < (num_threads-1))
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
		
	private void createPlantKDTree()
	{
		/* 2d - KD-Tree */
		plantKDTree = new KdTree<GenericPlant>(2);	
		
		ListIterator<GenericPlant> itr = plantList.listIterator();
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
		}
		/* Tree is created */
		
	}
}
