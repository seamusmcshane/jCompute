package jCompute.Scenario.Math.Mandelbrot;

import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.ConfigurationInterpreter;
import jCompute.Simulation.SimulationScenarioManagerInf;
import jCompute.Stats.StatGroupSetting;

@PluginImplementation
public class MandelbrotScenario implements ScenarioInf
{
	private ConfigurationInterpreter interpreter;
	
	private SimulationScenarioManagerInf simManager;
	
	private MandelbrotSettings settings;
	
	private String type = "Mandelbrot";
	
	public MandelbrotScenario()
	{
		super();
	}
	
	@Override
	public void loadConfig(ConfigurationInterpreter interpreter)
	{
		this.interpreter = interpreter;
		
		readScenarioSettings();
		
		interpreter.readStatSettings();
		
		simManager = new MandelbrotSimulationManager(this);
	}
	
	@Override
	public String getScenarioType()
	{
		return type;
	}
	
	public boolean readScenarioSettings()
	{		
		settings = new MandelbrotSettings();
		
		// Load in all the settings
		loadSettings();
		
		return true;
	}
	
	public void loadSettings()
	{		
		String section;
		
		section = "Kernel";
		
		settings.setTextureSize(interpreter.getIntValue(section, "TextureSize"));
		settings.setIterations(interpreter.getIntValue(section, "Iterations"));
		settings.setComputeMethod(interpreter.getStringValue(section, "ComputeMethod"));
		
		int numCoordinates = interpreter.getSubListSize("Coordinates","Coordinate");

		for(int i=0;i<numCoordinates;i++)
		{
			section = "Coordinates.Coordinate("+i+")";
			
			settings.addCoordinates(interpreter.getDoubleValue(section, "X"), interpreter.getDoubleValue(section, "Y"));
			settings.addZoom(interpreter.getDoubleValue(section, "StartZoom"), interpreter.getDoubleValue(section, "EndZoom"));		
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

	public MandelbrotSettings getSettings()
	{
		return settings;
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
