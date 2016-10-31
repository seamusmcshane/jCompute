package jcompute.results.trace.samples;

public class DoubleTraceSample extends TraceSample
{
	public final double value;
	
	public DoubleTraceSample(double time, double value)
	{
		super(time);
		
		this.value = value;
	}

	@Override
	public String toString()
	{
		return String.valueOf(value);
	}
}
