package jCompute.Batch.BatchManager;

import jCompute.Batch.Batch;

public interface BatchManagerEventListenerInf
{
	public void batchAdded(final Batch batch);
	// public void batchRemoved(final int batchId);
	public void batchFinished(final Batch batch);
	public void batchProgress(final Batch batch);
	public void batchQueuePositionChanged(final Batch batch);
}
