package jcompute.batch.log.item.logger;

import java.io.IOException;

import jcompute.batch.BatchItem;
import jcompute.batch.BatchSettings;

public interface BatchItemLogInf
{
	public void init(String[] parameterName, String[] groupName, int BW_BUFFER_SIZE, String batchName, BatchSettings settings, String batchStatsExportDir)
	throws IOException;
	
	public void logItem(BatchItem item, String custom);
	
	public void close();
}
