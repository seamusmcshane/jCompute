package alifeSim.Scenario;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.Wini;

public interface ScenarioInf
{
	public String getStringValue(String section,String value);
	
	public int getIntValue(String section,String value);
	
	public double getDoubleValue(String section,String value);
	
	public Ini scenarioFile();
	
	public double getScenarioVersion();

	public String getScenarioType();

}