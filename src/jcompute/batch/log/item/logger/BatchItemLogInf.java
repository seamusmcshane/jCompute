package jcompute.batch.log.item.logger;

import java.io.IOException;

import jcompute.batch.batchitem.BatchItem;

public interface BatchItemLogInf
{
	public void init(String[] parameterName, String[] groupName, int BW_BUFFER_SIZE, String batchName, int itemSamples, String batchStatsExportDir)
	throws IOException;
	
	public void logItem(BatchItem item, String custom);
	
	public void close();
}
