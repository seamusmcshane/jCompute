package tools.TimeSeriesAnalysis;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class RecurrencePlot extends JPanel
{
	private final int X_POS = 0;
	private final int Y_POS = 1;
	private final int Z_POS = 2;
	
	// Data
	private boolean dataloaded = false;
	private double data[][];
	
	// Time Length
	private int tMax;
	
	// Final BitMap
	private int[] bitmap;
	
	// Colors
	private final int WHITE = 0xFFFFFF;
	private final int BLACK = 0x000000;
	
	private BufferedImage image;
	
	public RecurrencePlot()
	{
		
	}
	
	public void setData(double[][] data)
	{
		this.data = data;
		
		this.image = null;
		
		if(data != null)
		{
			if(data.length >= 3)
			{
				dataloaded = true;
				
				tMax = data[0].length;
				
				System.out.println("Data set / tMax " + tMax);
				
			}
		}
		
	}
	
	public void createRecurrence(double radiusThreshold)
	{
		if(radiusThreshold > 1.0)
		{
			radiusThreshold = 1.0;
		}
		
		bitmap = new int[tMax * tMax];
		
		double max = Double.MIN_VALUE;
		
		for(int i = 0; i < tMax; i++)
		{
			max = Math.max(data[X_POS][i], max);;
			max = Math.max(data[Y_POS][i], max);;
			max = Math.max(data[Z_POS][i], max);;
		}
		
		double radius = max * radiusThreshold;
		radius = radius * radius;
		
		for(int y = 0; y < tMax; y++)
		{
			for(int x = 0; x < tMax; x++)
			{
				bitmap[x + (y * tMax)] = WHITE;
				
				double dis = Double.MAX_VALUE;
				
				dis = distanceSquared(data[X_POS][y], data[Y_POS][y], data[Z_POS][y], data[X_POS][x], data[Y_POS][x],
						data[Z_POS][x]);
				
				if(dis <= radius)
				{
					// Set Value
					bitmap[x + (y * tMax)] = BLACK;
				}
			}
		}
		
		System.out.println("Created Recurrence");
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if(dataloaded)
		{
			if(image == null)
			{
				float scale = (float) this.getWidth() / (float) tMax;
				image = scale(getImage(tMax), BufferedImage.TYPE_INT_RGB, this.getWidth(), this.getWidth(), scale,
						scale);
				
				System.out.println("Scale Image " + scale);
				
			}
			
			g.drawImage(image, 0, 0, null);
			
			System.out.println("Drew Image");
		}
		
	}
	
	public BufferedImage getImage(int size)
	{
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		
		for(int y = 0; y < tMax; y++)
		{
			for(int x = 0; x < tMax; x++)
			{
				image.setRGB(y, x, bitmap[x + (y * tMax)]);
			}
		}
		
		System.out.println("Created Image");
		
		return image;
	}
	
	public static BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth,
			double fHeight)
	{
		BufferedImage dbi = null;
		if(sbi != null)
		{
			dbi = new BufferedImage(dWidth, dHeight, imageType);
			Graphics2D g = dbi.createGraphics();
			AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
			g.drawRenderedImage(sbi, at);
		}
		return dbi;
	}
	
	public double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2)
	{
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;
		
		return (dx * dx) + (dy * dy) + (dz * dz);
	}
}
