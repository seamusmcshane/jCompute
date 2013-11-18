package alifeSim.Scenario;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;


/**
 * Base Scenario File reader.
 */

public class ScenarioVT implements ScenarioInf
{
	HierarchicalINIConfiguration scenario;

	public ScenarioVT(File file)
	{
			try
			{
				scenario = new HierarchicalINIConfiguration();
				scenario.load(file);
			}
			catch (ConfigurationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public ScenarioVT(String text)
	{
		InputStream stream;

		try
		{
			stream = new ByteArrayInputStream(text.getBytes("UTF-8"));
			scenario = new HierarchicalINIConfiguration();
			scenario.load(stream);
		}
		catch (ConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
	public boolean hasSection(String section)
	{
		for(String name : scenario.getSections())
		{
			if(section.equals(name))
			{
				return true;
			}
		}
		return false;
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
	
	public HierarchicalINIConfiguration scenarioFile()
	{
		return scenario;
	}
	
	public double getScenarioVersion()
	{
		return Double.parseDouble(scenario.getString("Config.ConfigVersion","0.00"));
	}

	public String getScenarioType()
	{
		return scenario.getString("Config.ScenarioType","Scenario Type Not Set!!!");
	}
	
}