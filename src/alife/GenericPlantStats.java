package alife;

import org.newdawn.slick.geom.Vector2f;

public class GenericPlantStats
{
	private float energy;
	private float max_energy;
	private float absorption_rate=0;

	private float max_size;
	
	private float size;
	
	private int size_div=1;
	
	private boolean dead;
	
	/* Reproduction */
	private float reproductionBank;	
	private float reproductionCost;	
	private float base_reproduction_cost;
	
	
	public GenericPlantStats(float starting_energy, float max_energy, float absorption_rate, float base_reproduction_cost)
	{		
		
		dead = false;
		
		max_size = max_energy / size_div; // Not used
		
		size = 3;		
		
		if(starting_energy>max_energy)
		{
			starting_energy = max_energy;
		}
		
		energy = starting_energy;
		
		this.max_energy = max_energy;
		
		this.absorption_rate = absorption_rate;
		
		this.base_reproduction_cost = base_reproduction_cost;
		
		this.reproductionCost = max_energy*base_reproduction_cost;
				
		if(reproductionCost>max_energy)
		{
			this.reproductionCost = max_energy;
		}
		
	}
	
	/** Increments the Energy for living plants */
	public void increment()
	{
		if(!isDead())
		{
			
			this.energy = this.energy + (absorption_rate/2);
			
			if(this.energy > max_energy)
			{
				this.energy = max_energy;
			}
			
			this.reproductionBank = this.reproductionBank + (absorption_rate/2);
			
			if(this.reproductionBank > max_energy)
			{
				this.reproductionBank = max_energy;
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

		
		return num;
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
	
	public void decrementReproductionCost()
	{
		reproductionBank = (reproductionBank - reproductionCost );
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
