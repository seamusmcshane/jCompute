package alife;
/**
 * This class holds the stats for an individual agent.
 * It manages the energy of the agent.
 * 	Including all variables acting on the energy.
 * It also manages reproduction energy.
 */
public class SimpleAgentStats
{
	/** The movement cost of the agent before modification */
	private final float base_move_cost;
	
	/** the dead tag for the sim */
	private boolean dead;
		
	/** Agent movement speed */
	private final float max_speed;
	
	/** Agent Size */
	private final float size;
	
	/** Max Energy of Agent */
	private final float max_energy;
	
	/** Current Energy of the agent */
	private float energy;
	
	/** Agents hungry state */
	private boolean hungry=true;
	
	/** The ability of this agent to consume energy */
	private float digestive_efficency;
	
	/** The threshold before this agent is hungry */
	private float hungryThreshold;
	
	/** View Range */
	private float view_range;
	
	/** Reproduction Energy */
	private float reproductionBank;
	
	/** Cost of reproduction */
	private float reproductionCost;
	
	/** Base cost of reproduction */
	private float base_reproduction_cost;
	
	/** The consumption rate of energy from what every the agent is eating (Current only plants) */
	private float energy_consumption_rate;
	
	/** Agent age in Simulation Steps */
	private long age; // not used - todo evolution
	
	/** This agents type Predator/Prey */
	private SimpleAgentType type;
	
	/** The starting energy level */
	private float starting_energy;
	
	/** The ratio of energy to reproduction or survival */
	private float reproduction_energy_division;
		
		
	/**
	 * Creates the stats for this agent.
	 * @param type
	 * @param ms - Max Speed
	 * @param sz - Size
	 * @param se - Starting Energy
	 * @param me - max energy
	 * @param ht - hunger threshold
	 * @param vr - view range
	 * @param bmc - base movement cost
	 * @param brc - base reproduction cost
	 * @param ecr - energy consumption rate
	 * @param de - digestive efficiency
	 * @param red - reproduction energy division
	 */
	public SimpleAgentStats(SimpleAgentType type,float ms, float sz, float se,float me, float ht, float vr, float bmc,float brc, float ecr, float de, float red)
	{
		this.dead = false;
		
		this.type = type;
		
		this.max_speed = ms;
		
		this.size = sz;
		
		this.max_energy = me;
		
		this.reproductionBank = 0;
		
		this.energy = se ; // Starting Energy
		starting_energy = se;
		
		this.hungryThreshold = ht ; // in energy
				
		this.age = 0;
		
		this.view_range = size+vr;
		
		this.base_move_cost = bmc;
				
		this.base_reproduction_cost =brc;
		
		this.reproductionCost = max_energy*brc;
		
		this.energy_consumption_rate = ecr;
		
		this.digestive_efficency = de;
		
		this.reproduction_energy_division = red;
		
		if(reproductionCost>max_energy)
		{
			this.reproductionCost = max_energy; // Agent will most likely die on reproduction
		}

	}
	
	/** Hungry status */
	public boolean isHungry()
	{
		return hungry;
	}
	
	/** Hungry toggle */
	private void updateHunger()
	{
		if(energy<hungryThreshold)
		{
			hungry=true;
		}
		else
		{
			hungry=false;
		}
	}	
	
	/** The movement cost decrementer */
	public void decrementMoveEnergy()
	{
		energy = energy - (size*base_move_cost);
		
		if(energy <= 0 )
		{
			dead = true;
		}
		
		updateHunger();				
	}
	
	/**
	 * This method is in effect the digestive method.
	 * It add energy taking in to account how efficient the agents digestion is.
	 * The energy is split between reproduction energy banks and normal energy.
	 */
	public void addEnergy(float energy)
	{	
		// How efficiently can this agent convert what it eats to energy it can use.
		energy = energy * digestive_efficency;  
				
		/* This adds energy to the agents survival bank
		 * eg assuming reproduction_energy_division = 0.25, energy = 10;
		 * then what is added is 10*0.25 ie 2.5
		 */
		this.energy = this.energy + ( energy * (1*reproduction_energy_division)) ;	
		if(this.energy > max_energy)
		{
			this.energy = max_energy;
		}
		
		/* As above but the remainder of the multiple is added
		 * if energy = 10, and reproduction_energy_division = 0.25
		 * then 1-0.25 = 0.75 , 10*0.75 = 7.5.
		 */
		this.reproductionBank = this.reproductionBank + (energy*(1-reproduction_energy_division));
		if(this.reproductionBank > max_energy)
		{
			this.reproductionBank = max_energy;
		}		
		
	}
	
	/** Sets the agent as dead */
	public float killAgent()
	{
		dead = true;
		
		return energy;
	}
	
	/** Agent type */
	public SimpleAgentType getType()
	{
		return type;
	}
	
	/** Agents size */
	public float getSize()
	{
		return size;
	}
	
	/** Agents view range */
	public float getView_range()
	{
		return view_range;
	}	
	
	/** View range minus size */
	public float getBaseView_range()
	{
		return view_range-size;	
	}
	
	/** View range in squard size */
	public float getViewRangeSquared()
	{
		return (view_range*view_range);
	}	
	
	/** Agents speed */
	public float getMaxSpeed()
	{
		return max_speed;
	}
	
	/** Agents dead tag */
	public boolean isDead()
	{
		return dead;
	}
	
	/** Decrements the reproduction cost */
	public void decrementReproductionCost()
	{
		reproductionBank = (reproductionBank - reproductionCost );
	}
	
	/** Get the calculated reproduction cost for this agent */
	public float getReproductionCost()
	{
		return reproductionCost;
	}
	
	/** Get the base reproduction cost */
	public float getBaseReproductionCost()
	{
		return base_reproduction_cost;
	}
	
	/** The starting energy for this agent */
	public float getStartingEnergy()
	{
		return starting_energy;
	}
	
	/** This agents hunger threshold */
	public float getHungryThreshold()
	{
		return hungryThreshold;
	}
	
	/** The base movement cost of this agent */
	public float getBaseMoveCost()
	{
		return base_move_cost;
	}
	
	/** The energy consumption rate for this agent */
	public float getEnergyConsumptionRate()
	{
		return energy_consumption_rate;
	}
	
	/** The digestive efficiency for this agent */
	public float getDigestiveEfficency()
	{
		return digestive_efficency;
	}
	
	/** The Reproduction Energy Division for this agent */
	public float getReproductionEnergyDivision()
	{
		return reproduction_energy_division;
	}
	
	/** Returns if this agent can reproduce */
	public boolean canReproduce()
	{
		if(reproductionBank > reproductionCost)
		{						
			return true;
		}
		return false;
	}
	
	public float getEnergy()
	{
		return energy;
	}
}
