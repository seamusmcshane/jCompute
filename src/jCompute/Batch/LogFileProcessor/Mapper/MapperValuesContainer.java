package jCompute.Batch.LogFileProcessor.Mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapperValuesContainer
{
	private static Logger log = LoggerFactory.getLogger(MapperValuesContainer.class);

	private int ySteps;
	private int xSteps;
	private int samples;
	private double[][][] sampleValues;
	
	private double[][] avg;
	
	private double[][] stdDev;
	
	private int xPosMin = Integer.MAX_VALUE;
	private int xPosMax = Integer.MIN_VALUE;
	private int yPosMin = Integer.MAX_VALUE;
	private int yPosMax = Integer.MIN_VALUE;
	
	private double zMin = Double.MAX_VALUE;
	private double zMax = Double.MIN_VALUE;
	
	public MapperValuesContainer(int xSteps, int ySteps, int samples)
	{
		this.ySteps = ySteps;
		this.xSteps = xSteps;
		this.samples = samples;
		
		log.info("X Steps : " + xSteps);
		log.info("Y Steps : " + ySteps);
		log.info("Samples : " + samples);
		
		this.sampleValues = new double[xSteps][ySteps][samples];
		
		// Fill the array with invalid values
		for(int x = 0; x < xSteps; x++)
		{
			for(int y = 0; y < ySteps; y++)
			{
				for(int i = 0; i < samples; i++)
				{
					sampleValues[x][y][i] = Double.NEGATIVE_INFINITY;
					// System.out.println(sampleValues[x][y][i]);
				}
			}
		}
		
	}
	
	public void compute()
	{
		System.out.println("Averages");
		
		log.info("X Steps : " + xSteps);
		log.info("Y Steps : " + ySteps);
		log.info("Samples : " + samples);
		
		avg = new double[xSteps][ySteps];
		for(int x = 0; x < xSteps; x++)
		{
			for(int y = 0; y < ySteps; y++)
			{
				double total = 0;
				
				for(int i = 0; i < samples; i++)
				{
					if(sampleValues[x][y][i] == Double.NEGATIVE_INFINITY)
					{
						log.warn("X " +x + " y " + y + " s " + i + " value not correct " + sampleValues[x][y][i]);
					}
					
					total += sampleValues[x][y][i];
				}
				
				avg[x][y] = (total / samples);
				
				// log.info("X " +x + " y " + y + " avg " + avg[x][y]);

			}
		}
		
		System.out.println("Standard Deviations");
		stdDev = new double[xSteps][ySteps];
		for(int x = 0; x < xSteps; x++)
		{
			for(int y = 0; y < ySteps; y++)
			{
				double total = 0;
				for(int i = 0; i < samples; i++)
				{
					total += (sampleValues[x][y][i] - avg[x][y]) * (sampleValues[x][y][i] - avg[x][y]);
				}
				
				stdDev[x][y] = Math.sqrt(total / samples);
				// System.out.println(stdDev[x][y]);
				
			}
		}
		
		sampleValues = null;
	}
	
	public double getStandardDeviations(int getX, int getY)
	{
		return stdDev[getX][getY];
	}
	
	public double getAvgs(int getX, int getY)
	{
		return avg[getX][getY];
	}
	
	public boolean setSampleValue(int x, int y, double value)
	{
		if(x > xPosMax)
		{
			xPosMax = x;
		}
		
		if(x < xPosMin)
		{
			xPosMin = x;
		}
		
		if(y > yPosMax)
		{
			yPosMax = y;
		}
		
		if(y < yPosMin)
		{
			yPosMin = y;
		}
		
		if(value > zMax)
		{
			zMax = value;
		}
		
		if(value < zMin)
		{
			zMin = value;
		}
		
		for(int i = 0; i < samples; i++)
		{
			// Find a free slot
			if(sampleValues[x][y][i] == Double.NEGATIVE_INFINITY)
			{
				sampleValues[x][y][i] = value;
				// System.out.println();
				// System.out.println(x);
				// System.out.println(y);
				// System.out.println(i);
				// System.out.println(sampleValues[x][y][i]);
				
				// log.info("X " +x + " y " + y + " sampleValues " + sampleValues[x][y][i]);
				
				return true;
			}
		}
		
		log.warn("NO Space for X : " + x + " Y : " + y + " V : " + value);
		
		return false;
	}
	
	public int getXMax()
	{
		return xPosMax;
	}
	
	public int getXMin()
	{
		return xPosMin;
	}
	
	public int getYMax()
	{
		return yPosMax;
	}
	
	public int getYMin()
	{
		return yPosMin;
	}
	
	public double getZMax()
	{
		return zMax;
	}
	
	public double getZMin()
	{
		return zMin;
	}
	
	public double getAVGValue(int x, int y)
	{
		return avg[x][y];
	}
	
	public double getStdDevValue(int x, int y)
	{
		return stdDev[x][y];
	}
	
	public int getSamples()
	{
		return samples;
	}
	
	public int getXSteps()
	{
		return xSteps;
	}
	
	public int getYSteps()
	{
		return ySteps;
	}
	
	public double[][] getAvgData()
	{
		return avg;
	}
	
	public double[][] getStdDevData()
	{
		return stdDev;
	}
	
}
