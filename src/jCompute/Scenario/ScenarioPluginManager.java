package jCompute.Scenario;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jCompute.util.FileUtil;

public class ScenarioPluginManager
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ScenarioPluginManager.class);
	
	/** Plug-in location */
	private static final String PLUGINS_PATH = "plugins" + File.separator + "scenarios";
	
	/** Service loaded */
	private static ServiceLoader<ScenarioInf> loader;
	
	/**
	 * Loads all scenario plug-in from the plug-in directory.
	 * 
	 * @throws IOException
	 * If there is a problem with the plug-ins path.
	 */
	public static void loadPlugins() throws IOException
	{
		log.info("Loading scenario plugins...");
		
		try
		{
			URL[] urls = FileUtil.getFilesInDirAsURLS(PLUGINS_PATH);
			
			@SuppressWarnings("resource")
			URLClassLoader ucl = URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());
			
			loader = ServiceLoader.load(ScenarioInf.class, ucl);
			
			for(ScenarioInf currentScenario : loader)
			{
				log.info("Scenario : " + currentScenario.getScenarioType());
			}
		}
		catch(IOException e)
		{
			log.error("Problem loading scenario plugins.");
			
			throw(e);
		}
	}
	
	/**
	 * Checks if a scenario plug-in is loaded matching the target type.
	 * 
	 * @param targetType
	 * @return
	 * true if the type is found.
	 * false if the type is not found.
	 */
	public static boolean hasScenario(String targetType)
	{
		boolean status = false;
		
		// Look for a scenario with a type matching targetType
		for(ScenarioInf currentScenario : loader)
		{
			if(currentScenario.getScenarioType().equals(targetType))
			{
				// Found target
				status = true;
				break;
			}
		}
		
		return status;
	}
	
	/**
	 * Creates a scenario.
	 * 
	 * @param configText
	 * @return
	 * Returns a scenario based on the configuration text.
	 * or null if configuration is invalid or there is no matching scenario plug-in.
	 */
	public static ScenarioInf getScenario(String configText)
	{
		// Load the configuration text
		ConfigurationInterpreter parser = new ConfigurationInterpreter();
		
		// Is the configuration valid
		if(!parser.loadConfig(configText))
		{
			log.error("Invalid configuration text");
			// Invalid configuration and thus no scenario
			return null;
		}
		
		// Get the requested scenario type
		String type = parser.getScenarioType();
		
		// Invalid type returned
		if(ScenarioInf.INVALID.equals(type))
		{
			log.error("No scenario type returned");
			
			// avoid searching scenario list for "Invalid" and return null - no scenario now
			return null;
		}
		
		// Our returned scenario or null
		ScenarioInf scenario = null;
		
		// Look for scenario plugin based on type
		for(ScenarioInf currentScenario : loader)
		{
			if(type.equals(currentScenario.getScenarioType()))
			{
				scenario = currentScenario;
				scenario.loadConfig(parser);
			}
		}
		
		log.info(("Looking for Scenario Type " + type + " Found " + scenario) != null ? scenario.getScenarioType() : null);
		
		return scenario;
	}
}