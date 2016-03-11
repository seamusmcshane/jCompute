package jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Values;

public class SumTotal implements ValueMetricInf
{
	private double output;
	
	private final Type type = Type.VALUE_TOTAL;
	
	public SumTotal(double[] values)
	{
		double total = 0;
		
		for(double value : values)
		{
			total += value;
		}
		
		output = total;
	}
	
	@Override
	public double getResult()
	{
		return output;
	}
	
	@Override
	public Type getType()
	{
		return type;
	}
	
}
