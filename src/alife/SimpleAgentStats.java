package alife;
/**
 * This class holds the stats for an individual agent.
 * It manages the energy of the agent.
 * 	Including all variables acting on the energy.
 * It also manages reproduction energy.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentStats
{
	/** The movement cost of the agent before modification */
	private final float baseMoveCost;

	/** the dead tag for removing agent from the simulation */
	private boolean dead;

	/** Agent movement speed */
	private final float maxSpeed;

	/** Agent Size */
	private final float size;

	/** Max Energy of Agent */
	private final float maxEnergy;

	/** Current Energy of the agent */
	private float energy;

	/** Agents hungry state */
	private boolean hungry = true;

	/** The ability of this agent to consume energy */
	private float digestiveEfficency;

	/** The threshold before this agent is hungry */
	private float hungryThreshold;

	/** View Range */
	private float viewRange;

	/** Reproduction Energy */
	private float reproductionBank;

	/** Cost of reproduction */
	private float reproductionCost;

	/** Base cost of reproduction */
	private float baseReproductionCost;

	/** The consumption rate of energy from what every the agent is eating (Current only plants) */
	private float energyConsumptionRate;

	/** Agent age in Simulation Steps */
	private long age; // not used - todo evolution

	/** This agents type Predator/Prey */
	private SimpleAgentType type;

	/** The starting energy level */
	private float startingEnergy;

	/** The ratio of energy to reproduction or survival */
	private float reproductionEnergyDivision;

	/**
	 * Creates the stats for this agent.
	 * @param type SimpleAgentType
	 * @param maxSpeed float
	 * @param size float
	 * @param startingEnergy float
	 * @param maxEnergy float
	 * @param hungryThreshold float
	 * @param viewRange float
	 * @param baseMoveCost float
	 * @param baseReproductionCost float
	 * @param energyConsumptionRate float
	 * @param digestiveEfficency float
	 * @param reproductionEnergyDivision float
	 */
	public SimpleAgentStats(SimpleAgentType type, float maxSpeed, float size, float startingEnergy, float maxEnergy, float hungryThreshold, float viewRange, float baseMoveCost, float baseReproductionCost, float energyConsumptionRate, float digestiveEfficency, float reproductionEnergyDivision)
	{
		this.dead = false;

		this.type = type;

		this.maxSpeed = maxSpeed;

		this.size = size;

		this.maxEnergy = maxEnergy;

		this.reproductionBank = 0;

		this.energy = startingEnergy; // Starting Energy

		this.startingEnergy = startingEnergy;

		this.hungryThreshold = hungryThreshold; // in energy

		this.age = 0;	// Not implemented - age in steps

		this.viewRange = size + viewRange; // Places the view range from the body, rather than from the middle

		this.baseMoveCost = baseMoveCost;

		this.baseReproductionCost = baseReproductionCost;

		this.reproductionCost = maxEnergy * baseReproductionCost;

		this.energyConsumptionRate = energyConsumptionRate;

		this.digestiveEfficency = digestiveEfficency;

		this.reproductionEnergyDivision = reproductionEnergyDivision;

		if (reproductionCost > maxEnergy)
		{
			this.reproductionCost = maxEnergy; // Agent will most likely die on reproduction
		}

	}

	/** Hungry status 
	 * @return boolean */
	public boolean isHungry()
	{
		return hungry;
	}

	/** Hungry toggle */
	private void updateHunger()
	{
		if (energy < hungryThreshold)
		{
			hungry = true;
		}
		else
		{
			hungry = false;
		}
	}

	/** The movement cost decrementer */
	public void decrementMoveEnergy()
	{
		energy = energy - (size * baseMoveCost);

		//Debug
		//energy = energy - (baseMoveCost);

		if (energy <= 0)
		{
			dead = true;
		}

		updateHunger();
	}

	/**
	 * This method is in effect the digestive method.
	 * It add energy taking in to account how efficient the agents digestion is.
	 * The energy is split between reproduction energy banks and normal energy.
	 * @param energy float
	 */
	public void addEnergy(float energy)
	{
		// How efficiently can this agent convert what it eats to energy it can use.
		energy = energy * digestiveEfficency;

		/*
		 * This adds energy to the agents survival bank eg assuming
		 * reproductionEnergyDivision = 0.25, energy = 10; then what is added is
		 * 10*0.25 ie 2.5
		 */
		this.energy = this.energy + (energy * (1 * reproductionEnergyDivision));
		if (this.energy > maxEnergy)
		{
			this.energy = maxEnergy;
		}

		/*
		 * As above but the remainder of the multiple is added if energy = 10,
		 * and reproductionEnergyDivision = 0.25 then 1-0.25 = 0.75 , 10*0.75 =
		 * 7.5.
		 */
		this.reproductionBank = this.reproductionBank + (energy * (1 - reproductionEnergyDivision));
		if (this.reproductionBank > maxEnergy)
		{
			this.reproductionBank = maxEnergy;
		}

	}

	/** Sets the agent as dead 
	 * @return float */
	public float killAgent()
	{
		dead = true;

		return energy;
	}

	/** Agent type 
	 * @return SimpleAgentType */
	public SimpleAgentType getType()
	{
		return type;
	}

	/** Agents size 
	 * @return float */
	public float getSize()
	{
		return size;
	}

	/** Agents speed 
	 * @return float */
	public float getMaxSpeed()
	{
		return maxSpeed;
	}

	/** Agents dead tag 
	 * @return boolean */
	public boolean isDead()
	{
		return dead;
	}

	/** Decrements the reproduction cost */
	public void decrementReproductionCost()
	{
		reproductionBank = (reproductionBank - reproductionCost);
	}

	/** Get the calculated reproduction cost for this agent 
	 * @return float */
	public float getReproductionCost()
	{
		return reproductionCost;
	}

	/** Get the base reproduction cost 
	 * @return float */
	public float getBaseReproductionCost()
	{
		return baseReproductionCost;
	}

	/** The starting energy for this agent 
	 * @return float */
	public float getStartingEnergy()
	{
		return startingEnergy;
	}

	/** The max energy for this agent 
	 * @return float */
	public float getMaxEnergy()
	{
		return maxEnergy;
	}

	/** This agents hunger threshold 
	 * @return float */
	public float getHungryThreshold()
	{
		return hungryThreshold;
	}

	/** The base movement cost of this agent 
	 * @return float */
	public float getBaseMoveCost()
	{
		return baseMoveCost;
	}

	/** The energy consumption rate for this agent 
	 * @return float */
	public float getEnergyConsumptionRate()
	{
		return energyConsumptionRate;
	}

	/** The digestive efficiency for this agent 
	 * @return float */
	public float getDigestiveEfficency()
	{
		return digestiveEfficency;
	}

	/** The Reproduction Energy Division for this agent 
	 * @return float */
	public float getReproductionEnergyDivision()
	{
		return reproductionEnergyDivision;
	}

	/** Returns if this agent can reproduce 
	 * @return boolean */
	public boolean canReproduce()
	{
		if (reproductionBank > reproductionCost)
		{
			return true;
		}
		return false;
	}

	/** Returns the value of the Agents energy 
	 * @return float */
	public float getEnergy()
	{
		return energy;
	}

	/** Agents view range in front of the agent the agent 
	 * @return float */
	public float getViewRange()
	{
		return viewRange;
	}

	/** View range minus size 
	 * @return float */
	public float getBaseViewRange()
	{
		return viewRange - size;
	}

}
