package alife;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

import ags.utils.dataStructures.trees.thirdGenKD.KdTree;

public class ViewGeneratorController extends Thread
{

	private int num_threads;
	
	private Semaphore viewControlerSemaphore;
	
	private Semaphore viewGeneratorSemaphores[];
	
	/* KD tree representation of the world space */
	private KdTree<SimpleAgent> worldSpace;
	
	private ViewGeneratorThread viewThreads[];

	@SuppressWarnings("unchecked")
	private LinkedList<SimpleAgent>[] taskLists;
	
	private LinkedList<SimpleAgent> agentList;

	private int agentCount;

	
	public ViewGeneratorController(Semaphore viewControlerSemaphore, int num_threads)
	{
		this.viewControlerSemaphore = viewControlerSemaphore;
		
		this.num_threads = num_threads;
		
		setUpTaskLists();
		
		setUpSemaphores();
				
		setUpThreads();
		
	}
	
	public void setBarrierTask(LinkedList<SimpleAgent> inList, int num_agents)
	{
		agentCount = num_agents;		
		agentList = inList;		
	}
	
	private void setThreadBarrier()
	{
		// Ensure all threads are at the barrier
		int i;
		for(i=0;i<num_threads;i++)
		{

			try
			{
				viewGeneratorSemaphores[i].acquire();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	

		
	}
	
	private void releaseThreadBarrier()
	{
		// Ensure all threads are at the barrier
		int i;
		for(i=0;i<num_threads;i++)
		{
				viewGeneratorSemaphores[i].release();
		}		
	}	
	
	public void run()
	{
		while(true)
		{
			
			try
			{
				viewControlerSemaphore.acquire();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			
			// Split the lists
			splitLists();
			
			// set the tasks
			setTasks();
			
			// Pull down the barrier / Release the threads
			releaseThreadBarrier();	

			/* Prepare the barrier */
			setThreadBarrier();
			
			viewControlerSemaphore.release();
			
		}		
	}
	
	private void setTasks()
	{
		for(int i = 0;i<num_threads;i++)
		{
			viewThreads[i].setTask(taskLists[i], worldSpace);
		}	
	}
	
	@SuppressWarnings("unchecked")
	private void setUpTaskLists()
	{
		taskLists = new LinkedList[num_threads];
	}
	
	private void setUpSemaphores()
	{
		viewGeneratorSemaphores = new Semaphore[num_threads];

		for(int i=0;i<num_threads;i++)
		{
			viewGeneratorSemaphores[i] = new Semaphore(1);
			try
			{
				viewGeneratorSemaphores[i].acquire();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Sems set");
		
	}
	
	private void setUpThreads()
	{
		viewThreads = new ViewGeneratorThread[num_threads];

		for(int i = 0;i<num_threads;i++)
		{
			viewThreads[i] = new ViewGeneratorThread(viewGeneratorSemaphores[i]);
			viewThreads[i].start();
		}
	}
	
	/* splits the List */
	private void splitLists()
	{
		/* 2d - KD-Tree */
		worldSpace = new KdTree<SimpleAgent>(2);
		
		int i = 0;

		/* Create a list for each thread and the thread */
		for (i = 0; i < num_threads; i++)
		{
			taskLists[i] = new LinkedList<SimpleAgent>();
		}

		ListIterator<SimpleAgent> itr = agentList.listIterator();

		/* Calculate the Splits */
		int div = agentCount / num_threads;
		int thread_num = 0;
		int tAgentCount = 0;

		/* Split the lists */
		while (itr.hasNext())
		{
			/* Get an agent */
			SimpleAgent temp = itr.next();

			/* This Section add each agent and its coordinates to the kd tree */  
			{
				double[] pos = new double[2];
				pos[0] = temp.getPos().getX();
				pos[1] = temp.getPos().getY();
				worldSpace.addPoint(pos, temp);			
			}
			
			/* This section does the decision boundaries for splitting the list */
			if (tAgentCount > div)
			{
				div = div + div;
				thread_num++;
			}

			/* Add the agent to the smaller list */
			taskLists[thread_num].add(temp);

			tAgentCount++;
		}
		/* Lists are now Split */
		

	}
	
}
