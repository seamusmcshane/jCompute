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
 * multi-threaded field of view updates
 * and drawing of the agents.
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

		doneList.add(agent);

		agentCount++;
	}

	/* Draws all the agents */
	public void drawAI(Graphics g)
	{
		
		itrDrawAI = doneList.listIterator();

		while (itrDrawAI.hasNext())
		{

			tAgentDrawAI = itrDrawAI.next();

			/* Set the current status of the view drawing */
			tAgentDrawAI.setViewDrawing(view_drawing);
			
			// Optimization - Only draw visible agents that are inside the camera_boundarie
			if (tAgentDrawAI.getPos().getX() > (Simulation.camera_bound.getX() - Simulation.global_translate.getX()) && tAgentDrawAI.getPos().getX() < (Simulation.camera_bound.getMaxX() - Simulation.global_translate.getX()) && tAgentDrawAI.getPos().getY() > (Simulation.camera_bound.getY() - Simulation.global_translate.getY()) && tAgentDrawAI.getPos().getY() < (Simulation.camera_bound.getMaxY() - Simulation.global_translate.getY()))
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
	
	/* Main Method */
	public void doAi()
	{

		/* Debug */
		//doTestAgent();
		
		/* Safe starting position */
		setUpLists();

		/* Remove bias from agents order in list */
		randomizeListOrder();

		/* Threaded */
		setUpAgentViews();

		/* Non Threaded */
		doAgents();

	}

	/* Creates the KD tree of Agents for letting the agents see the world && Then Creates the Agent Views */
	private void setUpAgentViews()
	{
		/* 2d - KD-Tree */
		worldSpace = new KdTree<SimpleAgent>(2);
		
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
			threadedLists[thread_num].add(temp);

			tAgentCount++;
		}

		/* Start the threads */
		for (i = 0; i < num_threads; i++)
		{
			viewThreads[i] = new ViewGeneratorThread(threadedLists[i], worldSpace); /* Threads list and the kd-tree */ 

			viewThreads[i].start();

		}

		/* Join the threads so we keep the simulation in sync */
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
			temp.brain.think();

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
			testAgent = new SimpleAgent(-1,0,0,new SimpleAgentStats(0,0,0,0,0)); /* TODO needs updated */
			
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
		testAgent.setDebugPos(Simulation.mouse_pos);
	}

	public void setFieldOfViewDrawing(Boolean setting)
	{
		view_drawing = setting;
	}	
}
