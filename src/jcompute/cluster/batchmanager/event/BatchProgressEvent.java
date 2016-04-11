package jcompute.cluster.batchmanager.event;

import jcompute.batch.Batch;

public class BatchProgressEvent
{
	private Batch batch;
	
	public BatchProgressEvent(Batch batch)
	{
		this.batch = batch;
	}

	public Batch getBatch()
	{
		return batch;
	}
}
