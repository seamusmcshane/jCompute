package jCompute.Batch.LogFileProcessor.Mapper;

import java.awt.image.BufferedImage;

import org.jzy3d.plot3d.builder.Mapper;

import jCompute.Gui.View.Misc.Palette;

public class MapperRemapper extends Mapper
{
	private MapperValuesContainer values;
	private int target;
	
	private final int PALLET_SIZE = 256;
	private final int[] pallete = Palette.SpectrumPalete(false, PALLET_SIZE);
	
	public MapperRemapper(MapperValuesContainer values, int target)
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
		}
		
		return value;
	}
	
	private int getColor(int x, int y)
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
		
		double one = ((double) PALLET_SIZE - 1) / max;
		int index = (int) (one * value);
		
		return pallete[index];
	}
	
	public void populateImage(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				image.setRGB(x, y, getColor(x, y));
			}
		}
		
	}
}