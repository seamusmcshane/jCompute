package jcompute.cluster.batchmanager.event;

import jcompute.batch.Batch;

public class BatchAddedEvent
{
	private Batch batch;
	
	public BatchAddedEvent(Batch batch)
	{
		this.batch = batch;
	}
	
	public Batch getBatch()
	{
		return batch;
	}
}
