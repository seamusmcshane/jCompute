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
import jcompute.results.custom.CustomItemResultInf;
import jcompute.results.export.format.CSVExporter;
import jcompute.util.text.JCText;

public class CustomItemLogger
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(CustomItemLogger.class);
	
	private PrintWriter itemLog;
	
	private final CustomItemResultInf itemResult;
	
	private final ItemLogExportFormat format;
	
	public CustomItemLogger(CustomItemResultInf itemResult, ItemLogExportFormat format)
	{
		this.itemResult = itemResult;
		
		this.format = format;
	}
	
	/**
	 * Initialises the logger ready for use.
	 * This method exists to enable file creation to be put off until the batch has started processing.
	 * 
	 * @param BW_BUFFER_SIZE
	 * @param batchName
	 * @param settings
	 * @param batchStatsExportDir
	 * @throws IOException
	 */
	public void init(int BW_BUFFER_SIZE, String batchName, BatchResultSettings settings, String batchStatsExportDir) throws IOException
	{
		// Create the log writer
		itemLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + itemResult.getLogFileName(), true), BW_BUFFER_SIZE));
		
		String header = getLogHeader();
		
		// CSV has added \n so use print not println
		itemLog.print(header.toString());
	}
	
	public CustomItemResultInf getItemResult()
	{
		return itemResult;
	}
	
	private String getLogHeader()
	{
		StringBuilder header = new StringBuilder();
		
		// Get the headings in an array.
		int numberOfFields = itemResult.getTotalFields();
		String[] headings = new String[numberOfFields];
		for(int f = 0; f < numberOfFields; f++)
		{
			headings[f] = itemResult.getFieldHeading(f);
		}
		
		switch(format)
		{
			case CSV:
			{
				// Use the CSVExporter
				CSVExporter.CreateCSVRow(header, headings, numberOfFields);
			}
			break;
			case TextV1:
			{
				
			}
			break;
			case TextV2:
			{
				
			}
			break;
			case XML:
			{
				
			}
			break;
		}
		
		return header.toString();
	}
	
	private String getLogRow(CustomItemResultInf logRow)
	{
		StringBuilder rowItem = new StringBuilder();
		
		// Get the values in an array.
		int numberOfFields = itemResult.getTotalFields();
		String[] values = new String[numberOfFields];
		for(int f = 0; f < numberOfFields; f++)
		{
			// new String has no object initialiser
			values[f] = new String(String.valueOf(logRow.getFieldValue(f)));
		}
		
		switch(format)
		{
			case CSV:
			{
				// Use the CSVExporter
				CSVExporter.CreateCSVRow(rowItem, values, numberOfFields);
			}
			break;
			case TextV1:
			{
				
			}
			break;
			case TextV2:
			{
				
			}
			break;
			case XML:
			{
				
			}
			break;
		}
		
		return rowItem.toString();
	}
	
	public void logItem(BatchItem item, CustomItemResultInf logRow)
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
