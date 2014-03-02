package alifeSim.Alife.SimpleAgent;

import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;

/**
 * This class is used for transferring many parameters into the simulation from the GUI in bulk
 * avoiding the need to pass them in singular.
 *
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentSetupSettings
{
	/* Internal Defaults */
	private AgentType type = AgentType.PREDATOR;
	
	private int initalNumbers=0;
	
	private float size=5f;
	
	private float speed=1f;

	private float viewRange=10f;

	private float digestiveEfficiency=0.5f;

	private float REDiv=0.5f;

	private float moveCost=0.025f;

	private float hungerThres=0.5f;

	private float consumptionRate=0.1f;

	private float reproductionCost=0.9f;

	private float startingEnergy=0.25f;

	public SimpleAgentSetupSettings()
	{

	}

	public AgentType getType()
	{
		return type;
	}
	
	public void setType(AgentType type)
	{
		this.type = type;
	}
	
	public float getSpeed()
	{
		return speed;
	}

	public void setSpeed(float speed)
	{
		this.speed = speed;
	}

	public float getViewRange()
	{
		return this.viewRange;
	}
/*	
	public float getViewRange()
	{
		return viewRange;
	}
*/
	public void setViewRange(float viewRange)
	{
		this.viewRange = viewRange;
	}

	public float getDigestiveEfficiency()
	{
		return digestiveEfficiency;
	}

	public void setDigestiveEfficiency(float digestiveEfficiency)
	{
		this.digestiveEfficiency = digestiveEfficiency;
	}

	public float getREDiv()
	{
		return REDiv;
	}

	public void setREDiv(float rEDiv)
	{
		REDiv = rEDiv;
	}

	public float getMoveCost()
	{
		return moveCost;
	}

	public void setMoveCost(float moveCost)
	{
		this.moveCost = moveCost;
	}

	public float getHungerThres()
	{
		return hungerThres;
	}

	public void setHungerThres(float hungerThres)
	{
		this.hungerThres = hungerThres;
	}

	public float getConsumptionRate()
	{
		return consumptionRate;
	}

	public void setConsumptionRate(float consumptionRate)
	{
		this.consumptionRate = consumptionRate;
	}

	public float getReproductionCost()
	{
		return reproductionCost;
	}

	public void setReproductionCost(float reproductionCost)
	{
		this.reproductionCost = reproductionCost;
	}

	public float getStartingEnergy()
	{
		return startingEnergy;
	}

	public void setStartingEnergy(float startingEnergy)
	{
		this.startingEnergy = startingEnergy;
	}

	public int getInitalNumbers()
	{
		return initalNumbers;
	}

	public void setInitalNumbers(int initalNumbers)
	{
		this.initalNumbers = initalNumbers;
	}

	public float getSize()
	{
		return size;
	}

	public void setSize(float size)
	{
		this.size = size;
	}

}
