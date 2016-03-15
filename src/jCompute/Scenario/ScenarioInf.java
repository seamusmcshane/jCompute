package jCompute.Scenario;

import jCompute.Simulation.SimulationScenarioManagerInf;
import jCompute.Stats.StatGroupSetting;

import java.util.List;

public interface ScenarioInf
{
	public void loadConfig(ConfigurationInterpreter interpreter);
	
	public String getScenarioType();
	
	public double getScenarioVersion();
	
	public SimulationScenarioManagerInf getSimulationScenarioManager();
	
	public List<StatGroupSetting> getStatGroupSettingsList();
	
	public String getScenarioText();
	
	public boolean endEventIsSet(String eventName);
	
	public int getEndEventTriggerValue(String eventName);
	
	public static final String INVALID = "Invalid";
	
}