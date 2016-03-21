package jCompute.Batch.LogFileProcessor.LogFormatProcessor;

import java.io.IOException;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Surface.Average;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Surface.Max;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Surface.StandardDeviation;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Surface.SurfaceMetricInf;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Surface.SurfaceMetricInf.Type;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Values.SumTotal;

public class ItemLogFormatValuesContainer
{
	private static Logger log = LoggerFactory.getLogger(ItemLogFormatValuesContainer.class);
	
	private final int ySteps;
	private final int xSteps;
	
	private final int samples;
	
	private double[] sampleValues;
	
	private double maxRate;
	
	private int xPosMin = Integer.MAX_VALUE;
	private int xPosMax = Integer.MIN_VALUE;
	private int yPosMin = Integer.MAX_VALUE;
	private int yPosMax = Integer.MIN_VALUE;
	private double zMin = Double.MAX_VALUE;
	private double zMax = Double.MIN_VALUE;
	
	private boolean computed;
	
	private SurfaceMetricInf[] surfaceMetrics;
	
	public ItemLogFormatValuesContainer(int xSteps, int ySteps, int samples) throws IOException
	{
		this.ySteps = ySteps;
		this.xSteps = xSteps;
		this.samples = samples;
		
		log.debug("X Steps : " + xSteps);
		log.debug("Y Steps : " + ySteps);
		log.debug("Samples : " + samples);
		
		this.sampleValues = new double[xSteps * ySteps * samples];
		
		// Fill the array with invalid values (3d to 1d indexing)
		IntStream.range(0, ySteps).forEach(y -> IntStream.range(0, xSteps).forEach(x ->
		{
			for(int i = 0; i < samples; i++)
			{
				sampleValues[(x * ySteps + y) * samples + i] = Double.NEGATIVE_INFINITY;
			}
		}));
		
		computed = false;
	}
	
	public void compute(double rangeMax) throws IOException
	{
		if(computed)
		{
			log.error("Already Computed");
		}
		
		// Metrics
		surfaceMetrics = new SurfaceMetricInf[SurfaceMetricInf.Type.values().length];
		
		// Average
		log.info("Computing Averages");
		surfaceMetrics[SurfaceMetricInf.Type.AVERAGE.asInt()] = new Average(sampleValues, xSteps, ySteps, samples);
		
		log.info("Averages Min : " + surfaceMetrics[SurfaceMetricInf.Type.AVERAGE.asInt()].getMin());
		log.info("Averages Max : " + surfaceMetrics[SurfaceMetricInf.Type.AVERAGE.asInt()].getMax());
		
		// Standard Dev
		log.info("Computing Standard Deviations");
		surfaceMetrics[SurfaceMetricInf.Type.STANDARD_DEVIATION.asInt()] = new StandardDeviation(sampleValues, surfaceMetrics[SurfaceMetricInf.Type.AVERAGE.ordinal()].getResult(), xSteps, ySteps,
				samples);
				
		log.info("Standard Deviations Min : " + surfaceMetrics[SurfaceMetricInf.Type.STANDARD_DEVIATION.asInt()].getMin());
		log.info("Standard Deviations Max : " + surfaceMetrics[SurfaceMetricInf.Type.STANDARD_DEVIATION.asInt()].getMax());
		
		// Max
		log.info("Computing Avg Max Total Surface");
		surfaceMetrics[SurfaceMetricInf.Type.MAX.asInt()] = new Max(surfaceMetrics[SurfaceMetricInf.Type.AVERAGE.asInt()].getResult(), xSteps, ySteps, rangeMax);
		double maxTotal = new SumTotal(surfaceMetrics[SurfaceMetricInf.Type.MAX.asInt()].getResult()).getResult();
		
		log.info("Avg Max Total Surface Min : " + surfaceMetrics[SurfaceMetricInf.Type.MAX.asInt()].getMin());
		log.info("Avg Max Total Surface Max : " + surfaceMetrics[SurfaceMetricInf.Type.MAX.asInt()].getMax());
		
		// Max possible area total
		double maxPossible = xSteps * ySteps * rangeMax;
		
		// Ratio of Max surface to possible surface
		maxRate = maxTotal / maxPossible;
		
		log.info("maxVal : " + rangeMax);
		log.info("maxTotal : " + maxTotal);
		log.info("maxPossible : " + maxPossible);
		
		log.info("Max Rate : " + maxRate);
		
		sampleValues = null;
		
		computed = true;
	}
	
	public double[] getMetricArray(Type metricSource)
	{
		return surfaceMetrics[metricSource.asInt()].getResult();
	}
	
	public double getDataMetricMax(Type metricSource)
	{
		return surfaceMetrics[metricSource.asInt()].getMax();
	}
	
	public double getDataMetricMin(Type metricSource)
	{
		return surfaceMetrics[metricSource.asInt()].getMin();
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
	
	public double getMaxRate()
	{
		return maxRate;
	}
}
