package jcompute.batch.log.item.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jcompute.batch.BatchItem;
import jcompute.batch.BatchResultSettings;

public class TextBatchItemLogFormatV1 implements BatchItemLogInf
{
	private PrintWriter itemLog;
	
	public TextBatchItemLogFormatV1()
	{
	}
	
	@Override
	public void init(String[] parameterName, String[] groupName, int BW_BUFFER_SIZE, String batchName, BatchResultSettings settings, String batchStatsExportDir)
	throws IOException
	{
		itemLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + "ItemLog.log", true), BW_BUFFER_SIZE));
		
		itemLog.println("[+Header]");
		itemLog.println("Name=" + batchName);
		itemLog.println("LogType=BatchItems");
		itemLog.println("Samples=" + settings.ItemSamples);
		itemLog.println("[+AxisLabels]");
		
		int numCordinates = groupName.length;
		
		for(int c = 1; c < (numCordinates + 1); c++)
		{
			itemLog.println("id=" + c);
			itemLog.println("AxisName=" + groupName[c - 1] + parameterName[c - 1]);
		}
		
		itemLog.println("[-AxisLabels]");
		itemLog.println("[-Header]");
		itemLog.println("[+Items]");
	}
	
	@Override
	public void logItem(BatchItem item, String custom)
	{
		itemLog.println("[+Item]");
		itemLog.println("IID=" + item.getItemId());
		itemLog.println("SID=" + item.getSampleId());
		ArrayList<Integer> coords = item.getCoordinates();
		ArrayList<Float> coordsValues = item.getCoordinatesValues();
		for(int c = 0; c < coords.size(); c++)
		{
			itemLog.println("[+Coordinate]");
			itemLog.println("Pos=" + coords.get(c));
			itemLog.println("Value=" + coordsValues.get(c));
			itemLog.println("[-Coordinate]");
		}
		itemLog.println("CacheIndex=" + item.getCacheIndex());
		itemLog.println("RunTime=" + item.getComputeTime());
		itemLog.println("EndEvent=" + item.getEndEvent());
		itemLog.println("StepCount=" + item.getStepCount());
		itemLog.println("[-Item]");
	}
	
	@Override
	public void close()
	{
		// Close the Items Section in v1 log
		itemLog.println("[-Items]");
		
		// Close Batch Log
		itemLog.flush();
		itemLog.close();
	}
}
