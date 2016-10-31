package jcompute.results.trace.samples;

public class IntegerTraceSample extends TraceSample
{
	public final int value;
	
	public IntegerTraceSample(double time, int value)
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
