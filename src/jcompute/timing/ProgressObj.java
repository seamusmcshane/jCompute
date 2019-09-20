package jcompute.timing;

public class ProgressObj
{
	private final double TICKS;
	
	private double tickCount;
	
	public ProgressObj(int ticks)
	{
		this.TICKS = ticks;
	}
	
	public void tick()
	{
		tickCount++;
	}
	
	public double progress()
	{
		return tickCount / TICKS;
	}
	
	public double progressAsPercentage()
	{
		return progress() * 100.0;
	}
}
