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
	
	/** Special Value used to moderate the event frequency and calculate progress 
	 * If set then frequency is moderated to 1 percent of progress.
	 * Else it will be every 100 steps;
	 * */
	private int endStepNum;
	private int eventFreq = 100;
	
	public SimulationStats(Simulation sim)
	{
		this.sim = sim;
		
		clearSimulationStats();
	}
		
	public int getSimulationSteps()
	{
		return simulationSteps;
	}

	public void incrementSimulationSteps()
	{
		simulationSteps++;
		
		simCallBack(false);
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
		
		simCallBack(true);
	}
	
	private void simCallBack(boolean override)
	{
		// This section is inverted we test instead if we are to abort the event trigger
		if(!override)
		{
			if( (simulationSteps % eventFreq) != 0 )
			{
				return;
			}
		}
		
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
		
		calulateEventFreq(endStepNum);
	}
	
	public void calulateEventFreq(int endStep)
	{
		if(endStep > 0)
		{
			eventFreq = (int) Math.ceil(endStepNum/100f);
		}
		else
		{
			eventFreq = 100;
		}
	}
}
