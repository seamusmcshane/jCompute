package alifeSim.Scenario;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import alifeSim.Simulation.SimulationManagerInf;
import alifeSim.Stats.StatGroupSetting;
import alifeSim.Stats.StatManager;

public interface ScenarioInf
{
	public String getStringValue(String section,String value);
	
	public int getIntValue(String section,String value);
	
	public double getDoubleValue(String section,String value);
	
	public HierarchicalConfiguration scenarioFile();
	
	public double getScenarioVersion();

	public String getScenarioType();

	public void loadConfig(File file);
	
	public void loadConfig(String text);
	
	public List<StatGroupSetting> getStatGroupSettingsList();
	
	public SimulationManagerInf getSimManager();

}