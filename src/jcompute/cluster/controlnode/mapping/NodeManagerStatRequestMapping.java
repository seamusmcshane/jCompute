package jcompute.cluster.controlnode.mapping;

import java.util.concurrent.Semaphore;

import jcompute.stats.StatExporter;
import jcompute.stats.StatExporter.ExportFormat;

public class NodeManagerStatRequestMapping
{
	private Semaphore requestBlock;
	private ExportFormat format; 
	private String fileNameSuffix; 
	public StatExporter exporter;

	public NodeManagerStatRequestMapping(ExportFormat format, String fileNameSuffix)
	{
		requestBlock = new Semaphore(0,false);
		
		this.format = format;
		
		this.fileNameSuffix = fileNameSuffix;
	}
	
	public void waitOnReply()
	{
		this.requestBlock.acquireUninterruptibly();
	}
	
	public void setStatExporter(StatExporter exporter)
	{
		this.exporter = exporter;
	}
	
	public StatExporter getExporter()
	{
		return exporter;
	}
	
	public void signalReply()
	{
		this.requestBlock.release();
	}

	public ExportFormat getFormat()
	{
		return format;
	}
	
	public String getFileNameSuffix()
	{
		return fileNameSuffix;
	}
	
}
