package alife;
/**
 * This class is used for transferring many parameters into the simulation from the GUI in bulk
 * avoiding the need to pass them in singular.
 *
 */
public class SimpleAgentManagementSetupParam
{
	private float predatorspeed;
	private float preyspeed;	

	private float preyviewrange;
	private float predatorviewrange;
	
	private float preyde;
	private float predatorde;
	
	private float preyrediv;
	private float predatorrediv;
	
	private float preymovecost;
	private float predatormovecost;
	
	private float preyhungerthres;
	private float predatorhungerthres;
	
	private float preyconsumptionrate;
	private float predatorconsumptionrate;
	
	private float preyrepocost;
	private float predrepocost;
	
	private float preystartingenergy;
	private float predstartingenergy;

	
	public SimpleAgentManagementSetupParam()
	{

		
	}
	
	public float getPredatorSpeed()
	{
		return predatorspeed;
	}


	public void setPredatorSpeed(float predatorspeed)
	{
		this.predatorspeed = predatorspeed;
	}


	public float getPreySpeed()
	{
		return preyspeed;
	}


	public void setPreySpeed(float preyspeed)
	{
		this.preyspeed = preyspeed;
	}


	public float getPreyViewRange()
	{
		return preyviewrange;
	}


	public void setPreyViewRange(float preyviewrange)
	{
		this.preyviewrange = preyviewrange;
	}


	public float getPredatorViewRange()
	{
		return predatorviewrange;
	}


	public void setPredatorViewRange(float predatorviewrange)
	{
		this.predatorviewrange = predatorviewrange;
	}


	public float getPreyDE()
	{
		return preyde;
	}


	public void setPreyDE(float preyde)
	{
		this.preyde = preyde;
	}


	public float getPredatorDE()
	{
		return predatorde;
	}


	public void setPredatorDE(float predatorde)
	{
		this.predatorde = predatorde;
	}


	public float getPreyREDiv()
	{
		return preyrediv;
	}


	public void setPreyREDiv(float preyrediv)
	{
		this.preyrediv = preyrediv;
	}


	public float getPredatorREDiv()
	{
		return predatorrediv;
	}


	public void setPredatorREDiv(float predatorrediv)
	{
		this.predatorrediv = predatorrediv;
	}


	public float getPreyMoveCost()
	{
		return preymovecost;
	}


	public void setPreyMoveCost(float preymovecost)
	{
		this.preymovecost = preymovecost;
	}


	public float getPredatorMoveCost()
	{
		return predatormovecost;
	}


	public void setPredatorMoveCost(float predatormovecost)
	{
		this.predatormovecost = predatormovecost;
	}


	public float getPreyHungerThres()
	{
		return preyhungerthres;
	}


	public void setPreyHungerThres(float preyhungerthres)
	{
		this.preyhungerthres = preyhungerthres;
	}


	public float getPredatorHungerThres()
	{
		return predatorhungerthres;
	}


	public void setPredatorHungerThres(float predatorhungerthres)
	{
		this.predatorhungerthres = predatorhungerthres;
	}


	public float getPredatorConsumptionRate()
	{
		return preyconsumptionrate;
	}


	public void setPredatorConsumptionRate(float predatorconsumptionrate)
	{
		this.predatorconsumptionrate = predatorconsumptionrate;
	}
	
	public float getPreyConsumptionRate()
	{
		return preyconsumptionrate;
	}


	public void setPreyConsumptionRate(float preyconsumptionrate)
	{
		this.preyconsumptionrate = preyconsumptionrate;
	}


	public float getPreyRepoCost()
	{
		return preyrepocost;
	}


	public void setPreyRepoCost(float preyrepocost)
	{
		this.preyrepocost = preyrepocost;
	}


	public float getPredRepoCost()
	{
		return predrepocost;
	}


	public void setPredRepoCost(float predrepocost)
	{
		this.predrepocost = predrepocost;
	}


	public float getPreyStartingEnergy()
	{
		return preystartingenergy;
	}


	public void setPreyStartingEnergy(float preystartingenergy)
	{
		this.preystartingenergy = preystartingenergy;
	}


	public float getPredStartingEnergy()
	{
		return predstartingenergy;
	}


	public void setPredStartingEnergy(float predstartingenergy)
	{
		this.predstartingenergy = predstartingenergy;
	}

}
