package alifeSim.Scenario;

import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Stats.StatGroupSetting;

public interface ScenarioInf
{
	public String getStringValue(String section,String value);
	
	public int getIntValue(String section,String value);
	
	public double getDoubleValue(String section,String value);
	
	public HierarchicalConfiguration scenarioFile();
	
	public double getScenarioVersion();

	public String getScenarioType();
	
	public void loadConfig(String text);
	
	public List<StatGroupSetting> getStatGroupSettingsList();
	
	public SimulationScenarioManagerInf getSimulationScenarioManager();
	
	public String getScenarioText();

}