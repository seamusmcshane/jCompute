package jCompute.Scenario.Math.LotkaVolterra;

import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.ScenarioVT;
import jCompute.Simulation.SimulationScenarioManagerInf;

import java.util.HashMap;

public class LotkaVolterraScenario extends ScenarioVT implements ScenarioInf
{
	private SimulationScenarioManagerInf simManager;
	
	private HashMap <String, Double>parameters;
	
	public LotkaVolterraTwoAndThreeSpeciesSettings settings;
	
	public LotkaVolterraScenario()
	{		
		super();
	}
	
	@Override
	public void loadConfig(String text)
	{
		super.loadConfig(text);
		
		readScenarioSettings();
		
		readStatSettings();
		
		simManager = new LotkaVolterraSimulationManager(this);
	}
	
	public boolean readScenarioSettings()
	{
		/* Read sub type */
		String section = "Header";				
		String subType = super.getStringValue(section,"SubType");

		settings = new LotkaVolterraTwoAndThreeSpeciesSettings();

		settings.setSubType(subType);
		System.out.println("SubType : " + settings.getSubType());

		// Load in all the settings
		loadSettings();	
		
		/* View */
		section = "View";				
		settings.setViewScale(super.getFloatValue(section,"Scale"));
		System.out.println("View Scale : " + settings.getViewScale());
		
		/* Integration */
		section = "Integration";				
		settings.setSubSteps(super.getIntValue(section,"SubSteps"));
		System.out.println("SubSteps : " + settings.getSubSteps());
		
		settings.setIntMethod(super.getStringValue(section,"Method"));
		System.out.println("Method : " + settings.getIntMethod());
		
		/* Parameters */		
		if(parameters.containsKey("initial_prey_population"))
		{
			settings.setInitialPreyPopulation(parameters.get("initial_prey_population"));
			System.out.println("InitialPreyPopulation : " + settings.getInitialPreyPopulation());
		}
		
		if(parameters.containsKey("initial_predator_population"))
		{
			settings.setInitialPredatorPopulation(parameters.get("initial_predator_population"));
			System.out.println("InitialPredatorPopulation : " + settings.getInitialPredatorPopulation());
		}
		
		if(parameters.containsKey("predator_conversion_rate"))
		{
			settings.setPredatorConversionRate(parameters.get("predator_conversion_rate"));
			System.out.println("PredatorConversionRate : " + settings.getPredatorConversionRate());
		}
		
		if(parameters.containsKey("predator_death_rate"))
		{
			settings.setPredatorDeathRate(parameters.get("predator_death_rate"));
			System.out.println("PredatorDeathRate : " + settings.getPredatorDeathRate());
		}
		
		if(parameters.containsKey("predator_predation_rate"))
		{
			settings.setPredatorPredationRate(parameters.get("predator_predation_rate"));
			System.out.println("PredationRate : " + settings.getPredatorPredationRate());
		}
		
		/* Not used when three species */
		if(parameters.containsKey("prey_growth"))
		{
			settings.setPreyGrowth(parameters.get("prey_growth"));
			System.out.println("PreyGrowth : " + settings.getPreyGrowth());
		}
		
		/* Three Species */		
		if(parameters.containsKey("initial_plant_population"))
		{
			settings.setInitialPlantPopulation(parameters.get("initial_plant_population"));
			System.out.println("Initial Plant Population : " + settings.getInitialPlantPopulation());
		}
		
		if(parameters.containsKey("plant_growth_rate"))
		{
			settings.setPlantGrowthRate(parameters.get("plant_growth_rate"));
			System.out.println("Plant Growth Rate : " + settings.getPlantGrowthRate());
		}
		
		if(parameters.containsKey("prey_plant_consumption_rate"))
		{
			settings.setPreyPlantConsumptionRate(parameters.get("prey_plant_consumption_rate"));
			System.out.println("Prey Plant Consumption Rate : " + settings.getPreyPlantConsumptionRate());
		}
		
		if(parameters.containsKey("prey_plant_conversion_rate"))
		{
			settings.setPreyPlantConversionRate(parameters.get("prey_plant_conversion_rate"));
			System.out.println("Prey to Plant Conversion Rate : " + settings.getPreyPlantConversionRate());
		}
		
		if(parameters.containsKey("prey_death_rate"))
		{
			settings.setPreyDeathRate(parameters.get("prey_death_rate"));
			System.out.println("Prey Death Rate : " + settings.getPreyDeathRate());
		}

		return true;
	}
	
	public void loadSettings()
	{
		int numParameters = super.getSubListSize("Parameters","Parameter");
		
		parameters = new HashMap<String, Double>();
		
		String section;
		
		for(int i=0;i<numParameters;i++)
		{
			section = "Parameters.Parameter("+i+")";
			
			parameters.put(super.getStringValue(section,"Name"), super.getDoubleValue(section,"Value"));
		}
	}
	
	@Override
	public SimulationScenarioManagerInf getSimulationScenarioManager()
	{
		return simManager;
	}
	
	public String getScenarioText()
	{
		return super.scenarioText;
	}
}
