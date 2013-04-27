package alifeSim.Alife.SimpleAgent;
/**
 * This class is used for transferring many parameters into the simulation from the GUI in bulk
 * avoiding the need to pass them in singular.
 *
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentSetupSettings
{
	private int initalNumbers;
	
	private float speed;

	private float viewRange;

	private float digestiveEfficiency;

	private float REDiv;

	private float moveCost;

	private float hungerThres;

	private float consumptionRate;

	private float reproductionCost;

	private float startingEnergy;

	public SimpleAgentSetupSettings()
	{

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
		return viewRange;
	}

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

}
