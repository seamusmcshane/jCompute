package jcompute.results.export.format;

import java.util.List;

import jcompute.results.trace.samples.TraceSample;

public class ARFFExporter
{
	/**
	 * @param fileData
	 * @param group
	 * @param statList
	 */
	public static void AddFileExportHeaderARFF(StringBuilder fileData, String group, List<String> statList)
	{
		// The ARFF HEADER
		fileData.append("% 1. Title : " + group + " Database\n");
		fileData.append("%\n");
		fileData.append("% 2. Sources :\n");
		fileData.append("%		(a) jcompute\n");
		fileData.append("%\n");
		
		// Add Relation Field
		fileData.append("@RELATION " + group + "\n");
		
		int statCount = statList.size();
		
		// The Attribute type rows
		for(int statIndex = 0; statIndex < statCount; statIndex++)
		{
			// All Assumed Numeric (All stats currently numeric)
			fileData.append("@ATTRIBUTE '" + statList.get(statIndex) + "' NUMERIC\n");
		}
		
		// Begin Data Section
		fileData.append("@DATA\n");
	}
	
	/**
	 * @param data
	 * @param traceHistorys
	 * @param history
	 * @param traceList
	 */
	public static void AppendARFFRow(StringBuilder data, TraceSample[][] traceHistorys, int history, List<String> traceList)
	{
		// Delegate to CSV
		CSVExporter.AppendCSVRow(data, traceHistorys, history, traceList);
	}
	
}
