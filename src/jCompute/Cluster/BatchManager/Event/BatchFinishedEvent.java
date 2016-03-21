package jCompute.Cluster.BatchManager.Event;

import jCompute.Batch.Batch;

public class BatchFinishedEvent
{
	private Batch batch;
	
	public BatchFinishedEvent(Batch batch)
	{
		this.batch = batch;
	}
	
	public Batch getBatch()
	{
		return batch;
	}
}
