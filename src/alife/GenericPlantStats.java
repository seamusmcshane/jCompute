package alife;
/**
 * This class holds the stats for an individual plant.
 * It manages the energy of the plant.
 * 	Including all variables acting on the energy.
 * It also manages reproduction energy.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class GenericPlantStats
{
	private float energy;
	private float maxEnergy;
	private float absorptionRate=0;

	@SuppressWarnings("unused")
	private float maxSize; 		// TODO - Allow plants to increase in size, based on energy value?
	
	private float size;
	
	private int sizeDiv=1;
	
	private boolean dead;
	
	/* Reproduction - DISABLED */
	private float reproductionBank;	
	private float reproductionCost;	
	
	@SuppressWarnings("unused")
	private float baseReproductionCost;
	
	/**
	 * Constructor for GenericPlantStats.
	 * @param startingEnergy float
	 * @param maxEnergy float
	 * @param absorptionRate float
	 * @param baseReproductionCost float
	 */
	public GenericPlantStats(float startingEnergy, float maxEnergy, float absorptionRate, float baseReproductionCost)
	{		
		
		dead = false;
		
		maxSize = maxEnergy / sizeDiv; // Not used size div = 1
		
		/* hard coded sizes for now - planned is - plants size changes with growth */
		size = 1;		
		
		if(startingEnergy>maxEnergy)
		{
			startingEnergy = maxEnergy;
		}
		
		energy = startingEnergy;
		
		this.maxEnergy = maxEnergy;
		
		this.absorptionRate = absorptionRate;
		
		this.baseReproductionCost = baseReproductionCost;
		
		this.reproductionCost = maxEnergy*baseReproductionCost;
				
		if(reproductionCost>maxEnergy)
		{
			this.reproductionCost = maxEnergy;
		}
		
	}
	
	/** Increments the Energy for living plants 
	 * Taking into account reproduction division. (DISABLED)
	 * */
	public void increment()
	{
		if(!isDead())
		{
			
			this.energy = this.energy + (absorptionRate);
//			this.energy = this.energy + (absorptionRate/2);
			
			if(this.energy > maxEnergy)
			{
				this.energy = maxEnergy;
			}
			
			/*this.reproductionBank = this.reproductionBank + (absorptionRate/2);
			
			if(this.reproductionBank > maxEnergy)
			{
				this.reproductionBank = maxEnergy;
			}	*/
		}
	}
	
	/**
	 * Remove energy from living plants 
	 * Happens when the plant is being ate.
	 * @param num float
	 * 	
	 * @return float */
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
	
	/** Returns the total energy value of this plant
	 * 	
	 * @return float */
	public float getEnergy()
	{			
		return energy;	
	}
	
	/** 
	 * Returns the max energy value this plant can achieve
	 * @return float */
	public float getMaxEnergy()
	{			
		return maxEnergy;	
	}
	
	/** 
	 * Returns the energy absorption rate this plant can has 
	 * @return float */
	public float getAbsorptionRate()
	{			
		return absorptionRate;	
	}
	
	/** 
	 * Returns the energy reproduction cost of this plant 
	 * 
	 * @return float */
	public float getBaseReproductionCost()
	{			
		return baseReproductionCost;	
	}
	
	/** 
	 * Return true if the plan is dead 
	 * 
	 * @return boolean */
	public boolean isDead()
	{
		return dead;
	}

	/** 
	 * Returns this size of this plant
	 *  
	 * @return float */
	public float getSize()
	{
		return size;
	}
	
	/** 
	 * Performs the reproduction cost calculation */
	public void decrementReproductionCost()
	{
		reproductionBank = (reproductionBank - reproductionCost );
	}

	/** 
	 * Returns if this plant can reproduce 
	 * 
	 * @return boolean */
	public boolean canReproduce()
	{
		if(reproductionBank > reproductionCost)
		{						
			return true;
		}
		return false;
	}
		
}
