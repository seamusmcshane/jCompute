package jcompute.scenario;

import java.util.List;

import jcompute.simulation.SimulationScenarioManagerInf;
import jcompute.stats.StatGroupSetting;

public interface ScenarioInf
{
	public boolean loadConfig(ConfigurationInterpreter interpreter);
	
	public String getScenarioType();
	
	public double getScenarioVersion();
	
	public SimulationScenarioManagerInf getSimulationScenarioManager();
	
	public List<StatGroupSetting> getStatGroupSettingsList();
	
	public String getScenarioText();
	
	public boolean endEventIsSet(String eventName);
	
	public int getEndEventTriggerValue(String eventName);
	
	public static final String INVALID = "Invalid";
	
}