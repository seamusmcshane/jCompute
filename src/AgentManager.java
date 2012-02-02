import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import org.newdawn.slick.Graphics;

import ags.utils.dataStructures.trees.thirdGenKD.KdTree;

public class AgentManager
{

	/* Field of View */
	//PRTree<SimpleAgent> worldView;
	
	KdTree<SimpleAgent> worldSpace;
	
	/* Actions Linked Lists */
	LinkedList<SimpleAgent> doList;
	LinkedList<SimpleAgent> doneList;
	
	/* Temp */
	Iterable<SimpleAgent> tempList;
	
	/* DrawAI */
	ListIterator<SimpleAgent> itrDrawAI;
	SimpleAgent tAgentDrawAI;
	
	/* DoAI */
	int num_threads=3;
	ViewGeneratorThread viewThreads[] = new ViewGeneratorThread[num_threads];
	LinkedList<SimpleAgent>[] threadedLists = new LinkedList[num_threads];
	
	int intial_num_agents;
	int max_tree_size;

	int agentCount;
	int agentsDone;
	int agentsTodo;
			
	SimpleAgent testAgent;
	
	public AgentManager(int num_agents)
	{
		
		this.intial_num_agents = num_agents;
		this.max_tree_size = intial_num_agents*4;
		setUpLists();
				
		agentsTodo=0;
		agentsDone=0;
		agentCount=0;
	}
	
	/* Being born counts as an Action */
	public void addNewAgent(SimpleAgent agent)
	{

		
		agent.setHighlighted(false);
		agent.setVisible(true);
		
		/*if(agentCount==0)
		{
			testAgent=agent;
			testAgent.setVisible(true);
			testAgent.setHighlighted(true);
		}	*/
		
		doneList.add(agent);
				
		agentCount++;
	}
	
	public void drawAI(Graphics g) 
	{
		
		itrDrawAI = doneList.listIterator(); 
		
		while(itrDrawAI.hasNext()) 
		{

			tAgentDrawAI = itrDrawAI.next();
			tAgentDrawAI.drawAgent(g);
			tAgentDrawAI.drawViewRange(g);
		}		
		
	}
	
	public void doAi()
	{
		
		setUpLists();
		
		setUpRTree();
		
		randomizeListOrder();
		
		/* TODO Threaded */
		setUpAgentViews();
				
		/* Non Threaded */
		doAgents();
		
	}
		  
	private void setUpRTree()
	{
			
		worldSpace = new KdTree<SimpleAgent>(2);		

		ListIterator<SimpleAgent> itrTree = doList.listIterator(); 
				
		while(itrTree.hasNext()) 
		{
			SimpleAgent temp = itrTree.next();
			
			double[] pos = new double[2];
			
			pos[0]=temp.getPos().getX();
			pos[1]=temp.getPos().getY();
							
			worldSpace.addPoint(pos, temp);
			
		}		

	}

	private void setUpLists()
	{
		doList = doneList;		
		doneList = new LinkedList<SimpleAgent>();	
		
	}
	
	private void setUpAgentViews()
	{
		int i=0;
		
		/* Create a list for each thread and the thread */
		for(i=0;i<num_threads;i++)
		{
			threadedLists[i] = new LinkedList<SimpleAgent>();
		}
				
		ListIterator<SimpleAgent> itr = doList.listIterator();
		
		/* Calculate the Splits */
		int div = agentCount / num_threads;
		int thread_num=0;
		int tAgentCount=0;

		/* Split the lists */
		while(itr.hasNext()) 
		{
			SimpleAgent temp = itr.next();

			if(tAgentCount>div)
			{
				div=div+div;
				thread_num++;
				//System.out.println(div);

			}
			
			threadedLists[thread_num].add(temp);
			
			tAgentCount++;
		}
		
		//System.out.println(tAgentCount);
		
		/* Start the threads */
		for(i=0;i<num_threads;i++)
		{
	 		viewThreads[i] = new ViewGeneratorThread(threadedLists[i],worldSpace);

			viewThreads[i].start();			

		}

		/* Join the threads */
		for(i=0;i<num_threads;i++)
		{
			try
			{
				viewThreads[i].join();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		

		}
	
	}
	
	/* Performs AI Action and moves it to Done list */
	private void doAgents()
	{
		ListIterator<SimpleAgent> itr = doList.listIterator(); 
		
		while(itr.hasNext()) 
		{

			/* Remove this Agent from the List */
			SimpleAgent temp = itr.next();
			
			/* remove from the doList */
			itr.remove();
			
			/* Move, Eat ,Sleep etc */
			temp.think();
			
			doneList.add(temp);
			
		}
	}
	
	private boolean removeAgent(SimpleAgent agent)
	{
		return doList.remove(agent);
	}
	
	private void randomizeListOrder()
	{
		Collections.shuffle(doList);		
	}
	
}
