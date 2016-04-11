package jCompute.Cluster.BatchManager.Event;

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
