package tools.SurfacePlotGenerator.Lib;

import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

import jCompute.Batch.LogFileProcessor.LogFormatProcessor.LogFormatValuesContainer;

public class SurfaceChartHelper
{
	public static MapperRemapper getAvg(LogFormatValuesContainer values)
	{
		MapperRemapper avgMap = new MapperRemapper(values, 0);
		
		return avgMap;
	}
	
	public static MapperRemapper getStdDev(LogFormatValuesContainer values)
	{
		MapperRemapper stdMap = new MapperRemapper(values, 1);
		
		return stdMap;
	}
	
	public static MapperRemapper getMax(LogFormatValuesContainer values)
	{
		MapperRemapper maxMap = new MapperRemapper(values, 2);
		
		return maxMap;
	}
	
	public static ITickRenderer getTickMapper(int coordMax, double valueMax)
	{
		return new TickValueMapper(coordMax, valueMax);
	}
}
