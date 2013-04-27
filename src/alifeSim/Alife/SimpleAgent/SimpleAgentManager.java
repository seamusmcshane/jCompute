package alifeSim.Alife.SimpleAgent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import org.newdawn.slick.Graphics;

import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;
import alifeSim.Gui.SimulationView;
import alifeSim.Gui.StatsPanel;
import alifeSim.Simulation.BarrierManager;
import alifeSim.World.World;
import alifeSim.datastruct.list.ArrayList;

/**
 * 
 * Agent Manager Class
 * This class contains the setup methods for the agents.
 * It handles movement , creation and destruction of Agents as well as 
 * multi-threaded field of view updates
 * and drawing of the agents.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */

public class SimpleAgentManager
{

	/** The agent Actions Linked Lists */
	//LinkedList<SimpleAgent> doList;
	//LinkedList<SimpleAgent> doneList;
	ArrayList<SimpleAgent> doList;
	ArrayList<SimpleAgent> doneList;	
	
	/** The draw agent references */
	//ListIterator<SimpleAgent> itrDrawAI;
	
	SimpleAgent tAgentDrawAI;

	/** Holds Unique Id position agent id */
	int agentIdCount;

	/** The agent count */
	int agentCount;
	int agentCountMax;
	
	/** Predator and prey counts */
	int preyCount;
	int predatorCount;

	/** Reference for setting barrier tasks */
	BarrierManager barrierManager;

	/** Agent Settings */
	SimpleAgentManagementSetupParam agentSettings;

	/** Hard coded Agent Size */
	float agentSize = 5f;

	/**
	 * Creates am Agent manager.
	 * @param barrierManager
	 * @param worldSize
	 * @param agentPreyNumbers
	 * @param agentPredatorNumbers
	 * @param agentSettings
	 */
	public SimpleAgentManager(BarrierManager barrierManager, int worldSize, int agentPreyNumbers, int agentPredatorNumbers, SimpleAgentManagementSetupParam agentSettings)
	{
		this.agentSettings = agentSettings;  // All the intial agent settings are contained in this struct

		agentCount = 0;
		agentCountMax = 0;
		preyCount = 0;
		predatorCount = 0;
		agentIdCount = 0;

		this.barrierManager = barrierManager;

		setUpLists();

		addAgents(worldSize, agentPreyNumbers, agentPredatorNumbers);

	}

	/**
	 * Adds in build the set number of predators and prey to the world.
	 * @param worldSize
	 * @param agentPreyNumbers
	 * @param agentPredatorNumbers
	 */
	private void addAgents(int worldSize, int agentPreyNumbers, int agentPredatorNumbers)
	{

		/* Random Starting Position */
		Random xr = new Random();
		Random yr = new Random();

		int x, y;

		// Prey
		for (int i = 0; i < agentPreyNumbers; i++)
		{

			x = xr.nextInt(worldSize) + 1;
			y = yr.nextInt(worldSize) + 1;
			
			while(World.isBoundaryWall(x, y))
			{
				x = xr.nextInt(worldSize) + 1;
				y = yr.nextInt(worldSize) + 1;				
			}

			addNewAgent(new SimpleAgent(0, x, y, new SimpleAgentStats(new SimpleAgentType(AgentType.PREY), agentSettings.getPreySpeed(), agentSize, agentSettings.getPreyStartingEnergy(), 100f, agentSettings.getPreyHungerThres(), agentSettings.getPreyViewRange(), agentSettings.getPreyMoveCost(), agentSettings.getPreyRepoCost(), agentSettings.getPreyConsumptionRate(), agentSettings.getPreyDE(), agentSettings.getPreyREDiv())));

		}

		// Predator
		for (int i = 0; i < agentPredatorNumbers; i++)
		{
			x = xr.nextInt(worldSize) + 1;
			y = yr.nextInt(worldSize) + 1;

			while(World.isBoundaryWall(x, y))
			{
				x = xr.nextInt(worldSize) + 1;
				y = yr.nextInt(worldSize) + 1;				
			}			
			
			addNewAgent(new SimpleAgent(0, x, y, new SimpleAgentStats(new SimpleAgentType(AgentType.PREDATOR), agentSettings.getPredatorSpeed(), agentSize, agentSettings.getPredStartingEnergy(), 100f, agentSettings.getPredatorHungerThres(), agentSettings.getPredatorViewRange(), agentSettings.getPredatorMoveCost(), agentSettings.getPredRepoCost(), agentSettings.getPredatorConsumptionRate(), agentSettings.getPredatorDE(), agentSettings.getPredatorREDiv())));

		}
	}

	/**
	 * Add an  agent to the done list for the next step  - keeps counts of agents and types added.
	 * @param agent
	 */
	private void addAgent(SimpleAgent agent)
	{
		doneList.add(agent);

		if (agent.body.stats.getType().getType() == AgentType.PREDATOR)
		{
			predatorCount++;
		}
		else
		// AgentType.PREY
		{
			preyCount++;
		}

		agentCount++;

		if(agentCount > agentCountMax)
		{
			agentCountMax = agentCount;
		}
		
	}

	/**
	 * Add an NEW agent to the done list for the next step and gives it a UID - keeps counts
	 * @param agent
	 */
	private void addNewAgent(SimpleAgent agent)
	{

		if (agent.body.stats.getType().getType() == AgentType.PREDATOR)
		{
			predatorCount++;
		}
		else
		// AgentType.PREY
		{
			preyCount++;
		}

		agentCount++;

		agentIdCount++;

		agent.setId(agentIdCount);

		doneList.add(agent);
	}

	/**
	 * Draws all the agents.
	 * @param g
	 * @param trueDrawing boolean
	 * @param viewRangeDrawing boolean
	 */
	public void drawAgent(Graphics g, boolean trueDrawing, boolean viewRangeDrawing)
	{

		//itrDrawAI = doneList.listIterator();
		
		doneList.resetHead();

		while (doneList.hasNext())
		{

			tAgentDrawAI = doneList.getNext();

			// Optimization - Only draw visible agents that are inside the cameraBoundarie
			if (tAgentDrawAI.body.getBodyPos().getX() > (SimulationView.cameraBound.getX() - SimulationView.globalTranslate.getX()) && tAgentDrawAI.body.getBodyPos().getX() < (SimulationView.cameraBound.getMaxX() - SimulationView.globalTranslate.getX()) && tAgentDrawAI.body.getBodyPos().getY() > (SimulationView.cameraBound.getY() - SimulationView.globalTranslate.getY()) && tAgentDrawAI.body.getBodyPos().getY() < (SimulationView.cameraBound.getMaxY() - SimulationView.globalTranslate.getY()))
			{
				/*
				 * Optimization - draw correct circular bodies or faster
				 * rectangular bodies
				 */
				if (trueDrawing)
				{
					tAgentDrawAI.body.drawTrueBody(g);
				}
				else
				{
					tAgentDrawAI.body.drawRectBody(g);
				}

				if (viewRangeDrawing)
				{
					/* Optimization - Only draw the views of agents we can see */
					tAgentDrawAI.brain.view.drawViewRange(g);
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
		barrierManager.setBarrierAgentTask(doList, agentCount);
	}

	/** This stage performs the list updating and stats updates. */
	public void stage3()
	{
		updateDoneList();

		/* Stats Panel */
		StatsPanel.setPredNo(predatorCount);

		StatsPanel.setPreyNo(preyCount);
	}

	/**
	 * This method moves agents between the do and done lists
	 * It is in effect managing the births and deaths of agents.
	 * If an agent can reproduce it does (deterministic), new agents get added to the done list (first action is being born).
	 * Agents that are dead stay in the do list which gets nullified at the start next step. */
	private void updateDoneList()
	{
		//ListIterator<SimpleAgent> itr = doList.listIterator();

		agentCount = 0;
		preyCount = 0;
		predatorCount = 0;

		/* Temp var */
		SimpleAgent temp;
		
		doList.resetHead();
		
		while (doList.hasNext())
		{

			/* Get a reference to the current agent */
			temp = doList.getNext();

			/* remove from the doList */
			//doList.remove();

			// If agent not dead ..	
			if (!temp.body.stats.isDead())
			{
				// can this agent reproduce...
				if (temp.body.stats.canReproduce())
				{
					temp.body.stats.decrementReproductionCost();

					/*
					 * This sets the new agent stats the same as predecessor If
					 * evolution was ever to be added, there would need to be a
					 * way of Calculating/Mutating the next generation agent
					 * stats here.
					 */
					addNewAgent(new SimpleAgent(0, temp.body.getBodyPos().getX() + 0.01f, temp.body.getBodyPos().getY() - 0.01f, new SimpleAgentStats(new SimpleAgentType(temp.body.stats.getType().getType()), temp.body.stats.getMaxSpeed(), agentSize, temp.body.stats.getStartingEnergy(), 100f, temp.body.stats.getHungryThreshold(), temp.body.stats.getBaseViewRange(), temp.body.stats.getBaseMoveCost(), temp.body.stats.getBaseReproductionCost(), temp.body.stats.getEnergyConsumptionRate(), temp.body.stats.getDigestiveEfficency(), temp.body.stats.getReproductionEnergyDivision())));
				}
				/*else
				{
					System.out.println("Agent Dead");
				}*/
				// Add to donelist  - agents not added get removed by java.
				addAgent(temp);
			}
		}
	}

	/** Sets up the safe starting position for the lists */
	private void setUpLists()
	{
		doList = doneList;
		doneList = new ArrayList<SimpleAgent>(agentCountMax);
	}

	/** Randomize the doList */
	private void randomizeListOrder()
	{
		// TODO randomizeListOrder
		//Collections.shuffle(doList);
	}

	/**
	 * Added for Unit tests
	 * @return agentCount */
	public int getAgentCount()
	{
		return agentCount;
	}

	/**
	 * Added for Unit tests
	 * @return preyCount */
	public int getPreyCount()
	{
		return preyCount;
	}

	/**
	 * Added for Unit tests
	 * @return predatorCount */
	public int getPredatorCount()
	{
		return predatorCount;
	}

}
