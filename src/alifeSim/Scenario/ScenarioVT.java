package alifeSim.Scenario;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import alifeSim.Stats.StatGroupSetting;


/**
 * Base Scenario File reader.
 */

public class ScenarioVT
{
	private XMLConfiguration scenario;
	private List<StatGroupSetting> statSettingsList;
	
	public ScenarioVT()
	{
		statSettingsList = new ArrayList<StatGroupSetting>();
	}
	
	public void loadConfig(String text)
	{
		InputStream stream;
		
		try
		{			
			stream = new ByteArrayInputStream(text.getBytes());
			scenario = new XMLConfiguration();
			scenario.setSchemaValidation(true);
			scenario.load(stream);	
		}
		catch (ConfigurationException e)
		{
			System.out.println("Error : " + e.toString() + " - " + e.getStackTrace()[0].getMethodName());
		}
	}
	
	public void loadConfig(File file)
	{
		try
		{
			scenario = new XMLConfiguration();
			scenario.setSchemaValidation(true);
			scenario.load(file);
		}
		catch (ConfigurationException e)
		{
			System.out.println("Error : " + e.toString() + " - " + e.getStackTrace()[0].getMethodName());
		}
		
	}
	
	public int getSubListSize(String section,String value)
	{
		return scenario.configurationsAt(section+"."+value).size();
	}
	
	public boolean hasStringValue(String section,String value)
	{
		try
		{
			scenario.getString(section  + "." + value);
			return true;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}

	}
	
	public boolean hasIntValue(String section,String value)
	{
		try
		{
			scenario.getInt(section + "." +  value);	
			return true;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}

	}
	
	public boolean hasFloatValue(String section,String value)
	{
		try
		{
			scenario.getFloat(section + "." +  value);	
			return true;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}

	}
	
	public boolean hasDoubleValue(String section,String value)
	{
		try
		{
			scenario.getDouble( section + "." +  value);
			
			return true;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}

	}
	
	public String getStringValue(String section,String value)
	{
		return scenario.getString(section  + "." + value);		
	}
	
	public boolean getBooleanValue(String section,String value)
	{
		return scenario.getBoolean(section  + "." + value);		
	}
	
	public int getIntValue(String section,String value)
	{
		return scenario.getInt(section + "." +  value);		
	}
	
	public float getFloatValue(String section,String value)
	{
		return scenario.getFloat(section + "." +  value);		
	}
	
	public double getDoubleValue(String section,String value)
	{
		return scenario.getDouble( section + "." +  value);		
	}
	
	public XMLConfiguration scenarioFile()
	{
		return scenario;
	}
	
	public double getScenarioVersion()
	{
		return Double.parseDouble(scenario.getString("Header.Version","0.00"));
	}

	public String getScenarioType()
	{
		return scenario.getString("Header.Type","Scenario Type Not Set!!!");
	}

	/**
	 * Only Sub Class add StatSettings
	 * @param statSetting
	 */
	protected void addStatSettings(StatGroupSetting statSetting)
	{
		statSettingsList.add(statSetting);
	}
	
	/** Only called by sub class */
	protected void readStatSettings()
	{
		int statisticsGroups = getSubListSize("Statistics","Stat");

		String section;
		for(int i=0;i<statisticsGroups;i++)
		{
			section = "Statistics.Stat("+i+")";
			addStatSettings(new StatGroupSetting(getStringValue(section, "Name"),getBooleanValue(section, "Enabled"),getBooleanValue(section, "TotalStat"),getBooleanValue(section, "Graph"),getIntValue(section, "GraphSampleRate")));
		}
		
		System.out.println("Statistics " + statisticsGroups );		
	}
	
	public List<StatGroupSetting> getStatGroupSettingsList()
	{
		return statSettingsList;
	}
}