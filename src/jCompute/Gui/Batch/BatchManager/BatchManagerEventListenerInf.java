package jCompute.Gui.Batch.BatchManager;

import jCompute.Gui.Batch.Batch.Batch;

public interface BatchManagerEventListenerInf
{
	public void batchAdded(final Batch batch);
	// public void batchRemoved(final int batchId);
	public void batchFinished(final Batch batch);
	public void batchProgress(final Batch batch);
	
}
