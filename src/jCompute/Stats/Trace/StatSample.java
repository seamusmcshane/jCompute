package jCompute.Stats.Trace;

public class StatSample
{
	private double time;
	private double sample;
	
	public StatSample(double time,double sample)
	{
		this.time = time;
		this.sample = sample;
	}
	
	public double getTime()
	{
		return time;
	}
	
	public double getSample()
	{
		return sample;
	}
	
}
