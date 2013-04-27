package alifeSim.Scenario;

import org.ini4j.Ini;

public interface ScenarioInf
{
	public String getStringValue(String section,String value);
	
	public int getIntValue(String section,String value);
	
	public double getDoubleValue(String section,String value);
	
	public Ini scenarioFile();
	
	public double getScenarioVersion();

	public String getScenarioType();

}