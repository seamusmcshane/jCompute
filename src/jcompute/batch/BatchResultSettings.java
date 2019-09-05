package jcompute.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.log.item.custom.logger.CustomItemResultsSettings;
import jcompute.batch.log.item.custom.logger.ItemLogExportFormat;

public class BatchResultSettings
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(BatchResultSettings.class);
	
	// Master Switch
	public final boolean ResultsEnabled;
	
	// Track Results
	public final boolean TraceEnabled;
	
	// Binary File Results
	public final boolean BDFCEnabled;
	
	// Zip archive settings
	public final boolean TraceStoreSingleArchive;
	public final int TraceArchiveCompressionLevel;
	
	// Write Buffer Size
	public final int BufferSize;
	
	/**
	 * Simple Batch Log
	 */
	public final boolean InfoLogEnabled;
	public final boolean ItemLogEnabled;
	
	/**
	 * Number of repeats of each item.
	 */
	// public final boolean MultiSampleItems;
	
	public BatchResultSettings(boolean resultsEnabled, boolean traceEnabled, boolean bdfcEnabled, boolean traceStoreSingleArchive,
	int traceArchiveCompressionLevel, int bufferSize, boolean infoLogEnabled, boolean itemLogEnabled)
	{
		this.ResultsEnabled = resultsEnabled;
		
		this.TraceEnabled = traceEnabled;
		this.BDFCEnabled = bdfcEnabled;
		
		this.TraceStoreSingleArchive = traceStoreSingleArchive;
		
		if(traceArchiveCompressionLevel > 9)
		{
			this.TraceArchiveCompressionLevel = 9;
		}
		else if(traceArchiveCompressionLevel < 0)
		{
			this.TraceArchiveCompressionLevel = 0;
		}
		else
		{
			this.TraceArchiveCompressionLevel = traceArchiveCompressionLevel;
		}
		
		this.BufferSize = bufferSize;
		
		this.InfoLogEnabled = infoLogEnabled;
		this.ItemLogEnabled = itemLogEnabled;
		
		// this.ItemSamples = itemSamples;
		// this.MultiSampleItems = (itemSamples > 1) ? true : false;
	}
}
