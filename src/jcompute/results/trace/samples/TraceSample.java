package jcompute.results.trace.samples;

public abstract class TraceSample
{
	public final double time;
	
	public TraceSample(double time)
	{
		this.time = time;
	}
	
	@Override
	public abstract String toString();
}
