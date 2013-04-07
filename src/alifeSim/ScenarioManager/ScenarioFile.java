package alifeSim.ScenarioManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

public class ScenarioFile
{
	Ini scenario;

	public ScenarioFile(String file)
	{
		try
		{
			scenario = new Wini(new FileInputStream(file));
		}
		catch (IOException e)
		{
			System.out.println("File not found :" + file);
			e.printStackTrace();
		}

	}

	public String getStringValue(String section,String value)
	{				
		return scenario.get(section,value);		
	}
	
	public int getIntValue(String section,String value)
	{				
		return scenario.get(section,value,int.class);		
	}
	
	public double getDoubleValue(String section,String value)
	{				
		return scenario.get(section,value,double.class);		
	}	
}