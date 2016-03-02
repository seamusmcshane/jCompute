package jCompute.Batch.LogFileProcessor;

import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

public interface BatchLogInf
{
	/*
	 * *****************************************************************************************************
	 * Axis Ranges
	 *****************************************************************************************************/
	public double getXValMin();
	
	public double getXValMax();
	
	public double getYValMin();
	
	public double getYValMax();
	
	public double getZValMin();
	
	public double getZValMax();
	
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
	 * Jzy3d Compatibility
	 *****************************************************************************************************/
	public ITickRenderer getXTickMapper();
	
	public ITickRenderer getYTickMapper();
	
	/*
	 * *****************************************************************************************************
	 * Processed Data
	 *****************************************************************************************************/
	public Mapper getAvg();
	
	public double[][] getAvgData();
	
	public Mapper getStdDev();
	
	public Mapper getMax();
	
	/*
	 * *****************************************************************************************************
	 * Metrics
	 *****************************************************************************************************/
	public double getMaxRate();
}
