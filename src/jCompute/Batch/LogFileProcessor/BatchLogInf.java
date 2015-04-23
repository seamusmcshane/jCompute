package jCompute.Batch.LogFileProcessor;

import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

public interface BatchLogInf
{
	public int getXMin();
	public int getYMin();
	public int getXMax();
	public int getYMax();
	
	public double getZmin();
	public double getZmax();
	
	public int getXSteps();
	public int getYSteps();
	
	public String getXAxisName();
	
	public String getYAxisName();
	
	public String getZAxisName();
	
	public String[] getAxisNames();
	
	public ITickRenderer getXTickMapper();
	
	public ITickRenderer getYTickMapper();
	
	public Mapper getStdDev();
	public Mapper getAvg();
	public double[][] getAvgData();

	public double getXValMin();
	public double getXValMax();
	public double getYValMin();
	public double getYValMax();
	
}
