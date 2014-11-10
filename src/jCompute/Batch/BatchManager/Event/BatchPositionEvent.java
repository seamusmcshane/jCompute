package jCompute.Batch.BatchManager.Event;

import jCompute.Batch.Batch;

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
