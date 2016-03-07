package jCompute.Batch.LogFileProcessor;

import jCompute.Batch.LogFileProcessor.LogFormatProcessor.LogFormatValuesContainer;

public interface BatchLogInf
{
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
	 * Metrics
	 *****************************************************************************************************/
	public double getMaxRate();
	
	/*
	 * *****************************************************************************************************
	 * Internal Log Processing Time
	 *****************************************************************************************************/
	public long getTimeTaken();
}
