package alife;

public class GenericPlantStats
{
	private float energy;
	private float max_energy;
	private float absorption_rate=0;

	private float max_size;
	
	private float size;
	
	private int size_div=1;
	
	private boolean dead;
	
	public GenericPlantStats(float starting_energy, float max_energy, float absorption_rate)
	{		
		
		dead = false;
		
		max_size = max_energy / size_div;
		
		size = 1;		
		
		if(starting_energy>max_energy)
		{
			starting_energy = max_energy;
		}
		
		energy = starting_energy;
		
		this.max_energy = max_energy;
		
		this.absorption_rate = absorption_rate;
		
	}
	
	/** Increments the Energy for living plants */
	public void increment()
	{
		if(!isDead())
		{
			energy = energy + absorption_rate;
			
			if(energy>max_energy)
			{
				energy = max_energy;
			}
		}
	}
	
	/* Remove energy from living plants */
	public float decrementEnergy(float num)
	{		
		
		if(!isDead())
		{
			if( (energy - num) <= 0) // if we take more energy that what is left.. take all the energy
			{
				num = energy;
				
				energy = 0;
				
				dead = true; // plant has been killed
				
			}
			else
			{
				energy = energy - num; // else take the amount we tried for
			}		
		}

		
		return (num);	// 1/x efficiency
	}
	
	public float getEnergy()
	{			
		return energy;	
	}
	
	/** Return true if the plan is dead */
	public boolean isDead()
	{
		return dead;
	}

	public float getSize()
	{
		return size;
	}
	
}
