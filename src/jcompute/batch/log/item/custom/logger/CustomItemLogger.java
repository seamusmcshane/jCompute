package jcompute.batch.log.item.custom.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.BatchItem;
import jcompute.batch.BatchResultSettings;
import jcompute.util.JCText;

public class CustomItemLogger
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(CustomItemLogger.class);
	
	public static final char DELIMITER = ',';
	
	public static final char LINEFEED = '\n';
	
	private PrintWriter itemLog;
	
	private final CustomCSVItemLogFormatInf logFormat;
	
	public CustomItemLogger(CustomCSVItemLogFormatInf logFormat)
	{
		this.logFormat = logFormat;
	}
	
	public void init(int BW_BUFFER_SIZE, String batchName, BatchResultSettings settings, String batchStatsExportDir) throws IOException
	{
		// Create the log writer
		itemLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + logFormat.getLogFileName(), true), BW_BUFFER_SIZE));
		
		String header = getLogHeader();
		
		itemLog.println(header.toString());
	}
	
	public CustomCSVItemLogFormatInf getLogformat()
	{
		return logFormat;
	}
	
	private String getLogHeader()
	{
		StringBuilder header = new StringBuilder();
		
		int numberOfFields = logFormat.numberOfFields();
		
		for(int f = 0; f < numberOfFields - 1; f++)
		{
			header.append(logFormat.getFieldHeading(f));
			
			header.append(DELIMITER);
		}
		
		// Append last field but not delimiter
		header.append(logFormat.getFieldHeading(numberOfFields - 1));
		
		header.append(LINEFEED);
		
		return header.toString();
	}
	
	private String getLogRow(CustomCSVItemLogFormatInf logRow)
	{
		StringBuilder rowItem = new StringBuilder();
		
		int numberOfFields = logRow.numberOfFields();
		
		for(int f = 0; f < numberOfFields - 1; f++)
		{
			rowItem.append(logRow.getFieldValue(f));
			
			rowItem.append(DELIMITER);
		}
		
		// Append last field but not delimiter
		rowItem.append(logRow.getFieldValue(numberOfFields - 1));
		
		rowItem.append(LINEFEED);
		
		return rowItem.toString();
	}
	
	public void logItem(BatchItem item, CustomCSVItemLogFormatInf logRow)
	{
		try
		{
			String rowItem = getLogRow(logRow);
			
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
