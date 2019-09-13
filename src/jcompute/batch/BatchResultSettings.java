package jcompute.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.export.ExportFormat;

public class BatchResultSettings
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(BatchResultSettings.class);
	
	// Master Switch
	public final boolean ResultsEnabled;
	
	// The base results path under which we place the rest
	public final String BaseExportDir;
	
	// The complete export dir for stats
	public final String BatchStatsExportDir;
	
	public final boolean ResultsPartofGroup;
	public final String GroupDirName;
	
	public final boolean GroupsHaveSubGroups;
	public final String SubgroupDirName;
	
	// Track Results
	public final boolean TraceEnabled;
	
	// Results format
	public final ExportFormat TraceExportFormat = ExportFormat.CSV;
	
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
	
	public BatchResultSettings(boolean resultsEnabled, String baseExportDir, String groupDirName, String subgroupDirName, String BatchStatsExportDir,
	boolean traceEnabled, boolean bdfcEnabled, boolean traceStoreSingleArchive, int traceArchiveCompressionLevel, int bufferSize, boolean infoLogEnabled,
	boolean itemLogEnabled)
	{
		this.ResultsEnabled = resultsEnabled;
		
		this.BaseExportDir = baseExportDir;
		
		this.GroupDirName = groupDirName;
		ResultsPartofGroup = (this.GroupDirName != null);
		
		this.SubgroupDirName = subgroupDirName;
		GroupsHaveSubGroups = (this.SubgroupDirName != null);
		
		this.BatchStatsExportDir = BatchStatsExportDir;
		
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
	}
}
