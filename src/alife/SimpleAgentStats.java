package alife;
/**
 * Used to store the internal statistics of the current agent.
 * 
 */
public class SimpleAgentStats
{
	
		private final float base_move_cost;  // Starting move cost
		
		private boolean dead;
	
/* Agent Specific Constants */	
		
		/* Agent Run speed */
		private final float max_speed;
		
		/* Agent Size */
		private final float size;
		
		/* Max Energy of Agent */
		private final float max_energy;

		private float energy;
		
		private boolean hungry=true;
		
		private float digestive_efficency;
		
		private float hungryThreshold;
		
		/* View Range */
		private final float view_range;
		
		
		/* Reproduction */
		private float reproductionBank;
		
		private float reproductionCost;
		
		private float base_reproduction_cost;

		private float energy_consumption_rate;

/* 	General Statistics */
		
		/* Agent in Simulation Steps */
		private long age;
		
/* Calculated Stats */
				
		private SimpleAgentType type;
		
		private float starting_energy;
		
		private float reproduction_energy_division;
		
	public SimpleAgentStats(SimpleAgentType type,float ms, float sz, float se,float me, float ht, float vr, float base_move_cost,float base_reproduction_cost, float ecr, float de, float red)
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
		
		this.base_move_cost = base_move_cost;
				
		this.base_reproduction_cost =base_reproduction_cost;
		
		this.reproductionCost = max_energy*base_reproduction_cost;
		
		this.energy_consumption_rate = ecr;
		
		this.digestive_efficency = de;
		
		this.reproduction_energy_division = red;
		
		if(reproductionCost>max_energy)
		{
			this.reproductionCost = max_energy; // Agent will most likely die on reproduction
		}

	}
	
	public boolean isHungry()
	{
		return hungry;
	}
	
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
				
		/* This adds energy to the agents survial bank
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
	
	public float killAgent()
	{
		dead = true;
		
		return energy;
	}
	
	public SimpleAgentType getType()
	{
		return type;
	}
	
	public float getSize()
	{
		return size;
	}
	
	public float getSizeSquard()
	{
		return (size*size);
	}

	public float getView_range()
	{
		return view_range;
	}	
	
	public float getBaseView_range()
	{
		return view_range-size;	
	}
	
	public float getViewRangeSquared()
	{
		return (view_range*view_range);
	}	
	
	public float getMaxSpeed()
	{
		return max_speed;
	}
	
	public boolean isDead()
	{
		return dead;
	}
	
	public void decrementReproductionCost()
	{
		reproductionBank = (reproductionBank - reproductionCost );
	}
	
	public float getReproductionCost()
	{
		return reproductionCost;
	}
	
	public float getBaseReproductionCost()
	{
		return base_reproduction_cost;
	}
	
	public float getStartingEnergy()
	{
		return starting_energy;
	}
	
	public float getHungryThreshold()
	{
		return hungryThreshold;
	}
	
	public float getBaseMoveCost()
	{
		return base_move_cost;
	}
	
	public float getEnergyConsumptionRate()
	{
		return energy_consumption_rate;
	}
	
	public float getDigestiveEfficency()
	{
		return digestive_efficency;
	}
	
	public float getReproductionEnergyDivision()
	{
		return reproduction_energy_division;
	}
	
	// cost is 1/2 max energy level
	public boolean canReproduce()
	{
		if(reproductionBank > reproductionCost)
		{						
			return true;
		}
		return false;
	}
}
