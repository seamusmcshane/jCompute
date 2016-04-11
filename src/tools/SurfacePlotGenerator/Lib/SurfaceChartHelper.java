package tools.SurfacePlotGenerator.Lib;

import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

import jCompute.batch.logfileprocessor.ItemLogProcessor;
import jCompute.batch.logfileprocessor.logformatprocessor.metrics.surface.SurfaceMetricInf.Type;

public class SurfaceChartHelper
{
	public static SurfaceMapper getAvg(ItemLogProcessor logProcessor)
	{
		return new SurfaceMapper(logProcessor.getDataMetric2dArray(Type.AVERAGE));
	}
	
	public static SurfaceMapper getStdDev(ItemLogProcessor logProcessor)
	{
		return new SurfaceMapper(logProcessor.getDataMetric2dArray(Type.STANDARD_DEVIATION));
	}
	
	public static SurfaceMapper getMax(ItemLogProcessor logProcessor)
	{
		return new SurfaceMapper(logProcessor.getDataMetric2dArray(Type.MAX));
	}
	
	public static ITickRenderer getTickMapper(int coordMax, double valueMax)
	{
		return new TickValueMapper(coordMax, valueMax);
	}
}
