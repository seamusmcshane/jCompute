package alifeSim.Scenario.Math;

import java.util.HashMap;

import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Simulation.SimulationScenarioManagerInf;

public class LVScenario extends ScenarioVT implements ScenarioInf
{
	private SimulationScenarioManagerInf simManager;
	
	private HashMap <String, Double>parameters;
	
	public LVSettings settings;
	
	public LVScenario()
	{		
		super();
	}
	
	@Override
	public void loadConfig(String text)
	{
		super.loadConfig(text);
		
		readScenarioSettings();
		
		readStatSettings();
		
		simManager = new LVSimulationManager(this);
	}
	
	public boolean readScenarioSettings()
	{
		settings = new LVSettings();
		
		loadSettings();
		
		/* Sub Type */
		String section = "Header";				
		settings.setSubType(super.getStringValue(section,"SubType"));
		System.out.println("SubType : " + settings.getSubType());
		
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
		
		if(parameters.containsKey("predation_rate"))
		{
			settings.setPredationRate(parameters.get("predation_rate"));
			System.out.println("PredationRate : " + settings.getPredationRate());
		}
		
		if(parameters.containsKey("prey_growth"))
		{
			settings.setPreyGrowth(parameters.get("prey_growth"));
			System.out.println("PreyGrowth : " + settings.getPreyGrowth());
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
