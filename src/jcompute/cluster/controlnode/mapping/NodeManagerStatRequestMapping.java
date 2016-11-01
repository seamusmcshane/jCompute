package jcompute.cluster.controlnode.mapping;

import java.util.concurrent.Semaphore;

import jcompute.results.export.ExportFormat;
import jcompute.results.export.Result;

public class NodeManagerStatRequestMapping
{
	private Semaphore requestBlock;
	private ExportFormat format; 
	private String fileNameSuffix; 
	public Result exporter;

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
	
	public void setStatExporter(Result exporter)
	{
		this.exporter = exporter;
	}
	
	public Result getExporter()
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
