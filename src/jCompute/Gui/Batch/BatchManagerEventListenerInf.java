package jCompute.Gui.Batch;

public interface BatchManagerEventListenerInf
{
	public void batchAdded(final Batch batch);
	// public void batchRemoved(final int batchId);
	public void batchFinished(final Batch batch);
	public void batchProgress(final Batch batch);
	
}
