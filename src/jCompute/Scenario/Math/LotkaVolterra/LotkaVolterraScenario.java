package jCompute.Scenario.Math.LotkaVolterra;

import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.ConfigurationInterpreter;
import jCompute.Simulation.SimulationScenarioManagerInf;
import jCompute.Stats.StatGroupSetting;

import java.util.HashMap;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class LotkaVolterraScenario implements ScenarioInf
{
	private ConfigurationInterpreter interpreter;
	
	private SimulationScenarioManagerInf simManager;
	
	private HashMap <String, Double>parameters;
	
	public LotkaVolterraTwoAndThreeSpeciesSettings settings;
	
	private String type = "LV";
	
	public LotkaVolterraScenario()
	{		
		super();
	}
		
	@Override
	public void loadConfig(ConfigurationInterpreter interpreter)
	{
		this.interpreter = interpreter;
		
		readScenarioSettings();
		
		interpreter.readStatSettings();
		
		simManager = new LotkaVolterraSimulationManager(this);
	}
	
	@Override
	public String getScenarioType()
	{
		return type;
	}
	
	private boolean readScenarioSettings()
	{
		/* Read sub type */
		String section = "Header";				
		String subType = interpreter.getStringValue(section,"SubType");

		settings = new LotkaVolterraTwoAndThreeSpeciesSettings();

		settings.setSubType(subType);
		System.out.println("SubType : " + settings.getSubType());

		// Load in all the settings
		loadSettings();	
		
		/* View */
		section = "View";				
		settings.setViewScale(interpreter.getFloatValue(section,"Scale"));
		System.out.println("View Scale : " + settings.getViewScale());
		
		/* Integration */
		section = "Integration";				
		settings.setSubSteps(interpreter.getIntValue(section,"SubSteps"));
		System.out.println("SubSteps : " + settings.getSubSteps());
		
		settings.setIntMethod(interpreter.getStringValue(section,"Method"));
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
		int numParameters = interpreter.getSubListSize("Parameters","Parameter");
		
		parameters = new HashMap<String, Double>();
		
		String section;
		
		for(int i=0;i<numParameters;i++)
		{
			section = "Parameters.Parameter("+i+")";
			
			parameters.put(interpreter.getStringValue(section,"Name"), interpreter.getDoubleValue(section,"Value"));
		}
	}
	
	@Override
	public SimulationScenarioManagerInf getSimulationScenarioManager()
	{
		return simManager;
	}
	
	public String getScenarioText()
	{
		return interpreter.getText();
	}
	
	@Override
	public double getScenarioVersion()
	{
		return interpreter.getFileVersion();
	}
	
	@Override
	public List<StatGroupSetting> getStatGroupSettingsList()
	{
		return interpreter.getStatGroupSettingsList();
	}

	@Override
	public boolean endEventIsSet(String eventName)
	{
		return interpreter.endEventIsSet(eventName);
	}

	@Override
	public int getEndEventTriggerValue(String eventName)
	{
		return interpreter.getEventValue(eventName);
	}
}
