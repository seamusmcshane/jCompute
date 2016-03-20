package jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Surface;

import java.util.stream.IntStream;

import jCompute.util.JCMath;

public class StandardDeviation implements SurfaceMetricInf
{
	private double[] output;
	
	private double outputMin;
	private double outputMax;
	
	private final Type type = Type.STANDARD_DEVIATION;
	
	public StandardDeviation(double[] values, double[] meanValues, int xSteps, int ySteps, int samples)
	{
		output = new double[xSteps * ySteps];
		
		// Takes values that is a flat 3d array representing X/Y/Samples
		// Takes meanValues that is a flat 2d array of arithmetic mean averages
		// Creates flat 2d array of standard deviations
		IntStream.range(0, ySteps).parallel().forEach(y -> IntStream.range(0, xSteps).parallel().forEach(x ->
		{
			// Total Variance of all samples
			double variance = 0;
			
			// Calculate variance of samples for each XY
			for(int s = 0; s < samples; s++)
			{
				double currentValue = values[(x * ySteps + y) * samples + s];
				
				// average of all samples
				double mean = meanValues[x * ySteps + y];
				
				// Difference from mean
				double difference = currentValue - mean;
				
				// Squared
				double differenceSqr = difference * difference;
				
				// Total the variance
				variance += differenceSqr;
			}
			
			// The Standard Deviation for these samples
			output[x * ySteps + y] = Math.sqrt(variance / samples);
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
