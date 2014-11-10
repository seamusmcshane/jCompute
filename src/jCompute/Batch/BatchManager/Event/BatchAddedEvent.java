package jCompute.Batch.BatchManager.Event;

import jCompute.Batch.Batch;

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
