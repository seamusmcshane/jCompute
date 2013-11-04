package alifeSim.Alife.SimpleAgent;

import java.util.ArrayList;
import java.util.Random;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Line;

import alifeSim.Alife.GenericPlant.GenericPlant;
import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;
import alifeSim.Gui.SimulationView;
import alifeSim.Gui.StatsPanel;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.BarrierManager;
import alifeSim.Stats.Stats;
import alifeSim.World.World;
import alifeSim.World.WorldInf;
import alifeSim.World.WorldSetupSettings;

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
	SimpleAgentSetupSettings predatorAgentSettings;
	SimpleAgentSetupSettings preyAgentSettings;

	/** Hard coded Agent Size */
	float agentSize = 5f;

	private WorldInf world;
	
	/**
	 * Creates am Agent manager.
	 * @param world 
	 * @param barrierManager
	 * @param scenario 
	 * @param worldSize
	 * @param agentPreyNumbers
	 * @param agentPredatorNumbers
	 * @param agentSettings
	 */
	public SimpleAgentManager(WorldInf world, BarrierManager barrierManager, SimpleAgentSetupSettings predatorAgentSettings,SimpleAgentSetupSettings preyAgentSettings)
	{
		
		/* All the intial agent settings are contained in this struct */
		this.predatorAgentSettings =predatorAgentSettings;
		this.preyAgentSettings = preyAgentSettings;
		
		this.world = world;
		
		agentCount = 0;
		agentCountMax = 0;
		preyCount = 0;
		predatorCount = 0;
		agentIdCount = 0;

		this.barrierManager = barrierManager;

		setUpLists();

		addAgents(world, predatorAgentSettings.getInitalNumbers(), preyAgentSettings.getInitalNumbers());

		System.out.println("SimpleAgent Manager Setup Complete");
		
	}

	/**
	 * Adds in build the set number of predators and prey to the world.
	 * @param worldSize
	 * @param agentPreyNumbers
	 * @param agentPredatorNumbers
	 */
	private void addAgents(WorldInf world, int agentPredatorNumbers,  int agentPreyNumbers)
	{	
		/* Random Starting Position */
		Random xr = new Random();
		Random yr = new Random();

		int x, y;

		// Predator
		for (int i = 0; i < agentPredatorNumbers; i++)
		{
			x = xr.nextInt(world.getWorldBoundingSquareSize()) + 1;
			y = yr.nextInt(world.getWorldBoundingSquareSize()) + 1;

			while(world.isInvalidPosition(x, y))
			{
				x = xr.nextInt(world.getWorldBoundingSquareSize()) + 1;
				y = yr.nextInt(world.getWorldBoundingSquareSize()) + 1;				
			}			
											//public SimpleAgentStats(SimpleAgentType type,                  , float maxSpeed,                 float size, float startingEnergy,                      float maxEnergy, float hungryThreshold,              float viewRange,                float baseMoveCost,                   float baseReproductionCost,                 float energyConsumptionRate,               float digestiveEfficency,                       float reproductionEnergyDivision)
			addNewAgent(new SimpleAgent(world,0, x, y, new SimpleAgentStats(new SimpleAgentType(AgentType.PREDATOR), predatorAgentSettings.getSpeed(), agentSize, predatorAgentSettings.getStartingEnergy(), 100f, predatorAgentSettings.getHungerThres(), predatorAgentSettings.getViewRange(), predatorAgentSettings.getMoveCost(), predatorAgentSettings.getReproductionCost(), predatorAgentSettings.getConsumptionRate(), predatorAgentSettings.getDigestiveEfficiency(), predatorAgentSettings.getREDiv())));

		}
		
		// Prey
		for (int i = 0; i < agentPreyNumbers; i++)
		{

			x = xr.nextInt(world.getWorldBoundingSquareSize()) + 1;
			y = yr.nextInt(world.getWorldBoundingSquareSize()) + 1;
			
			while(world.isInvalidPosition(x, y))
			{
				x = xr.nextInt(world.getWorldBoundingSquareSize()) + 1;
				y = yr.nextInt(world.getWorldBoundingSquareSize()) + 1;				
			}

			addNewAgent(new SimpleAgent(world,0, x, y, new SimpleAgentStats(new SimpleAgentType(AgentType.PREY), preyAgentSettings.getSpeed(), agentSize, preyAgentSettings.getStartingEnergy(), 100f, preyAgentSettings.getHungerThres(), preyAgentSettings.getViewRange(), preyAgentSettings.getMoveCost(), preyAgentSettings.getReproductionCost(), preyAgentSettings.getConsumptionRate(), preyAgentSettings.getDigestiveEfficiency(), preyAgentSettings.getREDiv())));

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
	public void drawAgent(Graphics g, boolean simpleDrawing, boolean viewRangeDrawing, boolean viewsDrawing)
	{

		for (SimpleAgent tAgentDrawAI : doneList) 
		{

			// Optimization - Only draw visible agents that are inside the cameraBoundarie
			if (tAgentDrawAI.body.getBodyPos().getX() > (SimulationView.cameraBound.getX() - SimulationView.globalTranslate.getX()) && tAgentDrawAI.body.getBodyPos().getX() < (SimulationView.cameraBound.getMaxX() - SimulationView.globalTranslate.getX()) && tAgentDrawAI.body.getBodyPos().getY() > (SimulationView.cameraBound.getY() - SimulationView.globalTranslate.getY()) && tAgentDrawAI.body.getBodyPos().getY() < (SimulationView.cameraBound.getMaxY() - SimulationView.globalTranslate.getY()))
			{
				/*
				 * Optimization - draw correct circular bodies or faster
				 * rectangular bodies
				 */
				if(simpleDrawing)
				{
					tAgentDrawAI.body.drawRectBody(g);
				}
				else
				{
					tAgentDrawAI.body.drawTrueBody(g);
				}
				
				if (viewRangeDrawing)
				{
					/* Optimization - Only draw the views of agents we can see */
					tAgentDrawAI.brain.view.drawViewRange(g);
				}
				
				if(viewsDrawing)
				{
					/* Draw the agent views */
					tAgentDrawAI.brain.view.drawViews(g);
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
	}

	/**
	 * This method moves agents between the do and done lists
	 * It is in effect managing the births and deaths of agents.
	 * If an agent can reproduce it does (deterministic), new agents get added to the done list (first action is being born).
	 * Agents that are dead stay in the do list which gets nullified at the start next step. */
	private void updateDoneList()
	{
		agentCount = 0;
		preyCount = 0;
		predatorCount = 0;
		//StatsPanel.statsDensityPanel.resetStats();
		
		for (SimpleAgent temp : doList) 
		{
			// If agent not dead ..	
			if (!temp.body.stats.isDead())
			{
				// can this agent reproduce...
				if (temp.body.stats.canReproduce())
				{
					//System.out.println("Agent temp.body.stats.canReproduce()" + temp.body.stats.canReproduce() );
					temp.body.stats.decrementReproductionCost();

					/*
					 * This sets the new agent stats the same as predecessor If
					 * evolution was ever to be added, there would need to be a
					 * way of Calculating/Mutating the next generation agent
					 * stats here.
					 */
					addNewAgent(new SimpleAgent(world,0, temp.body.getBodyPos().getX() + 0.01f, temp.body.getBodyPos().getY() - 0.01f, new SimpleAgentStats(new SimpleAgentType(temp.body.stats.getType().getType()), temp.body.stats.getMaxSpeed(), agentSize, temp.body.stats.getStartingEnergy(), 100f, temp.body.stats.getHungryThreshold(), temp.body.stats.getBaseViewRange(), temp.body.stats.getBaseMoveCost(), temp.body.stats.getBaseReproductionCost(), temp.body.stats.getEnergyConsumptionRate(), temp.body.stats.getDigestiveEfficency(), temp.body.stats.getReproductionEnergyDivision())));
				}

				// Add to donelist  - agents not added get removed by java.
				addAgent(temp);
				//StatsPanel.statsDensityPanel.incrementAgentNum(temp.body.getBodyPos().getX(),temp.body.getBodyPos().getY(),temp.body.stats.getType().getType());

			}
			//else
			//{
			//	
			//}
			
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
