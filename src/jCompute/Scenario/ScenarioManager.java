package jCompute.Scenario;

import java.io.File;
import java.util.Collection;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

public class ScenarioManager
{
	private static PluginManager spm;

	public static void init()
	{
		spm = PluginManagerFactory.createPluginManager();

		spm.addPluginsFrom(new File("scenarios/").toURI());
	}

	public static ScenarioInf getScenario(String configText)
	{
		// Load the config text
		ScenarioVT parser = new ScenarioVT();
		parser.loadConfig(configText);

		// Get the requested scenario type
		String type = parser.getScenarioType();
		System.out.println("Type : " + type);

		// Get all the scenario plugins
		Collection<ScenarioInf> scenarios = new PluginManagerUtil(spm).getPlugins(ScenarioInf.class);

		// Our returned scenario or null
		ScenarioInf scenario = null;
		
		System.out.println("scenarios " + scenarios.size());

		
		// Look for scenario plugin based on type
		for (ScenarioInf currentScenario : scenarios)
		{
			if(type.equals(currentScenario.getScenarioType()))
			{
				scenario = currentScenario;
				scenario.loadConfig(configText);
			}

		}

		return scenario;
	}

	/*
	 * public static ScenarioInf getScenario(String text) { ScenarioVT
	 * scenarioParser = null;
	 * 
	 * ScenarioInf simScenario = null;
	 * 
	 * scenarioParser = new ScenarioVT();
	 * 
	 * // To get the type of Scenario object to create.
	 * scenarioParser.loadConfig(text);
	 * 
	 * log.debug("Scenario Type : " + scenarioParser.getScenarioType());
	 * 
	 * if (scenarioParser.getScenarioType().equalsIgnoreCase("SAPP")) {
	 * log.debug("SAPP File"); simScenario = new SAPPScenario();
	 * 
	 * simScenario.loadConfig(text);
	 * 
	 * } else if (scenarioParser.getScenarioType().equalsIgnoreCase("LV")) {
	 * log.debug("LV File"); simScenario = new LotkaVolterraScenario();
	 * 
	 * simScenario.loadConfig(text); } else {
	 * log.error("DeterminScenarios :UKNOWN"); }
	 * 
	 * return simScenario; }
	 */

}
