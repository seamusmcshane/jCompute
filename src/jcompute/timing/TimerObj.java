package jcompute.timing;

public class TimerObj
{
	private long startTime;
	private long endTime;

	public TimerObj()
	{
		
	}
	
	public void startTimer()
	{
		startTime = System.currentTimeMillis(); // Start time for the average step		
	}
	
	public void stopTimer()
	{
		endTime = System.currentTimeMillis();
	}
	
	public long getTimeTaken()
	{
		return endTime - startTime;
	}
	
}
