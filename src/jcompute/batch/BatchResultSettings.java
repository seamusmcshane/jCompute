package jcompute.batch;

public class BatchResultSettings
{
	public final boolean ResultsEnabled;
	
	public final boolean TraceEnabled;
	public final boolean BDFCEnabled;
	public final boolean CustomEnabled;
	
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
	
	public BatchResultSettings(boolean resultsEnabled, boolean traceEnabled, boolean bdfcEnabled, boolean customEnabled, boolean traceStoreSingleArchive,
	int traceArchiveCompressionLevel, int bufferSize, boolean infoLogEnabled, boolean itemLogEnabled, int itemSamples)
	{
		this.ResultsEnabled = resultsEnabled;
		
		this.TraceEnabled = traceEnabled;
		this.BDFCEnabled = bdfcEnabled;
		this.CustomEnabled = customEnabled;
		
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
