package alifeSim.Alife.GenericPlant;

public class GenericPlantSetupSettings
{
	/* Plant Numbers */
	private int initialPlantNumbers;

	/* Plant Regeneration Rate */
	private int plantRegenRate;

	/* Plant Energy Absorption Rate */
	private int plantEnergyAbsorptionRate;

	/* Plant Starting Energy */
	private int plantStartingEnergy;

	public int getInitialPlantNumbers()
	{
		return initialPlantNumbers;
	}

	public void setInitialPlantNumbers(int initialPlantNumbers)
	{
		this.initialPlantNumbers = initialPlantNumbers;
	}

	public int getPlantRegenRate()
	{
		return plantRegenRate;
	}

	public void setPlantRegenRate(int plantRegenRate)
	{
		this.plantRegenRate = plantRegenRate;
	}

	public int getPlantEnergyAbsorptionRate()
	{
		return plantEnergyAbsorptionRate;
	}

	public void setPlantEnergyAbsorptionRate(int plantEnergyAbsorptionRate)
	{
		this.plantEnergyAbsorptionRate = plantEnergyAbsorptionRate;
	}

	public int getPlantStartingEnergy()
	{
		return plantStartingEnergy;
	}

	public void setPlantStartingEnergy(int plantStartingEnergy)
	{
		this.plantStartingEnergy = plantStartingEnergy;
	}
}
