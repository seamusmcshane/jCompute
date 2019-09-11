package jcompute.cluster.batchmanager;

import jcompute.batch.BatchItem;
import jcompute.results.export.Result;

public class CompletedItemsNode
{
	private BatchItem item;
	private Result exporter;
	
	public CompletedItemsNode(BatchItem item, Result exporter)
	{
		this.item = item;
		this.exporter = exporter;
	}
	
	public Result getExporter()
	{
		return exporter;
	}
	
	public BatchItem getItem()
	{
		return item;
	}
}
