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
		
	public SimpleAgentStats(SimpleAgentType type,float ms, float sz, float se,float me, float ht, float vr, float base_move_cost,float base_reproduction_cost, float ecr, float de)
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
		
		if(reproductionCost>max_energy)
		{
			this.reproductionCost = max_energy;
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
	
	public void addEnergy(float energy)
	{	
		
		energy = energy*digestive_efficency;  // How efficiently can this agent convert what it eats to energy
		
		energy = energy / 2;
		
		this.energy = this.energy + energy;
		if(this.energy > max_energy)
		{
			this.energy = max_energy;
		}
		
		this.reproductionBank = this.reproductionBank + energy; 
		
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
