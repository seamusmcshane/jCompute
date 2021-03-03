package jcompute.batch.batchresults;

import jcompute.configuration.batch.BatchJobConfig;
import jcompute.results.export.ExportFormat;

public class BatchResultSettings
{
	// Log4j2 Logger
	// private static Logger log = LogManager.getLogger(BatchResultSettings.class);
	
	// Master Switch
	public final boolean ResultsEnabled;
	
	// The base results path under which we place the rest
	public final String BaseExportDir;
	
	// The complete export path for stats
	public final String FullBatchStatsExportPath;
	
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
	// public final int BufferSize;
	
	/**
	 * Simple Batch Log
	 */
	public final boolean InfoLogEnabled;
	public final boolean ItemLogEnabled;
	
	/**
	 * Number of repeats of each item.
	 */
	// public final boolean MultiSampleItems;
	
	public BatchResultSettings(BatchJobConfig bjc, String fullBatchStatsExportPath)
	{
		this.ResultsEnabled = bjc.getStats().getStoreEnabled();
		
		this.BaseExportDir = bjc.getStats().getStatsExportDir();
		
		this.GroupDirName = bjc.getStats().getGroupDir();
		ResultsPartofGroup = (this.GroupDirName != null);
		
		this.SubgroupDirName = bjc.getStats().getSubGroupDir();
		GroupsHaveSubGroups = (this.SubgroupDirName != null);
		
		this.FullBatchStatsExportPath = fullBatchStatsExportPath;
		
		this.TraceEnabled = bjc.getStats().getTraceResultsEnabled();
		this.BDFCEnabled = bjc.getStats().getBDFCResultEnabled();
		
		this.TraceStoreSingleArchive = bjc.getStats().isSingleArchiveEnabled();
		
		int traceArchiveCompressionLevel = bjc.getStats().getCompressionLevel();
		
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
		
		// TODO remove
		// this.BufferSize = bufferSize;
		
		this.InfoLogEnabled = bjc.getLog().getInfoLog();
		this.ItemLogEnabled = bjc.getLog().getItemLog();
	}
}
