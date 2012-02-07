package alife;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import ags.utils.dataStructures.trees.thirdGenKD.KdTree;

/*
 * 
 * Agent Manager Class
 * This class contains the setup methods for the agents.
 * It handles movement , creation and destruction of Agents as well as 
 * multi threaded field of view updates
 * and drawing of the agents.
 * This class is synchronized to prevent changing the draw list while drawing.
 */

public class AgentManager
{

	/* KD tree representation of the world space */
	KdTree<SimpleAgent> worldSpace;

	/* Actions Linked Lists */
	LinkedList<SimpleAgent> doList;
	LinkedList<SimpleAgent> doneList;

	/* DrawAI */
	ListIterator<SimpleAgent> itrDrawAI;
	SimpleAgent tAgentDrawAI;

	/* DoAI */
	int num_threads = 5;
	
	ViewGeneratorThread viewThreads[] = new ViewGeneratorThread[num_threads];
	
	@SuppressWarnings("unchecked")
	LinkedList<SimpleAgent>[] threadedLists = new LinkedList[num_threads];

	int intial_num_agents;

	int agentCount;
	int agentsDone;
	int agentsTodo;

	/* For Debug */
	SimpleAgent testAgent;

	/* Draw the Pixel perfect real bodies or a faster less intensive rectangular version */
	private Boolean true_drawing;
	
	/* Draw the field of views for the agents */
	private Boolean view_drawing;

	public AgentManager(int num_agents)
	{

		this.intial_num_agents = num_agents;
		
		setUpLists();

		agentsTodo = 0;
		agentsDone = 0;
		agentCount = 0;
	}

	/* Being born counts as an Action thus all new agents start in the done list */
	public void addNewAgent(SimpleAgent agent)
	{		
		agent.setVisible(true);

		/*
		 * if(agentCount==0) { testAgent=agent; testAgent.setVisible(true);
		 * testAgent.setHighlighted(true); }
		 */

		doneList.add(agent);

		agentCount++;
	}

	/* Draws all the agents Note synchronized with doAi */
	public synchronized void drawAI(Graphics g)
	{
		
		itrDrawAI = doneList.listIterator();

		while (itrDrawAI.hasNext())
		{

			tAgentDrawAI = itrDrawAI.next();

			/* Set the current status of the view drawing */
			tAgentDrawAI.setViewDrawing(view_drawing);
			
			// Optimization - Only draw visible agents that are inside the camera_boundarie
			if (tAgentDrawAI.getPos().getX() > (mainApp.camera_bound.getX() - mainApp.global_translate.getX()) && tAgentDrawAI.getPos().getX() < (mainApp.camera_bound.getMaxX() - mainApp.global_translate.getX()) && tAgentDrawAI.getPos().getY() > (mainApp.camera_bound.getY() - mainApp.global_translate.getY()) && tAgentDrawAI.getPos().getY() < (mainApp.camera_bound.getMaxY() - mainApp.global_translate.getY()))
			{
				/* Optimization - draw correct circular bodies or faster rectangular bodies */
				if(true_drawing==true)
				{
					tAgentDrawAI.body.drawTrueBody(g);	
				}
				else
				{
					tAgentDrawAI.body.drawRectBody(g);						
				}

				/* Optimization - Only draw the views of agents we can see */
				tAgentDrawAI.drawViewRange(g);
			}

		}

	}

	public void setDrawType(Boolean true_draw)
	{
		this.true_drawing = true_draw;
	}
	
	/* Main Method - Called by update thread - Note synchronized with draw */
	public synchronized void doAi()
	{

		/* Debug */
		//doTestAgent();
		
		setUpLists();

		setUpRTree();

		randomizeListOrder();

		/* TODO Threaded */
		setUpAgentViews();

		/* Non Threaded */
		doAgents();

	}

	/* Creates the KD tree of Agents for letting the agents see the world */
	private void setUpRTree()
	{
		worldSpace = new KdTree<SimpleAgent>(2);

		ListIterator<SimpleAgent> itrTree = doList.listIterator();

		while (itrTree.hasNext())
		{
			SimpleAgent temp = itrTree.next();

			double[] pos = new double[2];

			pos[0] = temp.getPos().getX();
			pos[1] = temp.getPos().getY();

			worldSpace.addPoint(pos, temp);
		}
	}

	/* Creates the Agent Views */
	private void setUpAgentViews()
	{
		int i = 0;

		/* Create a list for each thread and the thread */
		for (i = 0; i < num_threads; i++)
		{
			threadedLists[i] = new LinkedList<SimpleAgent>();
		}

		ListIterator<SimpleAgent> itr = doList.listIterator();

		/* Calculate the Splits */
		int div = agentCount / num_threads;
		int thread_num = 0;
		int tAgentCount = 0;

		/* Split the lists */
		while (itr.hasNext())
		{
			SimpleAgent temp = itr.next();

			if (tAgentCount > div)
			{
				div = div + div;
				thread_num++;
				// System.out.println(div);

			}

			threadedLists[thread_num].add(temp);

			tAgentCount++;
		}

		/* Start the threads */
		for (i = 0; i < num_threads; i++)
		{
			viewThreads[i] = new ViewGeneratorThread(threadedLists[i], worldSpace);

			viewThreads[i].start();

		}

		/* Join the threads */
		for (i = 0; i < num_threads; i++)
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

		while (itr.hasNext())
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

	/* TODO */
	private boolean removeAgent(SimpleAgent agent)
	{
		return doList.remove(agent);
	}

	/* Sets up the safe starting position for the lists */
	private void setUpLists()
	{
		doList = doneList;
		doneList = new LinkedList<SimpleAgent>();

	}
	
	/* Randomize the doList */
	private void randomizeListOrder()
	{
		Collections.shuffle(doList);
	}

	/* Debug */
	private void doTestAgent()
	{
		if(testAgent==null)
		{
			testAgent = new SimpleAgent(-1,0,0,25,2);
			
			testAgent.setVisible(true);
			
			testAgent.setViewDrawing(view_drawing);
			
			doneList.add(testAgent);
			
		}
		
		setTestAgentLocation();
	}
	
	/* Draw true circular bodies or faster rectangular ones */
	public void setTrueDrawing(Boolean setting)
	{
		true_drawing = setting;
	}
	
	private void setTestAgentLocation()
	{
		testAgent.setDebugPos(mainApp.mouse_pos);
	}

	public void setFieldOfViewDrawing(Boolean setting)
	{
		view_drawing = setting;
	}	
}
