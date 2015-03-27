package tools.TimeSeriesAnalysis;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class RecurrencePlot extends JPanel
{
	private static final long serialVersionUID = 1165414116871596076L;
	
	// Axis Indexes
	private final int X_POS = 0;
	private final int Y_POS = 1;
	private final int Z_POS = 2;
	
	// Data
	private boolean dataloaded = false;
	private double data[][];
	
	// Time Length
	private int tMax;
	
	// Final BitMap
	private boolean coloured = false;
	private float[] redBitmap;
	private float[] greenBitmap;
	private float[] blueBitmap;
	private int[] blackWhite;
	private int bitmapSize;
	
	// Requested ImageSize
	private int maxOutputSize;
	
	// Requested Image
	private BufferedImage image;
	
	public RecurrencePlot(int maxOutputSize)
	{
		this.maxOutputSize = maxOutputSize;
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
	
	/**
	 * Calculates the correct ratio base on threshold and squares it if
	 * requested
	 * @param threshold
	 * @param dataIn
	 * @param square
	 * @return
	 */
	private double calcRadiusFromThreshold(double threshold, double[][] dataIn, boolean square)
	{
		if(threshold > 1.0)
		{
			threshold = 1.0;
		}
		
		if(threshold < 0)
		{
			threshold = 0.001;
		}
		
		double maxDimesion = Double.MIN_VALUE;
		
		for(int i = 0; i < tMax; i++)
		{
			maxDimesion = Math.max(dataIn[X_POS][i], maxDimesion);
			maxDimesion = Math.max(dataIn[Y_POS][i], maxDimesion);
			maxDimesion = Math.max(dataIn[Z_POS][i], maxDimesion);
		}
		
		double radius = maxDimesion * threshold;
		
		if(square)
		{
			radius = radius * radius;
		}
		
		return radius;
	}
	
	/**
	 * Toggle Drawing of coloured plot
	 * @param coloured
	 */
	public void enableColor(boolean coloured)
	{
		this.coloured = coloured;
	}
	
	/**
	 * Initiates creation of the recurrent plot
	 * @param radiusThreshold
	 * @param size
	 */
	public void createRecurrence(double radiusThreshold, int size)
	{
		double radius = calcRadiusFromThreshold(radiusThreshold, data, true);
		
		bitmapSize = size;
		
		double scale = tMax / (double) bitmapSize;
		
		greenBitmap = new float[bitmapSize * bitmapSize];
		blueBitmap = new float[bitmapSize * bitmapSize];
		redBitmap = new float[bitmapSize * bitmapSize];
		
		blackWhite = new int[bitmapSize * bitmapSize];
		
		for(int y = 0; y < bitmapSize; y++)
		{
			int by = (int) ((double) y * scale);
			
			for(int x = 0; x < bitmapSize; x++)
			{
				int bx = (int) ((double) x * scale);
				
				redBitmap[x + (y * bitmapSize)] = 0;
				greenBitmap[x + (y * bitmapSize)] = 0;
				blueBitmap[x + (y * bitmapSize)] = 0;
				
				double dis = Double.MAX_VALUE;
				
				dis = distanceSquared(data[X_POS][by], data[Y_POS][by], data[Z_POS][by], data[X_POS][bx],
						data[Y_POS][bx], data[Z_POS][bx]);
				
				if(dis < (radius * 2))
				{
					redBitmap[x + (y * bitmapSize)] = (int) dis;
				}
				
				if(dis < radius)
				{
					greenBitmap[x + (y * bitmapSize)] = (int) dis;
					blackWhite[x + (y * bitmapSize)]++;
				}
				
				if(dis < (radius / 2))
				{
					blueBitmap[x + (y * bitmapSize)] = (int) dis;
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
				float scale = (float) maxOutputSize / (float) bitmapSize;
				image = scaleAndFlip(getPlotImage(bitmapSize), BufferedImage.TYPE_INT_RGB, scale);
				
				System.out.println("Scale Image " + scale);
				
			}
			
			g.drawImage(image, 0, 0, null);
			
			System.out.println("Drew Image");
		}
		
	}
	
	/**
	 * Returns the recurrence plot image
	 * @param bitmapSize
	 * @return
	 */
	private BufferedImage getPlotImage(int bitmapSize)
	{
		BufferedImage image = new BufferedImage(bitmapSize, bitmapSize, BufferedImage.TYPE_INT_RGB);
		
		float rrMin = Integer.MAX_VALUE;
		float rrMax = Integer.MIN_VALUE;
		float grMin = Integer.MAX_VALUE;
		float grMax = Integer.MIN_VALUE;
		float brMin = Integer.MAX_VALUE;
		float brMax = Integer.MIN_VALUE;
		
		int len = bitmapSize * bitmapSize;
		for(int i = 0; i < len; i++)
		{
			rrMin = Math.min(rrMin, redBitmap[i]);
			grMin = Math.min(grMin, greenBitmap[i]);
			brMin = Math.min(brMin, blueBitmap[i]);
			
			rrMax = Math.max(rrMax, redBitmap[i]);
			grMax = Math.max(grMax, greenBitmap[i]);
			brMax = Math.max(brMax, blueBitmap[i]);
		}
		
		float rcscale = 1f / (float) rrMax;
		float gcscale = 1f / (float) grMax;
		float bcscale = 1f / (float) brMax;
		
		System.out.println("Color Scale " + rcscale);
		System.out.println("Color Scale " + gcscale);
		System.out.println("Color Scale " + bcscale);
		
		for(int y = 0; y < bitmapSize; y++)
		{
			for(int x = 0; x < bitmapSize; x++)
			{
				if(coloured)
				{
					// BLACK
					image.setRGB(y, x, 0);
					
					if(redBitmap[x + (y * bitmapSize)] > 0)
					{
						float val = redBitmap[x + (y * bitmapSize)];
						
						val = (val * rcscale);
						
						// System.out.println("RVal " + val);
						
						image.setRGB(y, x, new Color(val, 0, 0).getRGB());
					}
					if(greenBitmap[x + (y * bitmapSize)] > 0)
					{
						float val = greenBitmap[x + (y * bitmapSize)];
						
						val = (val * gcscale);
						
						// System.out.println("RVal " + val);
						
						image.setRGB(y, x, new Color(0, val, 0).getRGB());
						
					}
					if(blueBitmap[x + (y * bitmapSize)] > 0)
					{
						float val = blueBitmap[x + (y * bitmapSize)];
						
						val = (val * bcscale);
						
						// System.out.println("RVal " + val);
						
						image.setRGB(y, x, new Color(0, 0, val).getRGB());
					}
					
				}
				else
				{
					if(blackWhite[x + (y * bitmapSize)] > 0)
					{
						image.setRGB(y, x, Color.BLACK.getRGB());
					}
					else
					{
						// WHITE
						image.setRGB(y, x, 0xFFFFFF);
					}
				}
			}
		}
		
		System.out.println("Created Image");
		
		return image;
	}
	
	/**
	 * Returns an Image scaled and flipped vertically
	 * @param inImage
	 * @param imageType
	 * @param scale
	 * @return
	 */
	private static BufferedImage scaleAndFlip(BufferedImage inImage, int imageType, float scale)
	{
		int width = (int) ((float) inImage.getWidth() * scale);
		int height = (int) ((float) inImage.getHeight() * scale);
		
		BufferedImage scaledImage = new BufferedImage(width, height, imageType);
		
		AffineTransform scaleAF = new AffineTransform();
		scaleAF.scale(scale, scale);
		
		// Choose the correct filter for scaling
		if(scale > 1)
		{
			// Increase Size
			AffineTransformOp operation = new AffineTransformOp(scaleAF, AffineTransformOp.TYPE_BICUBIC);
			operation.filter(inImage, scaledImage);
		}
		else
		{
			// Reduce Size
			AffineTransformOp operation = new AffineTransformOp(scaleAF, AffineTransformOp.TYPE_BILINEAR);
			operation.filter(inImage, scaledImage);
		}
		
		// Vertical Flip
		AffineTransform flipAF = AffineTransform.getScaleInstance(1, -1);
		flipAF.translate(0, -scaledImage.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(flipAF, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		scaledImage = op.filter(scaledImage, null);
		
		return scaledImage;
	}
	
	// 3D Euclidean Distance
	public double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2)
	{
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;
		
		return (dx * dx) + (dy * dy) + (dz * dz);
	}
}
