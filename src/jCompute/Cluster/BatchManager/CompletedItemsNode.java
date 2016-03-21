package jCompute.Cluster.BatchManager;

import jCompute.Batch.BatchItem;
import jCompute.Stats.StatExporter;

public class CompletedItemsNode
{
	private BatchItem item;
	private StatExporter exporter;
	
	public CompletedItemsNode(BatchItem item, StatExporter exporter)
	{
		this.item = item;
		this.exporter = exporter;
	}
	
	public StatExporter getExporter()
	{
		return exporter;
	}
	
	public BatchItem getItem()
	{
		return item;
	}
	
}
