package alife;

public class GenericPlantStats
{
	private float energy;
	private float max_size;
	private float size;
	private int size_div=1;
	
	/* Reproduction */
	private float reproductionBank;	
	private float reproductionCost;	
	
	
	private float reproduction_energy_div;
	private float survival_energ_div;
	
	private boolean dead;
	
	/* All Initial Values - used in object clone */
	private int starting_energy;
	private int max_energy;
	private float absorption_rate;
	private String renergy_div;
	private int base_reproduction_cost;

	public GenericPlantStats(int starting_energy, int max_energy, float absorption_rate,String renergy_div, int base_reproduction_cost)
	{				
		dead = false;
		
		max_size = max_energy / size_div;
		
		size = 2;		
		
		this.max_energy = max_energy;
				
		/* Starting Energy */
		if(starting_energy>max_energy)
		{
			starting_energy = max_energy;
		}
		this.starting_energy = starting_energy;	
		energy = starting_energy;
		
		/* Energy Absorption rate from sun */
		if(absorption_rate>max_energy)
		{
			this.absorption_rate = max_energy;
		}		
		this.absorption_rate = absorption_rate;	
		
		/* base_reproduction_cost */		
		this.base_reproduction_cost = base_reproduction_cost;
		
		this.reproductionCost = max_energy * (base_reproduction_cost/100f); // 100 = 1.00 || 90 = 0.90
						
		if(reproductionCost>max_energy)
		{
			this.reproductionCost = max_energy;
		}
		
		this.renergy_div = renergy_div;
		
		calculate_energe_div(renergy_div);
				
	}
	
	private void calculate_energe_div(String renergy_div)
	{	
		if(renergy_div.equals("25:75"))
		{			
			reproduction_energy_div = 0.25f;
			survival_energ_div=0.75f;			
		}
		else if(renergy_div.equals("50:50"))
		{
			reproduction_energy_div = 0.50f;
			survival_energ_div=0.50f;			
		}
		else // renergy_div.equals("75:25")
		{
			reproduction_energy_div = 0.75f;
			survival_energ_div=0.25f;			
		}
		
	}
	
	/** Increments the Energy for living plants */
	public void increment()
	{
		if(!isDead())
		{					
			// Use some to keep us alive (survival_energ_div)
			this.energy = this.energy + (absorption_rate*survival_energ_div);			
			if(this.energy > max_energy)
			{
				this.energy = max_energy;
			}
			
			// Use some to reproduce (reproduction_energy_div)
			this.reproductionBank = this.reproductionBank + (absorption_rate*reproduction_energy_div); 			
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
		else
		{
			System.out.println("isDead");
		}		
		
		return (num/4);	// 1/x efficiency
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
	
	// cost is 1/2 max energy level
	public boolean canReproduce()
	{
		if(reproductionBank > reproductionCost)
		{						
			return true;
		}
		return false;
	}
	
	public void decrementReproductionCost()
	{
		reproductionBank = (reproductionBank - reproductionCost );
	}
	
	
	public GenericPlantStats clone()
	{
		return new GenericPlantStats(starting_energy, max_energy, absorption_rate,renergy_div, base_reproduction_cost);
	}
}
