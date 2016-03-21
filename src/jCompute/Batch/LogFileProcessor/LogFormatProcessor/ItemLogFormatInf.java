package jCompute.Batch.LogFileProcessor.LogFormatProcessor;

import java.util.ArrayList;

public interface ItemLogFormatInf
{
	/*
	 * *****************************************************************************************************
	 * Log Format
	 *****************************************************************************************************/
	public String getLogFormat();
	
	/*
	 * *****************************************************************************************************
	 * Axis Names
	 *****************************************************************************************************/
	public String getXAxisName();
	
	public String getYAxisName();
	
	public String getZAxisName();
	
	/*
	 * *****************************************************************************************************
	 * Log Info
	 *****************************************************************************************************/
	public String getLogFileName();
	
	public String getLogType();
	
	public int getSamples();
	
	/*
	 * *****************************************************************************************************
	 * Log Items
	 *****************************************************************************************************/
	public ArrayList<ItemLogItem> getLogItems();
}