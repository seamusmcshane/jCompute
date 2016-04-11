package jcompute.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class JComputeInfo
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(JComputeInfo.class);
	
	private static JComputeInfo instance;
	
	private final long LAUNCH_TIME;
	
	private final String BUILD_DATE;
	private final String BUILD_REVISON;
	private final String LAUNCH_DATE_TIME;
	
	private JComputeInfo() throws IOException
	{
		try
		{
			// Open the file stream
			FileInputStream input = new FileInputStream("buildinfo.prop");
			
			// Create and load properties
			Properties prop = new Properties();
			prop.load(input);
			
			// The time at which the program/jvm was launched.
			LAUNCH_TIME = ManagementFactory.getRuntimeMXBean().getStartTime();
			
			// The Build date of the jCompute Framework
			BUILD_DATE = prop.getProperty("datetime");
			
			// The revision control id
			BUILD_REVISON = prop.getProperty("revision");
			
			LAUNCH_DATE_TIME = TimeString.longTimeToDateString(LAUNCH_TIME);
			
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
	
	public String getBuildRevison()
	{
		return BUILD_REVISON;
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
