package jcompute.results.export;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.ResultManager;
import jcompute.results.export.bdfc.BDFCResult;
import jcompute.results.export.custom.CustomItemResult;
import jcompute.results.export.trace.TraceResultInf;
import jcompute.results.export.trace.TraceResults;
import jcompute.results.export.trace.TraceZipCompress;

public class Result
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(Result.class);
	
	// File Format for export
	private final ExportFormat format;
	
	/*
	 * ***************************************************************************************************
	 * Trace Files
	 *****************************************************************************************************/
	
	private final String traceFileNameSuffix;
	private TraceResultInf traceResults;
	
	/*
	 * ***************************************************************************************************
	 * Binary Files
	 *****************************************************************************************************/
	
	private BDFCResult bdfcResult;
	
	/*
	 * ***************************************************************************************************
	 * Custom Result
	 *****************************************************************************************************/
	
	private CustomItemResult ciResult;
	
	/**
	 * An object dedicated to exporting simulation stats Not-Thread safe.
	 * 
	 * @param format
	 */
	public Result(ExportFormat format, String fileNameSuffix)
	{
		this.format = format;
		this.traceFileNameSuffix = fileNameSuffix;
	}
	
	public void populateFromResultManager(ResultManager rm)
	{
		log.info("Populating from Result Manager - " + rm.getName());
		
		/*
		 * ***************************************************************************************************
		 * Trace Files
		 *****************************************************************************************************/
		
		traceResults = new TraceResults(rm, format, traceFileNameSuffix);
		
		// Compress the trace data if required
		if((format == ExportFormat.ZXML) || (format == ExportFormat.ZCSV))
		{
			traceResults = new TraceZipCompress(traceResults, format);
		}
		
		/*
		 * ***************************************************************************************************
		 * Binary Files
		 *****************************************************************************************************/
		
		bdfcResult = new BDFCResult(rm);
		
		/*
		 * ***************************************************************************************************
		 * Custom Result
		 *****************************************************************************************************/
		
		ciResult = new CustomItemResult(rm);
	}
	
	/*
	 * Bytes
	 */
	public byte[] toBytes()
	{
		ByteBuffer tbuffer;
		
		int size = 0;
		
		/*
		 * ***************************************************************************************************
		 * Trace Files + Calc
		 *****************************************************************************************************/
		
		byte[] traces = traceResults.toBytes();
		
		size += traces.length;
		
		/*
		 * ***************************************************************************************************
		 * Bin Collections + Files + Calc
		 *****************************************************************************************************/
		
		byte[] bdfc = bdfcResult.toBytes();
		
		size += bdfc.length;
		
		/*
		 * ***************************************************************************************************
		 * Custom Result
		 *****************************************************************************************************/
		
		byte[] cir = ciResult.toBytes();
		
		size += cir.length;
		
		/*
		 * ***************************************************************************************************
		 * Buffer
		 *****************************************************************************************************/
		
		// Unicode strings are 16bit=2bytes char
		tbuffer = ByteBuffer.allocate(size);
		
		/*
		 * ***************************************************************************************************
		 * Put Traces
		 *****************************************************************************************************/
		
		tbuffer.put(traces);
		
		/*
		 * ***************************************************************************************************
		 * Put Bin Collections + Files
		 *****************************************************************************************************/
		
		tbuffer.put(bdfc);
		
		/*
		 * ***************************************************************************************************
		 * Put Custom Result
		 *****************************************************************************************************/
		
		tbuffer.put(cir);
		
		return tbuffer.array();
	}
	
	public void populateFromByteBuffer(ByteBuffer source)
	{
		log.debug("StatExporter : populating from ByteBuffer");
		
		if((format == ExportFormat.ZXML) || (format == ExportFormat.ZCSV))
		{
			/*
			 * ***************************************************************************************************
			 * Get (Compressed) Trace Files
			 *****************************************************************************************************/
			
			traceResults = new TraceZipCompress(source);
		}
		else
		{
			/*
			 * ***************************************************************************************************
			 * Get Trace Files
			 *****************************************************************************************************/
			
			traceResults = new TraceResults(source, traceFileNameSuffix);
			
			/*
			 * ***************************************************************************************************
			 * Binary Files
			 *****************************************************************************************************/
			
			bdfcResult = new BDFCResult(source);
			
			/*
			 * ***************************************************************************************************
			 * Custom Result
			 *****************************************************************************************************/
			
			ciResult = new CustomItemResult(source);
		}
		
		int left = source.remaining();
		if(left > 0)
		{
			log.error("Stats not processed fully - bytes left : " + left);
		}
		
	}
	
	/**
	 * @param itemExportPath
	 */
	public void exportPerItemTraceResults(String itemExportPath)
	{
		traceResults.export(itemExportPath, format);
	}
	
	/**
	 * @param zipOut
	 * @param itemId
	 * @param sampleId
	 */
	public void exportPerItemTraceResultsToZipArchive(ZipOutputStream zipOut, int itemId, int sampleId)
	{
		// Create Zip Directories
		try
		{
			/*
			 * ***************************************************************************************************
			 * Write Trace Files
			 *****************************************************************************************************/
			
			String[] traceFileNames = traceResults.getTraceFileNames();
			byte[][] traceTextData = traceResults.getTraceData();
			
			int numFiles = traceFileNames.length;
			
			for(int f = 0; f < numFiles; f++)
			{
				// FileName
				zipOut.putNextEntry(new ZipEntry(itemId + "/" + sampleId + "/" + traceFileNames[f] + ".csv"));
				
				// Data
				zipOut.write(traceTextData[f]);
				
				// Entry end
				zipOut.closeEntry();
			}
		}
		catch(IOException e)
		{
			log.error("Could not create export files for " + itemId);
			
			e.printStackTrace();
		}
	}
	
	public void exportBinResults(String itemExportPath, String collatedStatsDir, int itemid)
	{
		bdfcResult.export(itemExportPath, collatedStatsDir, itemid);
	}
	
	public String[] getCustomLoggerNames()
	{
		return ciResult.getCirFileNames();
	}
	
	public byte[][] getCustomLoggerData()
	{
		return ciResult.getCirData();
	}
	
	public int getSize()
	{
		int traceFiles = (traceResults == null ? 0 : traceResults.getTraceFileNames().length);
		
		int binFiles = (bdfcResult == null ? 0 : bdfcResult.getBinFileNames().length);
		
		int cirFiles = (ciResult == null ? 0 : ciResult.getCirFileNames().length);
		
		return(traceFiles + binFiles + cirFiles);
	}
}
