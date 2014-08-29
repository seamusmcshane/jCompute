package jCompute.Simulation;

public class SimulationStats
{
	/** Sim ref for call back */
	private Simulation sim;
	
	/** Simulation Performance Indicators */
	private long stepStartTime = 0;
	private long stepEndTime = 0;
	private long stepTotalTime = 0; // Total Simulation run-time is the time taken per step for each step	
		
	private int progress = -1;
	
	/** Simulation Step Counter */
	private int simulationSteps;
	
	/** Special Value used to calculate progress */
	private int endStepNum = -1;
	
	/**
	 * Stat Event Freq Rate Limit (every valueof eventFreq in milliseconds)
	 */
	private int eventFreq = 1000;
	private long prevEventTimeMillis = 0;
	
	public SimulationStats(Simulation sim)
	{
		this.sim = sim;
		
		clearSimulationStats();
		
		prevEventTimeMillis = System.currentTimeMillis();
	}
		
	public int getSimulationSteps()
	{
		return simulationSteps;
	}

	public void incrementSimulationSteps()
	{
		simulationSteps++;
		
		simCallBack();
	}
	
	public int getAverageStepRate()
	{
		if(simulationSteps<100)
		{
			return 0;
		}
		
		return (int)((float)simulationSteps/((float)stepTotalTime/1000f));
	}
	
	public void setStepStartTime()
	{
		stepStartTime = System.currentTimeMillis(); // Start time for the average step		
	}

	public void setStepEndTime()
	{
		stepEndTime = System.currentTimeMillis();

		stepTotalTime += stepEndTime - stepStartTime;	
	}
	
	public long getTotalTime()
	{
		return stepTotalTime;
	}
	
	public void clearSimulationStats()
	{
		stepTotalTime = 0;
		simulationSteps = 0;
		
		simCallBack();
	}
	
	private void simCallBack()
	{
		long currentEventTimeMillis = System.currentTimeMillis();
		long timeElapsed = currentEventTimeMillis - prevEventTimeMillis;
		
		if( timeElapsed < eventFreq && simulationSteps!=endStepNum)
		{
			return;
		}
		
		prevEventTimeMillis = currentEventTimeMillis;
		
		updateProgress();
		
		sim.statChanged(stepTotalTime,simulationSteps,progress,getAverageStepRate());
	}
	
	/** Interface for call back */
	public interface statChangedInf
	{
		public void statChanged(long time,int stepNo, int progress, int asps);
	}
	
	public void updateProgress()
	{
		progress = (int) ((float)(simulationSteps+1)/(float)endStepNum*100f);
	}

	public void setEndStep(int endStepNum)
	{
		this.endStepNum = endStepNum;
	}

}
