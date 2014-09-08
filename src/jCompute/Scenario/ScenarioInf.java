package jCompute.Scenario;

import jCompute.Simulation.SimulationScenarioManagerInf;
import jCompute.Stats.StatGroupSetting;

import java.util.List;

import net.xeoh.plugins.base.Plugin;

import org.apache.commons.configuration.HierarchicalConfiguration;

public interface ScenarioInf extends Plugin
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