package jCompute.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JComputeInfo
{
	private static Logger log = LoggerFactory.getLogger(JComputeInfo.class);
	
	private static JComputeInfo instance;
	
	private final long LAUNCH_TIME;
	
	private final String BUILD_DATE;
	private final String LAUNCH_DATE_TIME;
	
	private JComputeInfo() throws IOException
	{
		try
		{
			// Open the file stream
			FileInputStream input = new FileInputStream("BuildDateTime");
			
			// Create and load properties
			Properties prop = new Properties();
			prop.load(input);
			
			// The time at which the program/jvm was launched.
			LAUNCH_TIME = ManagementFactory.getRuntimeMXBean().getStartTime();
			
			// The Build date of the jCompute Framework
			BUILD_DATE = prop.getProperty("BuildDateTime");
			
			LAUNCH_DATE_TIME = Text.longTimeToDateString(LAUNCH_TIME);
			
			// Close file stream
			input.close();
		}
		catch(IOException e)
		{
			// Log error and rethrow
			log.error("Properties file not found");
			
			throw e;
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * Instance
	 *****************************************************************************************************/
	
	public synchronized static JComputeInfo getInstance() throws IOException
	{
		if(instance == null)
		{
			
			instance = new JComputeInfo();
		}
		
		return instance;
	}
	
	/*
	 * ***************************************************************************************************
	 * Static Information
	 *****************************************************************************************************/
	
	public String getBuildDate()
	{
		return BUILD_DATE;
	}
	
	public String getLaunched()
	{
		return LAUNCH_DATE_TIME;
	}
	
	/*
	 * ***************************************************************************************************
	 * Real-Time Statistics
	 *****************************************************************************************************/
	
	public long getRuntime()
	{
		return System.currentTimeMillis() - LAUNCH_TIME;
	}
}