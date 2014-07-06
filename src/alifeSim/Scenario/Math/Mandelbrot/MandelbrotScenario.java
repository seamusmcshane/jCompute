package alifeSim.Scenario.Math.Mandelbrot;

import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Simulation.SimulationScenarioManagerInf;

public class MandelbrotScenario extends ScenarioVT implements ScenarioInf
{
	private SimulationScenarioManagerInf simManager;
	
	private MandelbrotSettings settings;
	
	public MandelbrotScenario()
	{
		super();
	}
	
	@Override
	public void loadConfig(String text)
	{
		super.loadConfig(text);
		
		readScenarioSettings();
		
		readStatSettings();
		
		simManager = new MandelbrotSimulationManager(this);
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
		
		settings.setTextureSize(super.getIntValue(section, "TextureSize"));
		settings.setIterations(super.getIntValue(section, "Iterations"));
		settings.setComputeMethod(super.getStringValue(section, "ComputeMethod"));
		
		int numCoordinates = super.getSubListSize("Coordinates","Coordinate");

		for(int i=0;i<numCoordinates;i++)
		{
			section = "Coordinates.Coordinate("+i+")";
			
			settings.addCoordinates(super.getDoubleValue(section, "X"), super.getDoubleValue(section, "Y"));
			settings.addZoom(super.getDoubleValue(section, "StartZoom"), super.getDoubleValue(section, "EndZoom"));		
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

	public MandelbrotSettings getSettings()
	{
		return settings;
	}
	
}
