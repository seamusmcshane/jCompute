package tools.SurfacePlotGenerator;

import org.jzy3d.plot3d.builder.Mapper;

public class MapperRemapper extends Mapper
{
	private MapperValuesContainer values;
	private int target;

	public MapperRemapper(MapperValuesContainer values, int target)
	{
		this.values = values;
		this.target = target;
	}

	@Override
	public double f(double x, double y)
	{
		double value = Double.NaN;

		switch(target)
		{
			case 0:
			{
				value = values.getAvgs((int) x, (int) y);

				break;
			}
			case 1:
			{

				value = values.getStandardDeviations((int) x, (int) y);

				break;
			}
		}

		return value;
	}
}