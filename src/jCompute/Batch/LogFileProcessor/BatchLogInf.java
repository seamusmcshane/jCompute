package jCompute.Batch.LogFileProcessor;

import jCompute.Batch.LogFileProcessor.LogFormatProcessor.LogFormatValuesContainer;

public interface BatchLogInf
{
	/*
	 * *****************************************************************************************************
	 * LogFile Info
	 *****************************************************************************************************/
	public String getLogFileName();
	
	/*
	 * *****************************************************************************************************
	 * Axis Ranges
	 *****************************************************************************************************/
	public double getXValMin();
	
	public double getXValMax();
	
	public double getXValRange();
	
	public double getYValMin();
	
	public double getYValMax();
	
	public double getYValRange();
	
	public double getZValMin();
	
	public double getZValMax();
	
	public double getZValRange();
	
	public double getZValRangeMin();
	
	public double getZValRangeMax();
	
	/*
	 * *****************************************************************************************************
	 * Axis Names
	 *****************************************************************************************************/
	public String[] getAxisNames();
	
	public String getXAxisName();
	
	public String getYAxisName();
	
	public String getZAxisName();
	
	/*
	 * *****************************************************************************************************
	 * Axis Limits
	 *****************************************************************************************************/
	public int getXMin();
	
	public int getYMin();
	
	public int getXMax();
	
	public int getYMax();
	
	public double getZmin();
	
	public double getZmax();
	
	/*
	 * *****************************************************************************************************
	 * Axis Granularity
	 *****************************************************************************************************/
	public int getXSteps();
	
	public int getYSteps();
	
	public int getNumSamples();
	
	/*
	 * *****************************************************************************************************
	 * Processed Data
	 *****************************************************************************************************/
	public double[] getAvgDataFlat();
	
	public double[][] getAvgData2d();
	
	public LogFormatValuesContainer getLogFormatValuesContainer();
	
	/*
	 * *****************************************************************************************************
	 * Processed Data Metrics
	 *****************************************************************************************************/
	
	public enum Source
	{
		AVG, STD_DEV, MAX
	}
	
	public double getDataMetricMinVal(Source metricSource);
	
	public double getDataMetricMaxVal(Source metricSource);
	
	public enum ComputedMetric
	{
		MAX_RATE
	}
	
	public double getComputedMetric(ComputedMetric metric);
	
	/*
	 * *****************************************************************************************************
	 * Internal Log Processing Time
	 *****************************************************************************************************/
	public long getTimeTaken();
	
}
