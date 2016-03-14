package jCompute.Scenario;

import java.io.File;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

public class ScenarioManager
{
	private static PluginManager spm;
	
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ScenarioManager.class);
	
	public static void init()
	{
		log.info("Loading scenario plugins...");
		
		spm = PluginManagerFactory.createPluginManager();
		
		spm.addPluginsFrom(new File("scenarios/").toURI());
		
		listScenarioPlugins();
	}
	
	private static void listScenarioPlugins()
	{
		// Get all the scenario plugins
		Collection<ScenarioInf> scenarios = new PluginManagerUtil(spm).getPlugins(ScenarioInf.class);
		
		// Look for scenario plugin based on type
		for(ScenarioInf currentScenario : scenarios)
		{
			log.info("Scenario : " + currentScenario.getScenarioType());
		}
		log.info("Total Scenarios : " + scenarios.size());
	}
	
	public static ScenarioInf getScenario(String configText)
	{
		// Load the config text
		ConfigurationInterpreter parser = new ConfigurationInterpreter();
		parser.loadConfig(configText);
		
		// Get the requested scenario type
		String type = parser.getScenarioType();
		
		// Invalid type returned
		if(ScenarioInf.INVALID.equals(type))
		{
			log.error("No scenario type returned");
			
			// avoid searching scenario list for "Invalid" and return null - no scenario now
			return null;
		}
		
		// Get all the scenario plugins
		Collection<ScenarioInf> scenarios = new PluginManagerUtil(spm).getPlugins(ScenarioInf.class);
		
		// Our returned scenario or null
		ScenarioInf scenario = null;
		
		// Look for scenario plugin based on type
		for(ScenarioInf currentScenario : scenarios)
		{
			if(type.equals(currentScenario.getScenarioType()))
			{
				scenario = currentScenario;
				scenario.loadConfig(parser);
			}
		}
		
		log.info("Looking for Scenario Type " + type + " Found " + scenario != null ? scenario.getScenarioType() : null);
		
		return scenario;
	}
	
}
