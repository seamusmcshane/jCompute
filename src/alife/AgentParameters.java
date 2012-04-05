package alife;

public class AgentParameters
{

	int speed;
	int view_range;
	int digestiveefficiency;
	int reproductionEnergyDiv;
	int movementcost;
	int hungerthreshold;
	int consumptionrate;
	int startingenergy;
	int reproductioncost;
	
	public AgentParameters(int speed, int view_range, int digestiveefficiency, int reproductionEnergyDiv,int movementcost,int hungerthreshold,int consumptionrate,int startingenergy,int reproductioncost)
	{
		this.speed = speed;
		this.view_range = view_range;
		this.digestiveefficiency = digestiveefficiency;
		this.reproductionEnergyDiv = reproductionEnergyDiv;
		this.movementcost = movementcost;
		this.hungerthreshold = hungerthreshold;
		this.consumptionrate = consumptionrate;
		this.startingenergy = startingenergy;
		this.reproductioncost = reproductioncost;
	}
	
	public int getSpeed()
	{
		return speed;
	}

	public int getView_range()
	{
		return view_range;
	}

	public int getDigestiveefficiency()
	{
		return digestiveefficiency;
	}

	public int getReproductionEnergyDiv()
	{
		return reproductionEnergyDiv;
	}

	public int getMovementcost()
	{
		return movementcost;
	}

	public int getHungerthreshold()
	{
		return hungerthreshold;
	}

	public int getConsumptionrate()
	{
		return consumptionrate;
	}

	public int getStartingenergy()
	{
		return startingenergy;
	}

	public int getReproductioncost()
	{
		return reproductioncost;
	}


	
}
