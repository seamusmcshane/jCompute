package alife;
/**
 * Used to store the internal statistics of the current agent.
 * 
 */
public class SimpleAgentStats
{
	
/* Agent Specific Constants */	
		
		/* Agent Run speed */
		private final float max_speed;
		
		/* Agent Size */
		private final float size;
		
		/* Max Energy of Agent */
		private final float max_energy;
		
		/* Physical Health of Agent */
		private final float max_health;
		
		/* View Range */
		private final float view_range;

/* 	General Statistics */
		
		/* Agent in Simulation Steps */
		private long age;
		
/* Calculated Stats */
		
		/* Rest Need */
		private int rest_steps;
		
		private SimpleAgentType type;
		
	public SimpleAgentStats(SimpleAgentType type,float rus, float sz, float me, float mh, float vr)
	{
		
		this.type = type;
		
		this.max_speed = rus;
		
		this.size = sz;
		
		this.max_energy = me;
		
		this.max_health = mh;
		
		this.age = 0;
		
		this.view_range = size+vr;
		
		calculate_procedural_constants();
	}

	private void calculate_procedural_constants()
	{
		
	}
	
	public SimpleAgentType getType()
	{
		return type;
	}
	
	public float getSize()
	{
		return size;
	}

	public float getView_range()
	{
		return view_range;
	}	
	
	public float getMaxSpeed()
	{
		return max_speed;
	}
}
