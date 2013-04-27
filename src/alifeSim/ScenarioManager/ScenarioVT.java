package alifeSim.ScenarioManager;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.Wini;

public class ScenarioVT implements ScenarioInf
{
	Ini scenario;

	public ScenarioVT(File file)
	{
		try
		{
			scenario = new Wini(file);
		}
		catch (IOException e)
		{
			System.out.println("File not found :" + file);
			e.printStackTrace();
		}

	}

	public String getStringValue(String section,String value)
	{
		return scenario.get(section,value);		
	}
	
	public int getIntValue(String section,String value)
	{
		return scenario.get(section,value,int.class);		
	}
	
	public double getDoubleValue(String section,String value)
	{
		return scenario.get(section,value,double.class);		
	}
	
	public Ini scenarioFile()
	{
		return scenario;
	}
	
	public double getScenarioVersion()
	{
		return scenario.get("Config","ConfigVersion",double.class);
	}

	public String getScenarioType()
	{
		return scenario.get("Config","ScenarioType");
	}
}