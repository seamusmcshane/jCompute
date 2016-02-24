package jCompute.Batch.LogFileProcessor.Mapper;

import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapperValuesContainer
{
	private static Logger log = LoggerFactory.getLogger(MapperValuesContainer.class);
	
	private final int ySteps;
	private final int xSteps;
	private final int samples;
	private double[] sampleValues;
	
	private double[] avg;
	
	private double[] stdDev;
	
	private double[] max;
	
	private double maxRate;
	
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
		
		this.sampleValues = new double[xSteps * ySteps * samples];
		
		// Fill the array with invalid values (3d to 1d indexing)
		IntStream.range(0, ySteps).forEach(y -> IntStream.range(0, xSteps).forEach(x ->
		{
			for(int i = 0; i < samples; i++)
			{
				sampleValues[(x * ySteps + y) * samples + i] = Double.NEGATIVE_INFINITY;
			}
		}));
		
	}
	
	public void compute(int maxVal)
	{
		System.out.println("Averages");
		
		System.out.println("X Steps : " + xSteps);
		System.out.println("Y Steps : " + ySteps);
		System.out.println("Samples : " + samples);
		
		avg = new double[xSteps * ySteps];
		
		IntStream.range(0, ySteps).forEach(y -> IntStream.range(0, xSteps).forEach(x ->
		{
			double total = 0;
			
			for(int i = 0; i < samples; i++)
			{
				if(sampleValues[(x * ySteps + y) * samples + i] == Double.NEGATIVE_INFINITY)
				{
					log.warn("X " + x + " y " + y + " s " + i + " value not correct " + sampleValues[(x * ySteps + y) * samples + i]);
				}
				
				total += sampleValues[(x * ySteps + y) * samples + i];
			}
			
			avg[x * ySteps + y] = (total / samples);
			
		}));
		
		System.out.println("Standard Deviations");
		stdDev = new double[xSteps * ySteps];
		
		IntStream.range(0, ySteps).forEach(y -> IntStream.range(0, xSteps).forEach(x ->
		{
			double total = 0;
			for(int i = 0; i < samples; i++)
			{
				total += (sampleValues[(x * ySteps + y) * samples + i] - avg[x * ySteps + y]) * (sampleValues[(x * ySteps + y) * samples + i] - avg[x * ySteps + y]);
			}
			
			stdDev[x * ySteps + y] = Math.sqrt(total / samples);
		}));
		
		max = new double[xSteps * ySteps];
		
		int avgMaxTotalY[] = new int[ySteps];
		int maxTotal = xSteps * ySteps * maxVal;
		
		IntStream.range(0, ySteps).parallel().forEach(y ->
		{
			for(int x = 0; x < xSteps; x++)
			{
				if(avg[x * ySteps + y] == maxVal)
				{
					avgMaxTotalY[y] += avg[x * ySteps + y];
					
					max[x * ySteps + y] = maxVal;
				}
				else
				{
					max[x * ySteps + y] = 0;
				}
			}
		});
		
		System.out.println("Max Total");
		int avgMaxTotal = 0;
		for(int y = 0; y < ySteps; y++)
		{
			avgMaxTotal += avgMaxTotalY[y];
		}
		
		maxRate = (double) ((double) avgMaxTotal / (double) maxTotal);
		
		System.out.println("Max Rate : " + maxRate);
		
		sampleValues = null;
	}
	
	public double getMax(int x, int y)
	{
		return max[x * ySteps + y];
	}
	
	public double getStandardDeviations(int x, int y)
	{
		return stdDev[x * ySteps + y];
	}
	
	public double getAvgs(int x, int y)
	{
		return avg[x * ySteps + y];
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
			if(sampleValues[(x * ySteps + y) * samples + i] == Double.NEGATIVE_INFINITY)
			{
				sampleValues[(x * ySteps + y) * samples + i] = value;
				// System.out.println();
				// System.out.println(x);
				// System.out.println(y);
				// System.out.println(i);
				// System.out.println(sampleValues[x][y][i]);
				
				// log.info("X " +x + " y " + y + " sampleValues " + sampleValues[x][y][i]);
				
				return true;
			}
		}
		
		log.error("NO Space for X : " + x + " Y : " + y + " V : " + value);
		
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
		return avg[x * ySteps + y];
	}
	
	public double getStdDevValue(int x, int y)
	{
		return stdDev[x * ySteps + y];
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
		// 1d to 2d
		double[][] data = new double[xSteps][xSteps];
		for(int y = 0; y < ySteps; y++)
		{
			for(int x = 0; x < xSteps; x++)
			{
				data[x][y] = avg[x * ySteps + y];
			}
		}
		
		return data;
	}
	
	public double[][] getStdDevData()
	{
		// 1d to 2d
		double[][] data = new double[xSteps][xSteps];
		for(int y = 0; y < ySteps; y++)
		{
			for(int x = 0; x < xSteps; x++)
			{
				data[x][y] = stdDev[x * ySteps + y];
			}
		}
		
		return data;
	}
	
	public double getMaxRate()
	{
		return maxRate;
	}
	
}
