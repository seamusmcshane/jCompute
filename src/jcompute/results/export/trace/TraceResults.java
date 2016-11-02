package jcompute.results.export.trace;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.ResultManager;
import jcompute.results.export.ExportFormat;
import jcompute.results.export.file.ExportFileWriter;
import jcompute.results.export.format.ARFFExporter;
import jcompute.results.export.format.CSVExporter;
import jcompute.results.export.format.XMLExporter;
import jcompute.results.trace.Trace;
import jcompute.results.trace.group.TraceGroup;
import jcompute.results.trace.samples.TraceSample;

public class TraceResults implements TraceResultInf
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(TraceResults.class);
	
	// File names
	private final String[] traceFileNames;
	
	// Data
	private final byte[][] traceTextData;
	
	public TraceResults(ResultManager rm, ExportFormat format, String traceFileNameSuffix)
	{
		log.info("Creating TraceResults from Result Manager - " + rm.getName());

		/*
		 * ***************************************************************************************************
		 * Trace Files
		 *****************************************************************************************************/
		
		Set<String> groupList = rm.getTraceGroupNames();
		int numFiles = groupList.size();
		
		// FileName
		traceFileNames = new String[numFiles];
		
		// FileData
		traceTextData = new byte[numFiles][];
		
		int file = 0;
		for(String group : groupList)
		{
			// Set the File Name
			String fileName;
			if(!traceFileNameSuffix.equals(""))
			{
				fileName = group + " " + traceFileNameSuffix;
			}
			else
			{
				fileName = group;
			}
			
			log.info("Adding " + fileName);
			
			traceFileNames[file] = fileName;
			
			// File Data as Bytes
			traceTextData[file] = createStatExportString(rm.getTraceGroup(fileName), format).getBytes();
			
			file++;
		}
	}
	
	public TraceResults(ByteBuffer source, String traceFileNameSuffix)
	{
		int numFiles = source.getInt();
		log.debug("Num Files " + numFiles);
		
		traceFileNames = new String[numFiles];
		
		// FileData
		traceTextData = new byte[numFiles][];
		
		for(int t = 0; t < numFiles; t++)
		{
			int tNum = source.getInt();
			
			if(tNum != t)
			{
				log.error("File Numbers not correct");
			}
			
			log.debug("Trace File Number : " + tNum);
			
			int len = source.getInt();
			byte[] stringBytes = new byte[len];
			
			log.debug("File Name Len " + len);
			
			source.get(stringBytes, 0, len);
			
			// FileName
			traceFileNames[t] = new String(stringBytes);
			
			/*
			 * Remote filenames are sent with out a suffix Append one if required
			 */
			if(!traceFileNameSuffix.equals(""))
			{
				traceFileNames[t] += " " + traceFileNameSuffix;
			}
			
			log.debug("File Name " + traceFileNames[t]);
			
			// FileData
			len = source.getInt();
			stringBytes = new byte[len];
			log.debug("Data Len " + len);
			
			source.get(stringBytes, 0, len);
			traceTextData[t] = stringBytes;
		}
	}
	
	/*
	 * Data formatter
	 */
	private String createStatExportString(TraceGroup traceGroup, ExportFormat format)
	{
		StringBuilder data = new StringBuilder();
		
		if(traceGroup != null)
		{
			String name = traceGroup.getName();
			
			List<String> traceList = traceGroup.getTraceList();
			
			if((format == ExportFormat.CSV) || (format == ExportFormat.ZCSV))
			{
				// Write File Header
				CSVExporter.AddFileExportHeaderCSV(data, traceList);
			}
			else if(format == ExportFormat.ARFF)
			{
				ARFFExporter.AddFileExportHeaderARFF(data, name, traceList);
			}
			else
			{
				XMLExporter.AddFileExportHeaderXML(data, name, traceList);
			}
			
			// Get the history length of the stats (which are all the same
			// length in steps)
			int historyLength = traceGroup.getTrace(traceList.get(0)).getHistoryLength();
			
			int traceCount = traceList.size();
			
			TraceSample[][] traceHistorys = new TraceSample[traceCount][];
			
			// Convert each Linked list to arrays - so we can look up individual
			// indexes quicker later.
			for(int traceIndex = 0; traceIndex < traceCount; traceIndex++)
			{
				Trace trace = traceGroup.getTrace(traceList.get(traceIndex));
				
				traceHistorys[traceIndex] = trace.getHistoryAsArray();
				
				// traceHistorys[statIndex] = traceGroup.getTrace(traceList.get(statIndex)).getHistory().toArray(new StatSample[historyLength]);
			}
			
			int history = 0;
			
			// Loop for the length of the stat history (sim run length)
			while(history < historyLength)
			{
				// Write Data Row
				if((format == ExportFormat.CSV) || (format == ExportFormat.ARFF) || (format == ExportFormat.ZCSV))
				{
					CSVExporter.AppendCSVRow(data, traceHistorys, history, traceList);
				}
				else
				{
					XMLExporter.AppendXMLRow(data, traceHistorys, history, traceList);
				}
				
				history++;
			}
			
			// File Footer
			if((format == ExportFormat.XML) || (format == ExportFormat.ZXML))
			{
				// data.append("</" + xmlString(name) + ">\n");
				
				XMLExporter.AppendCloseSection(data, name);
			}
			
		}
		
		return data.toString();
	}
	
	@Override
	public String[] getTraceFileNames()
	{
		return traceFileNames;
	}
	
	@Override
	public byte[][] getTraceData()
	{
		return traceTextData;
	}
	
	@Override
	public byte[] toBytes()
	{
		/*
		 * ***************************************************************************************************
		 * Size Calc
		 *****************************************************************************************************/
		
		int size = 0;
		
		int numTraceFiles = traceFileNames.length;
		
		// Total Trace files
		size += 4;
		
		// Calculate Trace Size
		for(int t = 0; t < numTraceFiles; t++)
		{
			// File Num Field
			size += 4;
			// File Name Len Field
			size += 4;
			// File Name Len in bytes
			size += traceFileNames[t].getBytes().length;
			// Data Len Field
			size += 4;
			// Data Len in bytes
			size += traceTextData[t].length;
		}
		
		/*
		 * ***************************************************************************************************
		 * Buffer
		 *****************************************************************************************************/
		
		// Unicode strings are 16bit=2bytes char
		ByteBuffer tbuffer = ByteBuffer.allocate(size);
		
		/*
		 * ***************************************************************************************************
		 * Put Traces
		 *****************************************************************************************************/
		
		// Total Trace Files
		tbuffer.putInt(numTraceFiles);
		
		for(int t = 0; t < numTraceFiles; t++)
		{
			writeBinFileToByteBuffer(tbuffer, t, traceFileNames[t], traceTextData[t]);
		}
		
		return tbuffer.array();
	}
	
	private void writeBinFileToByteBuffer(ByteBuffer tbuffer, int fileNum, String fileName, byte[] binData)
	{
		// Number of file
		tbuffer.putInt(fileNum);
		
		// File Name Len (bytes)
		tbuffer.putInt(fileName.getBytes().length);
		
		// File Name String to bytes
		tbuffer.put(fileName.getBytes());
		
		// File size bytes
		tbuffer.putInt(binData.length);
		
		// File size
		tbuffer.put(binData);
	}
	
	@Override
	public void export(String itemExportPath, ExportFormat format)
	{
		int numFiles = traceFileNames.length;
		
		for(int f = 0; f < numFiles; f++)
		{
			ExportFileWriter.WriteBinFile(itemExportPath, traceFileNames[f], traceTextData[f], format);
		}
	}
}
