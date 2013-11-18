package alifeSim.Scenario;

import org.apache.commons.configuration.HierarchicalINIConfiguration;

public interface ScenarioInf
{
	public String getStringValue(String section,String value);
	
	public int getIntValue(String section,String value);
	
	public double getDoubleValue(String section,String value);
	
	public HierarchicalINIConfiguration scenarioFile();
	
	public double getScenarioVersion();

	public String getScenarioType();

}