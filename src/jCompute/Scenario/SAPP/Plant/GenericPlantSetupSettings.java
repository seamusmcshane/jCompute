package jCompute.Scenario.SAPP.Plant;

public class GenericPlantSetupSettings
{
	/* Plant Numbers */
	private int initialPlantNumbers=0;

	/* Plant Regeneration Rate */
	private int plantRegenRate=0;
	
	/* Plant Regeneration ever N step[s */
	private int plantRegenerationNSteps=1;

	/* Plant Energy Absorption Rate */
	private int plantEnergyAbsorptionRate=0;

	/* Plant Starting Energy */
	private int plantStartingEnergy=0;

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

	public int getPlantRegenerationNSteps()
	{
		return plantRegenerationNSteps;
	}

	public void setPlantRegenerationNSteps(int plantRegenerationNSteps)
	{
		this.plantRegenerationNSteps = plantRegenerationNSteps;
	}
}
