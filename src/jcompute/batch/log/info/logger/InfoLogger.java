package jcompute.batch.log.info.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.util.text.TimeString;
import jcompute.util.text.TimeString.TimeStringFormat;

public class InfoLogger
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(InfoLogger.class);
	
	private final String logFileName = "InfoLog.log";
	private final PrintWriter infoLog;
	
	private boolean generalInfo;
	private boolean itemInfo;
	private boolean processedInfo;
	private boolean itemComputeInfo;
	private boolean scenarioParameters;
	
	public InfoLogger(String path) throws IOException
	{
		try
		{
			String filePath = path + File.separator + logFileName;
			infoLog = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
			
			generalInfo = false;
			itemInfo = false;
			processedInfo = false;
			itemComputeInfo = false;
			scenarioParameters = false;
			
			log.info("Info log started : " + filePath);
		}
		catch(IOException e)
		{
			log.error("Error creating info log");
			
			// This is a fatal error for the Info Logger - caught here to log.
			throw e;
		}
	}
	
	// General Info
	public void writeGeneralInfo(int batchId, String description, String scenarioType, String baseScenarioFileName)
	{
		infoLog.println("BatchId=" + batchId);
		infoLog.println("Description=" + description);
		infoLog.println("ScenarioType=" + scenarioType);
		infoLog.println("BaseFile=" + baseScenarioFileName);
		
		generalInfo = true;
	}
	
	// Item Info
	public void writeItemInfo(int totalItems, int samplesPerItem, /*int maxSteps,*/ String generationTime)
	{
		// Calculate unique items
		int uniqueItems = totalItems / samplesPerItem;
		infoLog.println("Items=" + totalItems);
		infoLog.println("ItemSamples=" + samplesPerItem);
		infoLog.println("UniqueItems=" + uniqueItems);
		/*infoLog.println("MaxSteps=" + maxSteps);*/
		infoLog.println("GenerationTime=" + generationTime);
		
		itemInfo = true;
	}
	
	// Batch processing info
	public void writeProcessedInfo(String addedDateTime, String startDateTime, String endDateTime, long startTimeMillis)
	{
		infoLog.println("AddedDateTime=" + addedDateTime);
		infoLog.println("StartDateTime=" + startDateTime);
		infoLog.println("FinishedDateTime=" + endDateTime);
		infoLog.println("TotalTime=" + TimeString.timeInMillisAsFormattedString(System.currentTimeMillis() - startTimeMillis, TimeStringFormat.DHMS));
		
		processedInfo = true;
	}
	
	public void writeItemComputeInfo(long itemCompleted, long cpuTotalTimes, long ioTotalTimes)
	{
		infoLog.println("CpuTotalTime=" + cpuTotalTimes);
		infoLog.println("CpuAvgTime=" + (cpuTotalTimes / itemCompleted));
		infoLog.println("IOTotalTime=" + ioTotalTimes);
		infoLog.println("IOAvgTime=" + (ioTotalTimes / itemCompleted));
		infoLog.println("ItemTotalTime=" + (cpuTotalTimes + ioTotalTimes));
		infoLog.println("ItemAvgTime=" + ((cpuTotalTimes + ioTotalTimes) / itemCompleted));
		
		itemComputeInfo = true;
	}
	
	public void writeParameters(ArrayList<String> parameters)
	{
		if(parameters == null)
		{
			log.error("Error writing parameters for info log - parameters cannot be null");
			
			return;
		}
		
		for(int i = 0; i < parameters.size(); i += 2)
		{
			// Skip "" ""
			if(!(parameters.get(i).equals("")))
			{
				infoLog.println(parameters.get(i) + "=" + parameters.get(i + 1));
			}
		}
		
		scenarioParameters = true;
	}
	
	public void close()
	{
		if((generalInfo != false) && (itemInfo != false) && (processedInfo != false) && (itemComputeInfo != false) && (scenarioParameters != false))
		{
			infoLog.flush();
			infoLog.close();
			
			log.info("Info Log Finished");
		}
		else
		{
			// Not a fatal error but not good either.
			log.error("Info log incomplete when it was requested to be closed");
			
			infoLog.flush();
			infoLog.close();
		}
	}
}
