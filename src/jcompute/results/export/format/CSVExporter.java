package jcompute.results.export.format;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.trace.samples.TraceSample;

public class CSVExporter
{
	public static final char DELIMITER = ',';
	public static final char LINEFEED = '\n';
	
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(CSVExporter.class);
	
	/**
	 * @param fileData
	 * @param statList
	 */
	public static void AddFileExportHeaderCSV(StringBuilder fileData, List<String> statList)
	{
		// CSV Header Row
		int statCount = statList.size();
		int statIndex = 0;
		
		StringBuilder logString = new StringBuilder();
		
		logString.append("Categories : ");
		
		for(statIndex = 0; statIndex < statCount; statIndex++)
		{
			fileData.append(statList.get(statIndex));
			logString.append(statList.get(statIndex));
			
			// Separator
			char c = CalculateSeperatorChar(statIndex, statCount);
			
			fileData.append(c);
			logString.append(c);
		}
		
		log.info(logString.toString());
	}
	
	/**
	 * @param row
	 * @param textValues
	 * @param numColumns
	 */
	public static void CreateCSVRow(StringBuilder row, String[] textValues, int numColumns)
	{
		// CSV Header Row
		StringBuilder logString = new StringBuilder();
		
		logString.append("Fields : ");
		
		for(int field = 0; field < numColumns; field++)
		{
			// Data
			row.append(textValues[field]);
			
			// Logger
			logString.append(textValues[field]);
			
			// Separator
			char c = CalculateSeperatorChar(field, numColumns);
			
			// Data
			row.append(c);
			
			// Logger
			logString.append(c);
		}
		
		log.info(logString.toString());
	}
	
	/**
	 * @param data
	 * @param traceHistorys
	 * @param history
	 * @param traceList
	 */
	public static void AppendCSVRow(StringBuilder data, TraceSample[][] traceHistorys, int history, List<String> traceList)
	{
		int statCount = traceList.size();
		
		// Do the same for every history, append , after each sample or a new
		// line after each history
		for(int statIndex = 0; statIndex < statCount; statIndex++)
		{
			// Data
			data.append(traceHistorys[statIndex][history].toString());
			
			// Separator
			data.append(CalculateSeperatorChar(statIndex, statCount));
		}
	}
	
	public static char CalculateSeperatorChar(int currentIndex, int MaxIndex)
	{
		// Check this first - we may have only one column
		if(currentIndex == MaxIndex)
		{
			return LINEFEED;
		}
		else if(currentIndex < (MaxIndex - 1))
		{
			// Multi-Column
			return DELIMITER;
		}
		else
		{
			// Multi-Column-End
			return LINEFEED;
		}
	}
}
