package alife;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import alife.SimulationEnums.AgentType;

/*
 * 
 * Agent Manager Class
 * This class contains the setup methods for the agents.
 * It handles movement , creation and destruction of Agents as well as 
 * multi-threaded field of view updates
 * and drawing of the agents.
 */

public class SimpleAgentManager
{
		
	/* Actions Linked Lists */
	LinkedList<SimpleAgent> doList;
	LinkedList<SimpleAgent> doneList;

	/* DrawAI */
	ListIterator<SimpleAgent> itrDrawAI;
	SimpleAgent tAgentDrawAI;
		
	int initial_num_agents;
	
	float base_movement_cost = 0.015f;
	float base_view_range = 25f;
	float base_prey_reproduction_cost = 0.25f; // percent
	float base_predator_reproduction_cost = 0.99f; // percent

	float base_prey_energy_level = 20f;
	
	int agentCount;
	int agentsDone;
	int agentsTodo;

	/* For Debug */
	SimpleAgent testAgent;

	/* Draw the Pixel perfect real bodies or a faster less intensive rectangular version */
	private Boolean true_drawing=true;
	
	/* Draw the field of views for the agents */
	private Boolean view_drawing=true;

	/* Reference for setting task */
	ViewGeneratorManager viewGenerator;
	
	public SimpleAgentManager(ViewGeneratorManager viewGenerator,int world_size, int agent_prey_numbers,int agent_predator_numbers)
	{
		this.viewGenerator = viewGenerator;
					
		setUpLists();

		addAgents(world_size,agent_prey_numbers,agent_predator_numbers);

		agentsTodo = 0;
		agentsDone = 0;
		agentCount = 0;
	}

	
	private void addAgents(int world_size,int agent_prey_numbers,int agent_predator_numbers)
	{
		initial_num_agents = agent_prey_numbers + agent_predator_numbers;
		
		/* Random Starting Position */
		Random xr = new Random();
		Random yr = new Random();
		
		int x, y;
		
		// Prey
		for (int i = 0; i < agent_prey_numbers; i++)
		{
			x = xr.nextInt(world_size) + 1;
			y = yr.nextInt(world_size) + 1;

			// (SimpleAgentType type,float ms, float sz, float me, float vr,float base_move_cost)
			addNewAgent(new SimpleAgent(i, x, y, new SimpleAgentStats(new SimpleAgentType(AgentType.PREY),0.9f, 5f, 100f, base_view_range, base_movement_cost,base_prey_reproduction_cost)));
		}	
		
		// Predator
		for (int i = 0; i < agent_predator_numbers; i++)
		{
			x = xr.nextInt(world_size) + 1;
			y = yr.nextInt(world_size) + 1;

			addNewAgent(new SimpleAgent(i, x, y, new SimpleAgentStats(new SimpleAgentType(AgentType.PREDATOR),1f, 5f, 100f, (base_view_range*2), base_movement_cost,base_predator_reproduction_cost)));
		}	
	}
	
	/* Being born counts as an Action thus all new agents start in the done list */
	public void addNewAgent(SimpleAgent agent)
	{		
		doneList.add(agent);

		agentCount++;
	}

	/* Draws all the agents */
	public void drawAgent(Graphics g)
	{

		itrDrawAI = doneList.listIterator();

		while (itrDrawAI.hasNext())
		{

			tAgentDrawAI = itrDrawAI.next();

			/* Set the current status of the view drawing */
			tAgentDrawAI.setViewDrawing(view_drawing);
			
			// Optimization - Only draw visible agents that are inside the camera_boundarie
			if (tAgentDrawAI.body.getBodyPos().getX() > (SimulationView.camera_bound.getX() - SimulationView.global_translate.getX()) && tAgentDrawAI.body.getBodyPos().getX() < (SimulationView.camera_bound.getMaxX() - SimulationView.global_translate.getX()) && tAgentDrawAI.body.getBodyPos().getY() > (SimulationView.camera_bound.getY() - SimulationView.global_translate.getY()) && tAgentDrawAI.body.getBodyPos().getY() < (SimulationView.camera_bound.getMaxY() - SimulationView.global_translate.getY()))
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
	
	
	// List prepare
	public void stage1()
	{
		/* Safe starting position */
		setUpLists();

		/* Remove bias from agents order in list */
		randomizeListOrder();		
	}
	
	// View 
	public void stage2()
	{
		// Set our Task for the view
		viewGenerator.setBarrierAgentTask(doList,agentCount);
			
	}
	
	// List update
	public void stage3()
	{
		updateDoneList();		
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
			if(!temp.body.stats.isDead())
			{
				// can this agent reproduce...
				if(temp.body.stats.canReproduce())
				{										
					temp.body.stats.decrementReproductionCost(); 
					
					// For now same as predecessor 
					addNewAgent(new SimpleAgent(agentCount++, temp.body.getBodyPos().getX()+0.01f, temp.body.getBodyPos().getY()-0.01f, new SimpleAgentStats(new SimpleAgentType(temp.body.stats.getType().getType()),temp.body.stats.getMaxSpeed(), 5f, 100f, temp.body.stats.getBaseView_range(), base_movement_cost,temp.body.stats.getBaseReproductionCost())));
				}
				
				// Add to donelist  - agents not added get removed by java.
				doneList.add(temp);				
			}		

		}
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
	
	/* Draw true circular bodies or faster rectangular ones */
	public void setTrueDrawing(Boolean setting)
	{
		true_drawing = setting;
	}
	
	private void setTestAgentLocation()
	{
		//testAgent.setDebugPos(Simulation.mouse_pos);
	}

	public void setFieldOfViewDrawing(Boolean setting)
	{
		view_drawing = setting;
	}	
}
