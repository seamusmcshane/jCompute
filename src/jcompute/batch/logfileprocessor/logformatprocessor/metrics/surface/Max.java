package jcompute.batch.logfileprocessor.logformatprocessor.metrics.surface;

import java.util.stream.IntStream;

import jcompute.util.JCMath;

public class Max implements SurfaceMetricInf
{
	private double[] output;
	
	private double outputMin;
	private double outputMax;
	
	private final Type type = Type.STANDARD_DEVIATION;
	
	public Max(double[] values, int xSteps, int ySteps, double maxValue)
	{
		output = new double[xSteps * ySteps];
		
		// Takes values that is a flat 2d array representing X/Y values
		// Creates flat 2d array with the max value intersection
		IntStream.range(0, ySteps).parallel().forEach(y ->
		{
			for(int x = 0; x < xSteps; x++)
			{
				if(values[x * ySteps + y] == maxValue)
				{
					output[x * ySteps + y] = maxValue;
				}
				else
				{
					output[x * ySteps + y] = 0;
				}
			}
		});
		
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
