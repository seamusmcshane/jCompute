package jcompute.batch.log.item.custom.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.BatchItem;
import jcompute.batch.BatchSettings;
import jcompute.util.JCText;

public class CustomItemLogger
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(CustomItemLogger.class);
	
	public static final char DELIMITER = ',';
	
	public static final char LINEFEED = '\n';
	
	private PrintWriter itemLog;
	
	public CustomItemLogger()
	{
		
	}
	
	public void init(CustomCSVItemLogFormatInf logformat, int BW_BUFFER_SIZE, String batchName, BatchSettings settings, String batchStatsExportDir)
	throws IOException
	{
		try
		{
			String logFileName = logformat.getLogFileName();
			
			if(logFileName == null)
			{
				logFileName = "LogFieldNameIsNull";
			}
			
			// Create the log writer
			itemLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + logFileName, true), BW_BUFFER_SIZE));
			
			String header = getLogHeader(logformat);
			
			itemLog.println(header.toString());
		}
		catch(Throwable e)
		{
			log.error("CustomItemLogger Init had a problem. This is what we know caught thowable : " + e.getClass().getName() + " cause : " + e.getCause()
			+ " message : " + e.getMessage());
			
			// Output the stack trace to the log so our message is before the trace. (preserve ordering)
			log.error(JCText.stackTraceToString(e.getStackTrace(), false));
			
			return;
		}
	}
	
	private String getLogHeader(CustomCSVItemLogFormatInf logformat)
	{
		StringBuilder header = new StringBuilder();
		
		int numberOfFields = logformat.numberOfFields();
		
		for(int f = 0; f < numberOfFields - 1; f++)
		{
			header.append(logformat.getFieldHeading(f));
			
			header.append(DELIMITER);
			
		}
		
		// Append last field but not delimiter
		header.append(logformat.getFieldHeading(numberOfFields));
		
		header.append(LINEFEED);
		
		return header.toString();
	}
	
	private String getLogRow(CustomCSVItemLogFormatInf logformat)
	{
		StringBuilder rowItem = new StringBuilder();
		
		int numberOfFields = logformat.numberOfFields();
		
		for(int f = 0; f < numberOfFields - 1; f++)
		{
			rowItem.append(logformat.getFieldValue(f));
			
			rowItem.append(DELIMITER);
		}
		
		// Append last field but not delimiter
		rowItem.append(logformat.getFieldValue(numberOfFields));
		
		rowItem.append(LINEFEED);
		
		return rowItem.toString();
	}
	
	public void logItem(BatchItem item, CustomCSVItemLogFormatInf logformat)
	{
		try
		{
			String rowItem = getLogRow(logformat);
			
			// No ending ,
			itemLog.println(rowItem);
		}
		catch(Throwable e)
		{
			log.error("CustomItemLogger logItem had a problem. This is what we know caught thowable : " + e.getClass().getName() + " cause : " + e.getCause()
			+ " message : " + e.getMessage());
			
			// Output the stack trace to the log so our message is before the trace. (preserve ordering)
			log.error(JCText.stackTraceToString(e.getStackTrace(), false));
			
			return;
		}
	}
	
	public void close()
	{
		try
		{
			// Close Batch Log
			itemLog.flush();
			itemLog.close();
		}
		catch(Throwable e)
		{
			log.error("CustomItemLogger close had a problem. This is what we know caught thowable : " + e.getClass().getName() + " cause : " + e.getCause()
			+ " message : " + e.getMessage());
			
			// Output the stack trace to the log so our message is before the trace. (preserve ordering)
			log.error(JCText.stackTraceToString(e.getStackTrace(), false));
			
			return;
		}
	}
}
