package jCompute.Batch.LogFileProcessor.LogFormatProcessor;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFormatValuesContainer
{
	private static Logger log = LoggerFactory.getLogger(LogFormatValuesContainer.class);
	
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
	
	private ForkJoinPool forkJoinPool;
	
	private boolean computed;
	
	public LogFormatValuesContainer(int xSteps, int ySteps, int samples) throws IOException
	{
		this.ySteps = ySteps;
		this.xSteps = xSteps;
		this.samples = samples;
		
		log.debug("X Steps : " + xSteps);
		log.debug("Y Steps : " + ySteps);
		log.debug("Samples : " + samples);
		
		this.sampleValues = new double[xSteps * ySteps * samples];
		
		// Create a pool with threads matching processors
		forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		
		// Fill the array with invalid values (3d to 1d indexing)
		ForkJoinTask<?> clearTask = forkJoinPool.submit(() -> IntStream.range(0, ySteps).forEach(y -> IntStream.range(0, xSteps).forEach(x ->
		{
			for(int i = 0; i < samples; i++)
			{
				sampleValues[(x * ySteps + y) * samples + i] = Double.NEGATIVE_INFINITY;
			}
		})));
		
		/*
		 * Wait on our task and wait
		 * As this stream is processing a file, we throw an IOException if the task it self throws any exception.
		 */
		try
		{
			clearTask.get();
		}
		catch(InterruptedException | ExecutionException e)
		{
			throw new IOException(e);
		}
		
		computed = false;
	}
	
	public void compute(double rangeMax) throws IOException
	{
		if(computed)
		{
			log.error("Already Computed");
		}
		
		log.info("Computing Averages");
		
		log.debug("X Steps : " + xSteps);
		log.debug("Y Steps : " + ySteps);
		log.debug("Samples : " + samples);
		
		avg = new double[xSteps * ySteps];
		
		ForkJoinTask<?> avgTask = forkJoinPool.submit(() -> IntStream.range(0, ySteps).parallel().forEach(y -> IntStream.range(0, xSteps).parallel().forEach(x ->
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
			
		})));
		
		/*
		 * Wait on our task and wait
		 * As this stream is processing a file, we throw an IOException if the task it self throws any exception.
		 */
		try
		{
			avgTask.get();
		}
		catch(InterruptedException | ExecutionException e)
		{
			throw new IOException(e);
		}
		
		log.info("Computing Standard Deviations");
		stdDev = new double[xSteps * ySteps];
		
		ForkJoinTask<?> stdDevTask = forkJoinPool.submit(() -> IntStream.range(0, ySteps).parallel().forEach(y -> IntStream.range(0, xSteps).parallel().forEach(x ->
		{
			double total = 0;
			for(int i = 0; i < samples; i++)
			{
				total += (sampleValues[(x * ySteps + y) * samples + i] - avg[x * ySteps + y]) * (sampleValues[(x * ySteps + y) * samples + i] - avg[x * ySteps + y]);
			}
			
			stdDev[x * ySteps + y] = Math.sqrt(total / samples);
		})));
		
		/*
		 * Wait on our task and wait
		 * As this stream is processing a file, we throw an IOException if the task it self throws any exception.
		 */
		try
		{
			stdDevTask.get();
		}
		catch(InterruptedException | ExecutionException e)
		{
			throw new IOException(e);
		}
		
		log.info("Computing Avg Max Total");
		max = new double[xSteps * ySteps];
		long avgMaxTotalY[] = new long[ySteps];
		ForkJoinTask<?> maxTask = forkJoinPool.submit(() -> IntStream.range(0, ySteps).parallel().forEach(y ->
		{
			for(int x = 0; x < xSteps; x++)
			{
				if(avg[x * ySteps + y] == rangeMax)
				{
					avgMaxTotalY[y] += avg[x * ySteps + y];
					
					max[x * ySteps + y] = rangeMax;
				}
				else
				{
					max[x * ySteps + y] = 0;
				}
			}
		}));
		
		/*
		 * Wait on our task and wait
		 * As this stream is processing a file, we throw an IOException if the task it self throws any exception.
		 */
		try
		{
			maxTask.get();
		}
		catch(InterruptedException | ExecutionException e)
		{
			throw new IOException(e);
		}
		
		// Free up the threads in the pool
		forkJoinPool.shutdown();
		
		long avgMaxTotal = 0;
		for(int y = 0; y < ySteps; y++)
		{
			avgMaxTotal += avgMaxTotalY[y];
		}
		
		// Max possible area total
		double maxTotal = (double) (xSteps * ySteps) * rangeMax;
		
		log.debug("maxVal : " + rangeMax);
		log.debug("maxTotal : " + maxTotal);
		log.debug("avgMaxTotal : " + avgMaxTotal);
		
		maxRate = (double) ((double) avgMaxTotal / maxTotal);
		
		log.info("Max Rate : " + maxRate);
		
		sampleValues = null;
		
		computed = true;
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
		
		log.error("LogFormatValuesContainer NO Space for X : " + x + " Y : " + y + " V : " + value);
		
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
	
	public double[] getAvgDataFlat()
	{
		return avg;
	}
	
	public double[][] getAvgData2d()
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
	
	public double[] getStdDevDataFlat()
	{
		return stdDev;
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
	
	public double[] getMaxgDataFlat()
	{
		return max;
	}
	
	public double getMaxRate()
	{
		return maxRate;
	}
	
}
