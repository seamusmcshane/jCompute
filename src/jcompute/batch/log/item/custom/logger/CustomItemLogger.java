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
import jcompute.results.export.format.CSVExporter;
import jcompute.util.text.JCText;

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
		
		// CSV has added \n so use print not println
		itemLog.print(header.toString());
	}
	
	public CustomCSVItemLogFormatInf getLogformat()
	{
		return logFormat;
	}
	
	private String getLogHeader()
	{
		StringBuilder header = new StringBuilder();
		
		// Get the headings in an array.
		int numberOfFields = logFormat.numberOfFields();
		String[] headings = new String[numberOfFields];
		for(int f=0;f<numberOfFields;f++)
		{
			headings[f] = logFormat.getFieldHeading(f);
		}
		
		// Use the CSVExporter
		CSVExporter.CreateCSVRow(header,headings,numberOfFields);
		
		return header.toString();
	}
	
	private String getLogRow(CustomCSVItemLogFormatInf logRow)
	{
		StringBuilder rowItem = new StringBuilder();
		
		// Get the values in an array.
		int numberOfFields = logFormat.numberOfFields();
		String[] values = new String[numberOfFields];
		for(int f=0;f<numberOfFields;f++)
		{
			// new String has no object initialiser
			values[f] = new String(String.valueOf(logRow.getFieldValue(f)));
		}
		
		// Use the CSVExporter
		CSVExporter.CreateCSVRow(rowItem,values,numberOfFields);
		
		return rowItem.toString();
	}
	
	public void logItem(BatchItem item, CustomCSVItemLogFormatInf logRow)
	{
		try
		{
			String rowItem = getLogRow(logRow);
			
			// CSV has added \n so use print not println
			itemLog.print(rowItem);
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
