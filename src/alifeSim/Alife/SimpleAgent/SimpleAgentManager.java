package alifeSim.Alife.SimpleAgent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.newdawn.slick.Graphics;

import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;
import alifeSim.Gui.SimulationView;
import alifeSim.Simulation.BarrierManager;
import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatInf;
import alifeSim.World.WorldInf;

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
	
	/** The agent count - used in array list size allocation*/
	int agentCountMax;	
	
	private SingleStat statAgentTotal;	
	private int agentTotal;
	
	/** Prey */
	private int preyTotal;
	private SingleStat statPreyTotal;

	private int preyBirths;
	private SingleStat statPreyBirths;
	
	private int preyDeaths;
	private SingleStat statPreyDeaths;
	
	/** Predators */
	private int predatorTotal;
	private SingleStat statPredatorTotal;
	
	private int predatorBirths;
	private SingleStat statPredatorBirths;
	
	private int predatorDeaths;
	private SingleStat statPredatorDeaths;
		
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
		
		setUpStats();
			
		this.barrierManager = barrierManager;

		setUpLists();

		addAgents(world, predatorAgentSettings.getInitalNumbers(), preyAgentSettings.getInitalNumbers());

		System.out.println("SimpleAgent Manager Setup Complete");
		
	}
	
	/**
	 * Adds the set number of predators and prey to the world.
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
			predatorTotal++;
		}
		else // AgentType.PREY
		{
			preyTotal++;
		}

		agentTotal++;

		if(agentTotal > agentCountMax)
		{
			agentCountMax = agentTotal;
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
			predatorTotal++;
		}
		else
		// AgentType.PREY
		{
			preyTotal++;
		}

		agentTotal++;

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
		barrierManager.setBarrierAgentTask(doList, agentTotal);
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
		agentTotal = 0;
		preyTotal = 0;
		predatorTotal = 0;
		
		preyBirths = 0;
		preyDeaths = 0;
		
		predatorBirths = 0;
		predatorDeaths = 0;
		
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
					
					if(temp.body.stats.getType().getType() == AgentType.PREDATOR)
					{
						predatorBirths++;
					}
					else
					{
						preyBirths++; 
					}
					
				}

				// Add to donelist  - agents not added get removed by java.
				addAgent(temp);
				//StatsPanel.statsDensityPanel.incrementAgentNum(temp.body.getBodyPos().getX(),temp.body.getBodyPos().getY(),temp.body.stats.getType().getType());

			}
			else
			{
				if(temp.body.stats.getType().getType() == AgentType.PREDATOR)
				{
					predatorDeaths++;
				}
				else
				{
					preyDeaths++; 
				}	
			}
			
		}
		
		statAgentTotal.addSample(agentTotal);
		statPredatorTotal.addSample(predatorTotal);
		statPreyTotal.addSample(preyTotal);
		
		statPreyBirths.addSample(preyBirths);
		statPredatorBirths.addSample(predatorBirths);
		
		statPreyDeaths.addSample(preyDeaths);
		statPredatorDeaths.addSample(predatorDeaths);

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
		return agentTotal;
	}

	/**
	 * Added for Unit tests
	 * @return preyCount */
	public int getPreyCount()
	{
		return preyTotal;
	}

	/**
	 * Added for Unit tests
	 * @return predatorCount */
	public int getPredatorCount()
	{
		return predatorTotal;
	}

	private void setUpStats()
	{
		statAgentTotal = new SingleStat("Agents");
		
		statPreyTotal = new SingleStat("Prey");
		statPreyTotal.setColor(Color.blue);
		
		statPreyBirths = new SingleStat ("Prey Births");
		statPreyBirths.setColor(new Color(Color.HSBtoRGB(0.65f,1f,1f)));
		
		
		
		statPreyDeaths = new SingleStat ("Prey Deaths");
		statPreyDeaths.setColor(new Color(Color.HSBtoRGB(0.65f,0.4f,1f)));
		
		
		statPredatorTotal = new SingleStat("Predators");
		statPredatorTotal.setColor(Color.red);
		
		statPredatorBirths = new SingleStat ("Predator Births");
		statPredatorBirths.setColor(new Color(Color.HSBtoRGB(0f,1f,1f)));
		
		statPredatorDeaths = new SingleStat ("Predator Deaths");
		statPredatorDeaths.setColor(new Color(Color.HSBtoRGB(0f,0.4f,1f)));

		preyBirths = 0;
		preyDeaths = 0;

		predatorBirths = 0;
		predatorDeaths = 0;
		
		
		agentTotal = 0;
		agentCountMax = 0;
		preyTotal = 0;
		predatorTotal= 0;
		agentIdCount = 0;
	}
	
	public List<StatInf> getPopulationStats()
	{

		List<StatInf> stat = new LinkedList<StatInf>();
		
		stat.add(statPredatorTotal);
		stat.add(statPreyTotal);
		
		return stat;
	}
	
	public List<StatInf> getBirthDeathStats()
	{
		List<StatInf> stat = new LinkedList<StatInf>();
		
		stat.add(statPreyBirths);
		stat.add(statPreyDeaths);
		
		stat.add(statPredatorBirths);
		stat.add(statPredatorDeaths);
		
		return stat;
	}

}
