package jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Surface;

import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.util.JCMath;

public class Average implements SurfaceMetricInf
{
	private static Logger log = LoggerFactory.getLogger(Average.class);
	
	private double[] output;
	
	private double outputMin;
	private double outputMax;
	
	private final Type type = Type.AVERAGE;
	
	public Average(double[] values, int xSteps, int ySteps, int samples)
	{
		output = new double[xSteps * ySteps];
		
		// Takes values that is a flat 3d array representing X/Y/Samples
		// Creates flat 2d array of arithmetic mean averages
		IntStream.range(0, ySteps).parallel().forEach(y -> IntStream.range(0, xSteps).parallel().forEach(x ->
		{
			double total = 0;
			
			for(int i = 0; i < samples; i++)
			{
				if(values[(x * ySteps + y) * samples + i] == Double.NEGATIVE_INFINITY)
				{
					log.warn("X " + x + " y " + y + " s " + i + " value not correct " + values[(x * ySteps + y) * samples + i]);
				}
				
				total += values[(x * ySteps + y) * samples + i];
			}
			
			output[x * ySteps + y] = (total / samples);
			
		}));
		
		// Set the min and max values
		outputMin = output[JCMath.findMinValueIndex(output)];
		outputMax = output[JCMath.findMaxValueIndex(output)];
	}
	
	@Override
	public double[] getResult()
	{
		return output;
	}
	
	@Override
	public double getMin()
	{
		return outputMin;
	}
	
	@Override
	public double getMax()
	{
		return outputMax;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
}
