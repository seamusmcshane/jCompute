package alifeSim.Scenario;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;


/**
 * Base Scenario File reader.
 */

public class ScenarioVT implements ScenarioInf
{
	XMLConfiguration scenario;
	
	public ScenarioVT()
	{

	}
	
	@Override
	public void loadConfig(String text)
	{		
		InputStream stream;
		
		System.out.println(text);

		try
		{			
			stream = new ByteArrayInputStream(text.getBytes());
			scenario = new XMLConfiguration();
			scenario.setSchemaValidation(true);
			scenario.load(stream);	
		}
		catch (ConfigurationException e)
		{
			System.out.println("Error : " + e.toString());
		}
	}
	
	@Override
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
			System.out.println("Error : " + e.toString());
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
}