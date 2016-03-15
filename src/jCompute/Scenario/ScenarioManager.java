package jCompute.Scenario;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.util.FileUtil;

public class ScenarioManager
{
	private static final String PLUGINS_PATH = "plugins" + File.separator + "scenarios";
	
	private static ServiceLoader<ScenarioInf> loader;
	
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ScenarioManager.class);
	
	public static void init()
	{
		// File[] plugins = FileUtil.getFilesInDir();
		
		log.info("Loading scenario plugins...");
		
		URL[] urls = null;
		try
		{
			urls = FileUtil.getFilesInDirAsURLS(PLUGINS_PATH);
			
			for(URL url : urls)
			{
				addUrlToClassPath(url);
			}
		}
		catch(IOException e)
		{
			log.error("Problem loading scenario plugins.");
			
			e.printStackTrace();
		}
		
		URLClassLoader ucl = new URLClassLoader(urls);
		
		loader = ServiceLoader.load(ScenarioInf.class, ucl);
		
		listScenarioPlugins();
	}
	
	public static void addUrlToClassPath(URL url) throws IOException
	{
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class urlClassLoaderClass = URLClassLoader.class;
		
		Method method;
		
		try
		{
			method = urlClassLoaderClass.getDeclaredMethod("addURL", new Class[]
			{
				URL.class
			});
			
			// Unlock Method
			method.setAccessible(true);
			
			// Add path
			method.invoke(sysloader, new Object[]
			{
				url
			});
			
			// Relock Method
			method.setAccessible(false);
		}
		catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			String message = "Error Loading Scenario Plugin";
			
			log.error(message);
			
			Throwable throwable = new Throwable(message);
			
			throwable.setStackTrace(Thread.currentThread().getStackTrace());
			
			throw new IOException(throwable);
		}
		
	}
	
	private static void listScenarioPlugins()
	{
		// Look for scenario plugin based on type
		for(ScenarioInf currentScenario : loader)
		{
			log.info("Scenario : " + currentScenario.getScenarioType());
		}
	}
	
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
		
		log.info("Looking for Scenario Type " + type + " Found " + scenario != null ? scenario.getScenarioType() : null);
		
		return scenario;
	}
	
}
