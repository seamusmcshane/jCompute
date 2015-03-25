package tools.TimeSeriesAnalysis;

import jCompute.Gui.View.Graphics.A3DVector3f;

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
	
	// Intermedia Vector Data
	private A3DVector3f[] vectors;
	
	// Final BitMap
	private int[][] bitmap;
	
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
		
		generateVectors();
		
	}
	
	private void generateVectors()
	{
		if(!dataloaded)
		{
			return;
		}
		
		vectors = new A3DVector3f[tMax];
		
		// Convert array to vectors
		for(int t = 0; t < tMax; t++)
		{
			vectors[t] = new A3DVector3f(data[X_POS][t], data[Y_POS][t], data[Z_POS][t]);
		}
		
		System.out.println("Generated Vectors");
		
	}
	
	public void createRecurrence(float normRadius)
	{
		float radius = normRadius * normRadius;
		
		bitmap = new int[tMax][tMax];
		
		for(int x = 0; x < tMax; x++)
		{
			for(int y = 0; y < tMax; y++)
			{
				bitmap[x][y] = WHITE;
				
				float dis = Float.MAX_VALUE;
				
				A3DVector3f current = vectors[y];
				
				dis = current.distanceSquared(vectors[x]);
				
				if(dis <= radius)
				{
					// Set Value
					bitmap[x][y] = BLACK;
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
		
		for(int x = 0; x < tMax; x++)
		{
			for(int y = 0; y < tMax; y++)
			{
				image.setRGB(x, y, bitmap[x][y]);
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
}
