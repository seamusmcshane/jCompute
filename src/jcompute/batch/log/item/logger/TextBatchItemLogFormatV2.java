package jcompute.batch.log.item.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jcompute.batch.BatchItem;
import jcompute.batch.BatchResultSettings;
import jcompute.batch.log.item.processor.logformat.ItemLogTextv2Format;

public class TextBatchItemLogFormatV2 implements BatchItemLogInf
{
	private PrintWriter itemLog;
	
	public TextBatchItemLogFormatV2()
	{
	}
	
	@Override
	public void init(String[] parameterName, String[] groupName, int BW_BUFFER_SIZE, String batchName, BatchResultSettings settings, String batchStatsExportDir)
	throws IOException
	{
		itemLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir + File.separator + "ItemLog.v2log", true), BW_BUFFER_SIZE));
		
		StringBuilder header = new StringBuilder();
		
		// Name
		header.append("Name=");
		header.append(batchName);
		header.append(ItemLogTextv2Format.OPTION_DELIMITER);
		
		// Type
		header.append("LogType=BatchItems");
		header.append(ItemLogTextv2Format.OPTION_DELIMITER);
		
		header.append("Samples=");
		header.append(settings.ItemSamples);
		header.append(ItemLogTextv2Format.OPTION_DELIMITER);
		
		int numCordinates = groupName.length;
		
		// AxisLabels
		header.append("AxisLabels=");
		header.append("Num=" + numCordinates);
		// ; done in loop - loop then exits skipping an ending ;
		for(int c = 1; c < (numCordinates + 1); c++)
		{
			header.append(ItemLogTextv2Format.SUBOPTION_DELIMITER);
			header.append("id=" + c);
			header.append(ItemLogTextv2Format.SUBOPTION_DELIMITER);
			header.append("AxisName=" + groupName[c - 1] + parameterName[c - 1]);
		}
		// No ending ,
		itemLog.println(header.toString());
	}
	
	@Override
	public void logItem(BatchItem item, String custom)
	{
		StringBuilder itemLine = new StringBuilder();
		
		// Item Id
		itemLine.append("IID=");
		itemLine.append(item.getItemId());
		itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
		
		// Sample Id
		itemLine.append("SID=");
		itemLine.append(item.getSampleId());
		itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
		
		// Surface Coords and Values
		ArrayList<Integer> coords = item.getCoordinates();
		ArrayList<Float> coordsValues = item.getCoordinatesValues();
		
		itemLine.append("Coordinates=");
		itemLine.append("Num=" + coords.size());
		// ; done in loop - loop then exits skipping an ending ;
		for(int c = 0; c < coords.size(); c++)
		{
			itemLine.append(ItemLogTextv2Format.SUBOPTION_DELIMITER);
			itemLine.append("Pos=");
			itemLine.append(coords.get(c));
			itemLine.append(ItemLogTextv2Format.SUBOPTION_DELIMITER);
			
			itemLine.append("Value=");
			itemLine.append(coordsValues.get(c));
		}
		itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
		
		// Cache
		itemLine.append("CacheIndex=");
		itemLine.append(item.getCacheIndex());
		itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
		
		// Runtime
		itemLine.append("RunTime=");
		itemLine.append(item.getComputeTime());
		itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
		
		// Endevent
		itemLine.append("EndEvent=");
		itemLine.append(item.getEndEvent());
		itemLine.append(ItemLogTextv2Format.OPTION_DELIMITER);
		
		// StepCount
		itemLine.append("StepCount=");
		itemLine.append(item.getStepCount());
		
		// No ending ,
		itemLog.println(itemLine);
	}
	
	@Override
	public void close()
	{
		// Close Batch Log
		itemLog.flush();
		itemLog.close();
	}
}
