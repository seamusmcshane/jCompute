package alifeSim.Scenario.Math;

public class LVSettings
{
	private String subType;
	
	private double initial_prey_population = 800;
	private double initial_predator_population = 100;
		
	private double prey_growth = 0.5;
	private double predation_rate = 0.008;
	private double predator_death_rate = 0.8;
	private double predator_conversion_rate = 0.2;

	private int sub_steps = 256;
	private String intType = "RK4";
	
	private float viewScale = 1f;
		
	public double getInitialPreyPopulation()
	{
		return initial_prey_population;
	}

	public void setInitialPreyPopulation(double initial_prey_population)
	{
		this.initial_prey_population = initial_prey_population;
	}

	public double getInitialPredatorPopulation()
	{
		return initial_predator_population;
	}

	public void setInitialPredatorPopulation(double initial_predator_population)
	{
		this.initial_predator_population = initial_predator_population;
	}

	public double getPreyGrowth()
	{
		return prey_growth;
	}

	public void setPreyGrowth(double prey_growth)
	{
		this.prey_growth = prey_growth;
	}

	public double getPredationRate()
	{
		return predation_rate;
	}

	public void setPredationRate(double predation_rate)
	{
		this.predation_rate = predation_rate;
	}

	public double getPredatorDeathRate()
	{
		return predator_death_rate;
	}

	public void setPredatorDeathRate(double predator_death_rate)
	{
		this.predator_death_rate = predator_death_rate;
	}

	public double getPredatorConversionRate()
	{
		return predator_conversion_rate;
	}

	public void setPredatorConversionRate(double predator_conversion_rate)
	{
		this.predator_conversion_rate = predator_conversion_rate;
	}

	public int getSubSteps()
	{
		return sub_steps;
	}

	public void setSubSteps(int sub_steps)
	{
		this.sub_steps = sub_steps;
	}

	public String getIntMethod()
	{
		return intType;
	}

	public void setIntMethod(String intType)
	{
		this.intType = intType;
	}

	public float getViewScale()
	{
		return viewScale;
	}

	public void setViewScale(float viewScale)
	{
		this.viewScale = viewScale;
	}

	public String getSubType()
	{
		return subType;
	}

	public void setSubType(String subType)
	{
		this.subType = subType;
	}

}
