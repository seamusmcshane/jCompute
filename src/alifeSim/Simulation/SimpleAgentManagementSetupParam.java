package alifeSim.Simulation;
/**
 * This class is used for transferring many parameters into the simulation from the GUI in bulk
 * avoiding the need to pass them in singular.
 *
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentManagementSetupParam
{
	private float predatorSpeed;
	private float preySpeed;

	private float preyViewRange;
	private float predatorViewRange;

	private float preyDE;
	private float predatorDE;

	private float preyREDiv;
	private float predatorREDiv;

	private float preyMoveCost;
	private float predatorMoveCost;

	private float preyHungerThres;
	private float predatorHungerThres;

	private float preyConsumptionRate;
	@SuppressWarnings("unused")
	private float predatorConsumptionRate;

	private float preyRepoCost;
	private float predRepoCost;

	private float preyStartingEnergy;
	private float predStartingEnergy;

	public SimpleAgentManagementSetupParam()
	{

	}

	/**
	 * Method getPredatorSpeed.
	 * @return float */
	public float getPredatorSpeed()
	{
		return predatorSpeed;
	}

	/**
	 * Method setPredatorSpeed.
	 * @param predatorSpeed float
	 */
	public void setPredatorSpeed(float predatorSpeed)
	{
		this.predatorSpeed = predatorSpeed;
	}

	/**
	 * Method getPreySpeed.
	 * @return float */
	public float getPreySpeed()
	{
		return preySpeed;
	}

	/**
	 * Method setPreySpeed.
	 * @param preySpeed float
	 */
	public void setPreySpeed(float preySpeed)
	{
		this.preySpeed = preySpeed;
	}

	/**
	 * Method getPreyViewRange.
	 * @return float */
	public float getPreyViewRange()
	{
		return preyViewRange;
	}

	/**
	 * Method setPreyViewRange.
	 * @param preyViewRange float
	 */
	public void setPreyViewRange(float preyViewRange)
	{
		this.preyViewRange = preyViewRange;
	}

	/**
	 * Method getPredatorViewRange.
	 * @return float */
	public float getPredatorViewRange()
	{
		return predatorViewRange;
	}

	/**
	 * Method setPredatorViewRange.
	 * @param predatorViewRange float
	 */
	public void setPredatorViewRange(float predatorViewRange)
	{
		this.predatorViewRange = predatorViewRange;
	}

	/**
	 * Method getPreyDE.
	 * @return float */
	public float getPreyDE()
	{
		return preyDE;
	}

	/**
	 * Method setPreyDE.
	 * @param preyDE float
	 */
	public void setPreyDE(float preyDE)
	{
		this.preyDE = preyDE;
	}

	/**
	 * Method getPredatorDE.
	 * @return float */
	public float getPredatorDE()
	{
		return predatorDE;
	}

	/**
	 * Method setPredatorDE.
	 * @param predatorDE float
	 */
	public void setPredatorDE(float predatorDE)
	{
		this.predatorDE = predatorDE;
	}

	/**
	 * Method getPreyREDiv.
	 * @return float */
	public float getPreyREDiv()
	{
		return preyREDiv;
	}

	/**
	 * Method setPreyREDiv.
	 * @param preyREDiv float
	 */
	public void setPreyREDiv(float preyREDiv)
	{
		this.preyREDiv = preyREDiv;
	}

	/**
	 * Method getPredatorREDiv.
	 * @return float */
	public float getPredatorREDiv()
	{
		return predatorREDiv;
	}

	/**
	 * Method setPredatorREDiv.
	 * @param predatorREDiv float
	 */
	public void setPredatorREDiv(float predatorREDiv)
	{
		this.predatorREDiv = predatorREDiv;
	}

	/**
	 * Method getPreyMoveCost.
	 * @return float */
	public float getPreyMoveCost()
	{
		return preyMoveCost;
	}

	/**
	 * Method setPreyMoveCost.
	 * @param preyMoveCost float
	 */
	public void setPreyMoveCost(float preyMoveCost)
	{
		this.preyMoveCost = preyMoveCost;
	}

	/**
	 * Method getPredatorMoveCost.
	 * @return float */
	public float getPredatorMoveCost()
	{
		return predatorMoveCost;
	}

	/**
	 * Method setPredatorMoveCost.
	 * @param predatorMoveCost float
	 */
	public void setPredatorMoveCost(float predatorMoveCost)
	{
		this.predatorMoveCost = predatorMoveCost;
	}

	/**
	 * Method getPreyHungerThres.
	 * @return float */
	public float getPreyHungerThres()
	{
		return preyHungerThres;
	}

	/**
	 * Method setPreyHungerThres.
	 * @param preyHungerThres float
	 */
	public void setPreyHungerThres(float preyHungerThres)
	{
		this.preyHungerThres = preyHungerThres;
	}

	/**
	 * Method getPredatorHungerThres.
	 * @return float */
	public float getPredatorHungerThres()
	{
		return predatorHungerThres;
	}

	/**
	 * Method setPredatorHungerThres.
	 * @param predatorHungerThres float
	 */
	public void setPredatorHungerThres(float predatorHungerThres)
	{
		this.predatorHungerThres = predatorHungerThres;
	}

	/**
	 * Method getPredatorConsumptionRate.
	 * @return float */
	public float getPredatorConsumptionRate()
	{
		return predatorConsumptionRate;
	}

	/**
	 * Method setPredatorConsumptionRate.
	 * @param predatorConsumptionRate float
	 */
	public void setPredatorConsumptionRate(float predatorConsumptionRate)
	{
		this.predatorConsumptionRate = predatorConsumptionRate;
	}

	/**
	 * Method getPreyConsumptionRate.
	 * @return float */
	public float getPreyConsumptionRate()
	{
		return preyConsumptionRate;
	}

	/**
	 * Method setPreyConsumptionRate.
	 * @param preyConsumptionRate float
	 */
	public void setPreyConsumptionRate(float preyConsumptionRate)
	{
		this.preyConsumptionRate = preyConsumptionRate;
	}

	/**
	 * Method getPreyRepoCost.
	 * @return float */
	public float getPreyRepoCost()
	{
		return preyRepoCost;
	}

	/**
	 * Method setPreyRepoCost.
	 * @param preyRepoCost float
	 */
	public void setPreyRepoCost(float preyRepoCost)
	{
		this.preyRepoCost = preyRepoCost;
	}

	/**
	 * Method getPredRepoCost.
	 * @return float */
	public float getPredRepoCost()
	{
		return predRepoCost;
	}

	/**
	 * Method setPredRepoCost.
	 * @param predRepoCost float
	 */
	public void setPredRepoCost(float predRepoCost)
	{
		this.predRepoCost = predRepoCost;
	}

	/**
	 * Method getPreyStartingEnergy.
	 * @return float */
	public float getPreyStartingEnergy()
	{
		return preyStartingEnergy;
	}

	/**
	 * Method setPreyStartingEnergy.
	 * @param preyStartingEnergy float
	 */
	public void setPreyStartingEnergy(float preyStartingEnergy)
	{
		this.preyStartingEnergy = preyStartingEnergy;
	}

	/**
	 * Method getPredStartingEnergy.
	 * @return float */
	public float getPredStartingEnergy()
	{
		return predStartingEnergy;
	}

	/**
	 * Method setPredStartingEnergy.
	 * @param predStartingEnergy float
	 */
	public void setPredStartingEnergy(float predStartingEnergy)
	{
		this.predStartingEnergy = predStartingEnergy;
	}

}
