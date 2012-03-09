package alife;
/**
 * Used to store the internal statistics of the current agent.
 * 
 */
public class SimpleAgentStats
{
	
		private final float base_move_cost;
		
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
		
		private float hungryThreshold;
		
		/* View Range */
		private final float view_range;
		
		
		/* Reproduction */
		private float reproductionBank;
		
		private float reproductionCost;
		
		private float base_reproduction_cost;


/* 	General Statistics */
		
		/* Agent in Simulation Steps */
		private long age;
		
/* Calculated Stats */
				
		private SimpleAgentType type;
		
	public SimpleAgentStats(SimpleAgentType type,float ms, float sz, float me, float vr,float base_move_cost,float base_reproduction_cost)
	{
		this.dead = false;
		
		this.type = type;
		
		this.max_speed = ms;
		
		this.size = sz;
		
		this.max_energy = me;
		
		this.reproductionBank = 0;
		
		this.energy = max_energy / 10 ;
		
		this.hungryThreshold = max_energy / 2;
		
		this.age = 0;
		
		this.view_range = size+vr;
		
		this.base_move_cost = base_move_cost;
				
		this.base_reproduction_cost =base_reproduction_cost;
		
		this.reproductionCost = max_energy*base_reproduction_cost;
				
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
		
		return (energy);
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
