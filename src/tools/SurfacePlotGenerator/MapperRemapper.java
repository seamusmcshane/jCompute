package tools.SurfacePlotGenerator;

import java.awt.Color;
import java.awt.image.BufferedImage;

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
	
	private int getColor(int x,int y)
	{
		double value = 0;
		double max = values.getZMax();
		
		switch(target)
		{
			case 0:
			{
				value = values.getAvgs((int) x, (int) y);
				
				max = values.getZMax();
				
				break;
			}
			case 1:
			{
				value = values.getStandardDeviations((int) x, (int) y);
				
				max = values.getZMax();

				break;
			}
		}
		
		double hue  = (1.0/max)*value;
		
		return 	new Color(Color.HSBtoRGB((float)hue, 1f, 1f)).getRGB();	
	}

	public void populateImage(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		
		for(int x = 0;x<width;x++)
		{
			for(int y = 0;y<height;y++)
			{
				image.setRGB(x, y, getColor(x,y));
			}
		}

	}
}