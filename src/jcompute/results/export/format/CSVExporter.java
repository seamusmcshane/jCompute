package jcompute.results.export.format;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.trace.samples.TraceSample;

public class CSVExporter
{
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
		fileData.append(statList.get(statIndex) + ",");
		
		StringBuilder logString = new StringBuilder();
		
		logString.append("Categories : " + statList.get(statIndex));
		
		for(statIndex = 1; statIndex < statCount; statIndex++)
		{
			logString.append(", " + statList.get(statIndex));
			
			fileData.append(statList.get(statIndex));
			
			if(statIndex < (statCount - 1))
			{
				fileData.append(",");
			}
			else
			{
				fileData.append("\n");
			}
			
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
		
		// Append the sample from the first stat with a , appended
		data.append(traceHistorys[0][history].toString() + ",");
		
		// Do the same for every history, append , after each sample or a new
		// line after each history
		for(int statIndex = 1; statIndex < statCount; statIndex++)
		{
			data.append(traceHistorys[statIndex][history].toString());
			
			if(statIndex < (statCount - 1))
			{
				data.append(",");
			}
			else
			{
				data.append("\n");
			}
			
		}
	}
}
