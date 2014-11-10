package jCompute.Batch.BatchManager.Event;

import jCompute.Batch.Batch;

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
