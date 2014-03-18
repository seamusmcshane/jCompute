package alifeSim.Batch;

public interface BatchManagerEventListenerInf
{
	public void batchAdded(int batchId,String baseFile,String scenarioType,int batchItems,int progress,int completedItems);
	public void batchRemoved(int batchId);
	public void batchProgress(int batchId,int progress,int completedItems);
	
}
