package jcompute.configuration.batch;

import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Statistics")
@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
public class StatsConfig
{
	private boolean store;
	private boolean traceResultsEnabled;
	private boolean BDFCResultEnabled;
	// Not Implemented customResultFormat;
	// private boolean batchHeaderInCustomResult; //
	// private boolean itemInfoInCustomResult; //
	private boolean singleArchive;
	// Not Implemented bufferSize; //
	private int compressionLevel;
	private String statsExportDir;
	private String groupDir;
	private String subGroupDir;
	
	public StatsConfig()
	{
		
	}
	
	public StatsConfig(boolean store, boolean BDFCResultEnabled, boolean singleArchive, int compressionLevel,
	String statsExportDir, String groupDir, String subGroupDir)
	{
		this.store = store;
		this.BDFCResultEnabled = BDFCResultEnabled;
		this.singleArchive = singleArchive;
		this.compressionLevel = compressionLevel;
		this.statsExportDir = statsExportDir;
		this.groupDir = groupDir;
		this.subGroupDir = subGroupDir;
	}
	
	@XmlElement(name = "Store")
	public void setStoreEnabled(boolean store)
	{
		this.store = store;
	}
	
	public boolean getStoreEnabled()
	{
		return store;
	}
	
	@XmlElement(name = "TraceResults")
	public void setTraceResultsEnabled(boolean traceResultsEnabled)
	{
		this.traceResultsEnabled = traceResultsEnabled;
	}
	
	public boolean getTraceResultsEnabled()
	{
		return traceResultsEnabled;
	}
	
	@XmlElement(name = "BDFCResult")
	public void setBDFCResultEnabled(boolean BDFCResultEnabled)
	{
		this.BDFCResultEnabled = BDFCResultEnabled;
	}
	
	public boolean getBDFCResultEnabled()
	{
		return BDFCResultEnabled;
	}
	
	@XmlElement(name = "SingleArchive")
	public void setSingleArchiveEnabled(boolean singleArchive)
	{
		this.singleArchive = singleArchive;
	}
	
	public boolean isSingleArchiveEnabled()
	{
		return singleArchive;
	}
	
	@XmlElement(name = "CompressionLevel")
	public void setCompressionLevel(int compressionLevel)
	{
		this.compressionLevel = compressionLevel;
	}
	
	public int getCompressionLevel()
	{
		return compressionLevel;
	}
	
	@XmlElement(name = "BatchStatsExportDir")
	public String getStatsExportDir()
	{
		return statsExportDir;
	}
	
	public void setStatsExportDir(String statsExportDir)
	{
		this.statsExportDir = statsExportDir;
	}
	
	@XmlElement(name = "BatchGroupDir")
	public void setGroupDir(String groupDir)
	{
		this.groupDir = groupDir;
	}
	
	public String getGroupDir()
	{
		return groupDir;
	}
	
	@XmlElement(name = "BatchSubGroupDirName")
	public void setSubGroupDir(String subGroupDir)
	{
		this.subGroupDir = subGroupDir;
	}
	
	public String getSubGroupDir()
	{
		return subGroupDir;
	}
	
}
