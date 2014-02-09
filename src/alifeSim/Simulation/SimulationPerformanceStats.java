package alifeSim.Simulation;

public class SimulationPerformanceStats
{
	/** Simulation Performance Indicators */
	private long stepStartTime = 0;
	private long stepEndTime = 0;
	private long stepTotalTime = 0; // Total Simulation run-time is the time taken per step for each step	
	
	long simulationSteps;
			
	private SimulationPerformanceStatsOutputInf outputTarget;
	
	public SimulationPerformanceStats(SimulationPerformanceStatsOutputInf output)
	{
		if(output == null)
		{
			System.out.println("SimulationPerformanceStats is now using SimulationPerformanceStatsNullOutput");
			outputTarget = new SimulationPerformanceStatsNullOutput();
		}
		else
		{
			System.out.println("SimulationPerformanceStats is now set");
			outputTarget = output;
		}
		
		clearSimulationStats();
	}
	
	public long getSimulationSteps()
	{
		return simulationSteps;
	}

	public void incrementSimulationSteps()
	{
		simulationSteps++;
	}
	
	public void updateStatsOutput()
	{
		if(simulationSteps%15 == 0)
		{
			outputTarget.setASPS((int)((float)simulationSteps/((float)stepTotalTime/1000f)));			
			outputTarget.setStepNo(simulationSteps);
			outputTarget.setTime(stepTotalTime);
		}
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
		outputTarget.clearStats();
	}

}
