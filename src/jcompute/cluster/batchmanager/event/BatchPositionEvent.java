package jcompute.cluster.batchmanager.event;

import jcompute.batch.Batch;

public class BatchPositionEvent
{
	private Batch batch;
	
	public BatchPositionEvent(Batch batch)
	{
		this.batch = batch;
	}
	
	public Batch getBatch()
	{
		return batch;
	}
}
