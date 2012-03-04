package alife;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

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

	// Used to prevent dual Access to the done list - which would cause an exception
	Semaphore lock = new Semaphore(1,true);
	
	// TODO make a GUI setting
	int num_threads=6;
	
	/* Actions Linked Lists */
	LinkedList<SimpleAgent> doList;
	LinkedList<SimpleAgent> doneList;

	/* DrawAI */
	ListIterator<SimpleAgent> itrDrawAI;
	SimpleAgent tAgentDrawAI;
	
	ViewGeneratorController viewGenerator;
	Semaphore viewControlerSemaphore;
	
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
		
		this.num_threads = Runtime.getRuntime().availableProcessors(); // Ask Java how many CPU we can use
		
		System.out.println("Threads used for View Generation : " + num_threads);
		
		this.intial_num_agents = num_agents;
		
		viewControlerSemaphore = new Semaphore(1,true);
					
		viewControlerSemaphore.acquireUninterruptibly();
		
		viewGenerator = new ViewGeneratorController(viewControlerSemaphore,num_threads);
		
		viewGenerator.start();
		
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
		
		// Get a lock on the done list
		lock.acquireUninterruptibly();
		
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

		// Release the lock on the done list
		lock.release();
		
	}

	public void setDrawType(Boolean true_draw)
	{
		this.true_drawing = true_draw;
	}
	
	/* Main Method */
	public void doAi()
	{
		// Get a lock on the done list
		lock.acquireUninterruptibly();
		
		/* Debug */
		//doTestAgent();
		
		/* Safe starting position */
		setUpLists();

		/* Remove bias from agents order in list */
		randomizeListOrder();
	
		/* Threaded */
		viewGenerator.setBarrierTask(doList,agentCount);
		
		viewControlerSemaphore.release();
		
		viewControlerSemaphore.acquireUninterruptibly();
		
		/* Non Threaded */
		updateDoneList();//
		
		// Release the lock on the done list
		lock.release();

	}



	/* Performs AI Action and moves it to Done list */
	private void updateDoneList()
	{
		ListIterator<SimpleAgent> itr = doList.listIterator();

		while (itr.hasNext())
		{

			/* Remove this Agent from the List */
			SimpleAgent temp = itr.next();

			/* remove from the doList */
			itr.remove();
			
			// If agent not dead ..			
			// Add to donelist  - agents not added get removed by java.
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
