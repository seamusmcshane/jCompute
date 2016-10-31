package jcompute.cluster.batchmanager;

import jcompute.batch.BatchItem;
import jcompute.results.ResultExporter;

public class CompletedItemsNode
{
	private BatchItem item;
	private ResultExporter exporter;
	
	public CompletedItemsNode(BatchItem item, ResultExporter exporter)
	{
		this.item = item;
		this.exporter = exporter;
	}
	
	public ResultExporter getExporter()
	{
		return exporter;
	}
	
	public BatchItem getItem()
	{
		return item;
	}
	
}
