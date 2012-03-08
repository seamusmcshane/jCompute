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
		
		/* View Range */
		private final float view_range;

/* 	General Statistics */
		
		/* Agent in Simulation Steps */
		private long age;
		
/* Calculated Stats */
				
		private SimpleAgentType type;
		
	public SimpleAgentStats(SimpleAgentType type,float ms, float sz, float me, float vr,float base_move_cost)
	{
		this.dead = false;
		
		this.type = type;
		
		this.max_speed = ms;
		
		this.size = sz;
		
		this.max_energy = me;
		
		this.energy = max_energy /2 ;
		
		this.age = 0;
		
		this.view_range = size+vr;
		
		this.base_move_cost = base_move_cost;

	}
	
	public void decrementMoveEnergy()
	{
		//System.out.println("cost to move : " + (size*base_move_cost));
		energy = energy - (size*base_move_cost);

		//System.out.println("energy : " + energy);
		
		if(energy <= 0 )
		{
			dead = true;
			
			//System.out.println("DEAD!");
		}
				
	}
	
	public void addEnergy(float energy)
	{
		this.energy = this.energy + energy;
		
		if(this.energy > max_energy)
		{
			this.energy = max_energy;
		}
		
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
}
