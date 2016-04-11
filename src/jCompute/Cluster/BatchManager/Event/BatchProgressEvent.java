package jCompute.cluster.batchmanager.event;

import jCompute.batch.Batch;

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
