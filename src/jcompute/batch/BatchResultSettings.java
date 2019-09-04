package jcompute.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.log.item.custom.logger.ItemLogExportFormat;

public class BatchResultSettings
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(BatchResultSettings.class);
	
	public final boolean ResultsEnabled;
	
	public final boolean TraceEnabled;
	public final boolean BDFCEnabled;
	public final boolean CustomResultsEnabled;
	public final ItemLogExportFormat CustomItemResultsFormat;
	
	public final boolean TraceStoreSingleArchive;
	public final int TraceArchiveCompressionLevel;
	public final int BufferSize;
	
	/**
	 * Simple Batch Log
	 */
	public final boolean InfoLogEnabled;
	public final boolean ItemLogEnabled;
	
	/**
	 * Number of repeats of each item.
	 */
	public final int ItemSamples;
	public final boolean MultiSampleItems;
	
	public BatchResultSettings(boolean resultsEnabled, boolean traceEnabled, boolean bdfcEnabled, boolean customResultsEnabled, String customItemResultsFormat,
	boolean traceStoreSingleArchive, int traceArchiveCompressionLevel, int bufferSize, boolean infoLogEnabled, boolean itemLogEnabled, int itemSamples)
	{
		this.ResultsEnabled = resultsEnabled;
		
		this.TraceEnabled = traceEnabled;
		this.BDFCEnabled = bdfcEnabled;
		
		CustomItemResultsFormat = ItemLogExportFormat.fromString(customItemResultsFormat);
		
		if(CustomItemResultsFormat != null)
		{
			this.CustomResultsEnabled = customResultsEnabled;
		}
		else
		{
			// Sanity check - this is a bug somewhere else.
			this.CustomResultsEnabled = false;
			
			log.error("Disabling Custom results, as no ItemLogExportFormat set.");
		}
		
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
		
		this.ItemSamples = itemSamples;
		this.MultiSampleItems = (itemSamples > 1) ? true : false;
	}
}
