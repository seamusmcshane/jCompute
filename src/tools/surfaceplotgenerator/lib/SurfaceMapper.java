package tools.surfaceplotgenerator.lib;

import org.jzy3d.plot3d.builder.Mapper;

public class SurfaceMapper extends Mapper
{
	private double[][] values;
	
	public SurfaceMapper(double[][] values)
	{
		this.values = values;
	}
	
	@Override
	public double f(double x, double y)
	{
		int ix = (int) Math.round(x);
		int iy = (int) Math.round(y);
		
		return values[ix][iy];
	}
}