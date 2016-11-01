package jcompute.batch;

public class BatchResultSettings
{
	public final boolean ResultsEnabled;
	public final boolean TraceStoreSingleArchive;
	public final int TraceArchiveCompressionLevel;
	public final int BufferSize;
	
	/**
	 * Simple Batch Log
	 */
	public final boolean InfoLogEnabled;
	public final boolean ItemLogEnabled;
	public final boolean CustomItemLogEnabled;
	
	/**
	 * Number of repeats of each item.
	 */
	public final int ItemSamples;
	public final boolean MultiSampleItems;
	
	public BatchResultSettings(boolean resultsEnabled, boolean traceStoreSingleArchive, int traceArchiveCompressionLevel, int bufferSize, boolean infoLogEnabled,
	boolean itemLogEnabled, boolean customItemLogEnabled, int itemSamples)
	{
		this.ResultsEnabled = resultsEnabled;
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
		this.CustomItemLogEnabled = customItemLogEnabled;
		
		this.ItemSamples = itemSamples;
		this.MultiSampleItems = (itemSamples > 1) ? true : false;
	}
}
