package jCompute.Simulation.Event;

public class SimulationStatChangedEvent
{
	private int simId;
	private long time;
	private int stepNo;
	private int progress;
	private int asps;
	
	public SimulationStatChangedEvent(int simId, long time, int stepNo, int progress, int asps)
	{
		this.simId = simId;
		this.time = time;
		this.stepNo = stepNo;
		this.progress = progress;
		this.asps = asps;
	}

	public int getSimId()
	{
		return simId;
	}

	public long getTime()
	{
		return time;
	}

	public int getStepNo()
	{
		return stepNo;
	}

	public int getProgress()
	{
		return progress;
	}

	public int getAsps()
	{
		return asps;
	}
	
}
