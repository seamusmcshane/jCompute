package jCompute.cluster.batchmanager.event;

import jCompute.batch.Batch;

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
