package alifeSim.Simulation;

public class SimulationStats
{
	/** Sim ref for call back */
	private Simulation sim;
	
	/** Simulation Performance Indicators */
	private long stepStartTime = 0;
	private long stepEndTime = 0;
	private long stepTotalTime = 0; // Total Simulation run-time is the time taken per step for each step	
	
	/** Simulation Step Counter */
	private int simulationSteps;
	
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
		sim.statChanged(stepTotalTime,simulationSteps,getAverageStepRate());
	}
	
	/** Interface for call back */
	public interface statChangedInf
	{
		public void statChanged(long time,int stepNo,int asps);
	}
}
