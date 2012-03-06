package alife;

public class GenericPlantStats
{
	private float energy;
	private float max_energy;
	private float absorption_rate=0;

	private float max_size;
	private float size;
	
	private int size_div=5;
	
	public GenericPlantStats(float starting_energy, float max_energy, float absorption_rate)
	{		
		max_size = max_energy / size_div;
		
		size = starting_energy / size_div;		
		
		if(starting_energy>max_energy)
		{
			starting_energy = max_energy;
		}
		
		energy = starting_energy;
		
		this.max_energy = max_energy;
		
		this.absorption_rate = absorption_rate;
		
	}
	
	/** Increments the Energy for living plants */
	public void increamentEnergy()
	{
		if(!isDead())
		{
			energy = energy + absorption_rate;
		}
	}
	
	public float decrementEnergy(int num)
	{			
		energy = energy - num;
				
		return num;	
	}
	
	public float getEnergy()
	{			
		return energy;	
	}
	
	/** Return true if the plan is dead */
	public boolean isDead()
	{
		if(energy>0)
		{
			return false;
		}
		
		return true;
	}

	public float getSize()
	{
		return size;
	}
	
}
