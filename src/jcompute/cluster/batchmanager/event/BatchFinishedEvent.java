package jcompute.cluster.batchmanager.event;

import jcompute.batch.Batch;

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
