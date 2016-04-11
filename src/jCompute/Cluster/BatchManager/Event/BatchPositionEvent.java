package jCompute.cluster.batchmanager.event;

import jCompute.batch.Batch;

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
