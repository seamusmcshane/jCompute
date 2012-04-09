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
import alife.SimpleAgentTypeEnum.AgentType;

/**
 * 
 * Agent Manager Class
 * This class contains the setup methods for the agents.
 * It handles movement , creation and destruction of Agents as well as 
 * multi-threaded field of view updates
 * and drawing of the agents.
 */

public class SimpleAgentManager
{
		
	/** The agent Actions Linked Lists */
	LinkedList<SimpleAgent> doList;
	LinkedList<SimpleAgent> doneList;

	/** The draw agent references */
	ListIterator<SimpleAgent> itrDrawAI;
	SimpleAgent tAgentDrawAI;
	
	/** Holds Unique Id position agent id */
	int agentIdCount;
	
	/** The agent count */
	int agentCount;
	
	/** Predator and prey counts */
	int prey_count;
	int pred_count;
	
	/** Reference for setting barrier tasks */
	BarrierManager barrierManager;
	
	/** Agent Settings */
	SimpleAgentManagementSetupParam agentSettings;
	
	/**
	 * Creates am Agent manager.
	 * @param barrierManager
	 * @param world_size
	 * @param agent_prey_numbers
	 * @param agent_predator_numbers
	 * @param agentSettings
	 */
	public SimpleAgentManager(BarrierManager barrierManager,int world_size, int agent_prey_numbers,int agent_predator_numbers,SimpleAgentManagementSetupParam agentSettings)
	{
		this.agentSettings = agentSettings;  // All the intial agent settings are contained in this struct

		agentCount = 0;
		prey_count = 0;
		pred_count = 0;
		agentIdCount=0;
		
		this.barrierManager = barrierManager;
					
		setUpLists();
		
		addAgents(world_size,agent_prey_numbers,agent_predator_numbers);

	}
	
	/**
	 * Adds in build the set number of predators and prey to the world.
	 * @param world_size
	 * @param agent_prey_numbers
	 * @param agent_predator_numbers
	 */
	private void addAgents(int world_size,int agent_prey_numbers,int agent_predator_numbers)
	{
		
		/* Random Starting Position */
		Random xr = new Random();
		Random yr = new Random();
		
		int x, y;
		
		// Prey
		for (int i = 0; i < agent_prey_numbers; i++)
		{
		
			x = xr.nextInt(world_size) + 1;
			y = yr.nextInt(world_size) + 1;

			//SimpleAgentStats(SimpleAgentType type,float ms, float sz, float se,float me, float ht, float vr, float base_move_cost,float base_reproduction_cost, float ecr, float de, float red)
			addNewAgent(new SimpleAgent(0, x, y, new SimpleAgentStats(new SimpleAgentType(AgentType.PREY),agentSettings.getPreySpeed(), 5f,agentSettings.getPreyStartingEnergy(), 100f,agentSettings.getPreyHungerThres(), agentSettings.getPreyViewRange(), agentSettings.getPreyMoveCost(),agentSettings.getPreyRepoCost(),agentSettings.getPreyConsumptionRate(),agentSettings.getPreyDE(),agentSettings.getPreyREDiv())));

		}	
		
		// Predator
		for (int i = 0; i < agent_predator_numbers; i++)
		{
			x = xr.nextInt(world_size) + 1;
			y = yr.nextInt(world_size) + 1;

			//SimpleAgentStats(SimpleAgentType type,float ms, float sz, float se,float me, float ht, float vr, float base_move_cost,float base_reproduction_cost, float ecr, float de, float red)
			addNewAgent(new SimpleAgent(0, x, y, new SimpleAgentStats(new SimpleAgentType(AgentType.PREDATOR),agentSettings.getPredatorSpeed(), 5f,agentSettings.getPredStartingEnergy(), 100f,agentSettings.getPredatorHungerThres(), agentSettings.getPredatorViewRange(), agentSettings.getPredatorMoveCost(),agentSettings.getPredRepoCost(),agentSettings.getPredatorConsumptionRate(),agentSettings.getPredatorDE(),agentSettings.getPredatorREDiv())));

		}	
	}

	/**
	 * Add an  agent to the done list for the next step  - keeps counts of agents and types added.
	 * @param agent
	 */
	public void addAgent(SimpleAgent agent)
	{		
		doneList.add(agent);

		if(agent.body.stats.getType().getType() == AgentType.PREDATOR)
		{
			pred_count++;
		}
		else // AgentType.PREY
		{
			prey_count++;
		}
		
		agentCount++;	
			
	}

	/**
	 * Add an NEW agent to the done list for the next step and gives it a UID - keeps counts
	 * @param agent
	 */
	public void addNewAgent(SimpleAgent agent)
	{		

		if(agent.body.stats.getType().getType() == AgentType.PREDATOR)
		{
			pred_count++;
		}
		else // AgentType.PREY
		{
			prey_count++;
		}
		
		agentCount++;
		
		agentIdCount++;
		
		agent.setId(agentIdCount);
		
		doneList.add(agent);
	}

	/**
	 * Draws all the agents.
	 * @param g
	 * @param true_drawing
	 * @param view_range_drawing
	 */
	public void drawAgent(Graphics g,boolean true_drawing,boolean view_range_drawing)
	{

		itrDrawAI = doneList.listIterator();

		while (itrDrawAI.hasNext())
		{

			tAgentDrawAI = itrDrawAI.next();
			
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

				
				if(view_range_drawing)
				{
					/* Optimization - Only draw the views of agents we can see */
					tAgentDrawAI.drawViewRange(g);					
				}
			}

		}
		
	}
	
	/** Agent List preparation for the barrier */
	public void stage1()
	{
		/* Safe starting position */
		setUpLists();

		/* Remove bias from agents order in list */
		randomizeListOrder();		
	}
	
	/** Sets the barrier task for agents */
	public void stage2()
	{
		barrierManager.setBarrierAgentTask(doList,agentCount);			
	}
	
	/** This stage performs the list updating and stats updates. */
	public void stage3()
	{
		updateDoneList();
		
		/* Stats Panel */
		StatsPanel.setPredNo(pred_count);
		
		StatsPanel.setPreyNo(prey_count);			
	}

	/**
	 * This method moves agents between the do and done lists
	 * It is in effect managing the births and deaths of agents.
	 * If an agent can reproduce it does (deterministic), new agents get added to the done list (first action is being born).
	 * Agents that are dead stay in the do list which gets nullified at the start next step. */
	private void updateDoneList()
	{
		ListIterator<SimpleAgent> itr = doList.listIterator();

		agentCount=0;
		prey_count = 0;
		pred_count = 0;
		
		while (itr.hasNext())
		{

			/* Get a reference to the current agent */
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
					
					/* This sets the new agent stats the same as predecessor
					 * If evolution was ever to be added, there would need to be a way of 
					 * Calculating/Mutating the next generation agent stats here. */
					addNewAgent(new SimpleAgent(0, temp.body.getBodyPos().getX()+0.01f, temp.body.getBodyPos().getY()-0.01f, new SimpleAgentStats(new SimpleAgentType(temp.body.stats.getType().getType()),temp.body.stats.getMaxSpeed(), 5f, temp.body.stats.getStartingEnergy(),100f,temp.body.stats.getHungryThreshold(), temp.body.stats.getBaseView_range(), temp.body.stats.getBaseMoveCost(),temp.body.stats.getBaseReproductionCost(),temp.body.stats.getEnergyConsumptionRate(),temp.body.stats.getDigestiveEfficency(),temp.body.stats.getReproductionEnergyDivision())));
				}
				
				// Add to donelist  - agents not added get removed by java.
				addAgent(temp);
			}		
		}
	}

	/** Sets up the safe starting position for the lists */
	private void setUpLists()
	{
		doList = doneList;
		doneList = new LinkedList<SimpleAgent>();
	}
	
	/** Randomize the doList */
	private void randomizeListOrder()
	{
		Collections.shuffle(doList);
	}

}
