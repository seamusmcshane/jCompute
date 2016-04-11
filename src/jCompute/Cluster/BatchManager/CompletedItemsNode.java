package jCompute.Cluster.BatchManager;

import jCompute.Stats.StatExporter;
import jCompute.batch.BatchItem;

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
