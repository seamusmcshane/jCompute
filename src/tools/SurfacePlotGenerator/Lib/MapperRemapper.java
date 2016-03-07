package tools.SurfacePlotGenerator.Lib;

import org.jzy3d.plot3d.builder.Mapper;

import jCompute.Batch.LogFileProcessor.LogFormatProcessor.LogFormatValuesContainer;

public class MapperRemapper extends Mapper
{
	private LogFormatValuesContainer values;
	private int target;
	
	public MapperRemapper(LogFormatValuesContainer values, int target)
	{
		this.values = values;
		this.target = target;
	}
	
	@Override
	public double f(double x, double y)
	{
		double value = Double.NaN;
		
		int ix = (int) Math.round(x);
		int iy = (int) Math.round(y);
		
		switch(target)
		{
			case 0:
			{
				value = values.getAvgs(ix, iy);
				
				break;
			}
			case 1:
			{
				
				value = values.getStandardDeviations(ix, iy);
				
				break;
			}
			case 2:
			{
				
				value = values.getMax(ix, iy);
				
				break;
			}
		}
		
		return value;
	}
}