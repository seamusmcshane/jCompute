package alifeSim.Simulation;

import alifeSim.Gui.SimulationGUI;
import alifeSim.Gui.StatsPanel;

public class SimulationPerformanceStats
{
	/** Simulation Performance Indicators */
	//long stepNo = 0;
	private long stepStartTime = 0;
	private long stepEndTime = 0;
	private long stepTotalTime = 0; // Total Simulation run-time is the time taken per step for each step	
	
	
	long simulationSteps;
	long simulationRuntime;
	
	// Average Steps per second
	int averageStepsPerSecond;
	private int numSamples = 150;	
	private float stepSamples[];
	private float tasps; 		// To avoid a cumulative rounding error when calculating the average, a double is use
	private float sps;	 			// Average Steps Per Second as an int for display purposes
	
	// Step Per Second Calculations
	private long startTime;
	private long previousTime;
	private long currentTime;
	private long diffTime;
	
	public SimulationPerformanceStats()
	{
		clearSimulationStats();
	}
	
	public long getSimulationSteps()
	{
		return simulationSteps;
	}

	public void incrementSimulationSteps()
	{
		simulationSteps++;
		//System.out.println("Step " + simulationSteps);
	}

	public long getSimulationRuntime()
	{
		return simulationRuntime;
	}

	public void setSimulationRuntime(long simulationRuntime)
	{
		this.simulationRuntime = simulationRuntime;
	}

	public int getAverageStepsPerSecond()
	{
		return averageStepsPerSecond;
	}

	public void setAverageStepsPerSecond(int averageStepsPerSecond)
	{
		this.averageStepsPerSecond = averageStepsPerSecond;
	}
	
	/**
	 * Average the steps thus giving an average steps per second count
	 * @return int */
	public int averageStepsPerSecond()
	{
		return (int) (tasps / numSamples);
	}
	
	public void calcTasp()
	{		
		for (int i = 0; i < numSamples; i++)
		{
			tasps += stepSamples[i];				// Total all the steps
		}
	}

	public void simStatsDisplay()
	{
		if(simulationSteps%15 == 0)
		{
			SimulationGUI.setASPS(averageStepsPerSecond());
			SimulationGUI.setStepNo(simulationSteps);
			SimulationGUI.setTime(stepTotalTime);
			SimulationGUI.setTime(stepTotalTime);
		}
		StatsPanel.updateGraphs(simulationSteps);
	}
	
	/**
	 * Initializes the average steps per second counters
	 */
	public void setUpStepsPerSecond()
	{
		startTime = System.nanoTime();
		previousTime = startTime;				// At Start up this is true
		currentTime = System.nanoTime();
		diffTime = currentTime - previousTime;	// Diff time is initialized
	}

	/**
	 * Calculates the Average Steps Per Second
	 */
	public void calcStepsPerSecond()
	{
		currentTime = System.nanoTime();			// Current TIme

		diffTime = currentTime - previousTime;		// Time between this and the last call				

		sps = 1000f / (diffTime / 1000000f);		//  converts diff time to milliseconds then gives a instantaneous performance indicator of steps per second		

		previousTime = currentTime;		 			// Stores the current diff for the diff in the next iteration

		for (int i = 0; i < (numSamples - 1); i++)	// Moves the previous samples back by 1, leaves space for the new sps sample 
		{
			stepSamples[i] = stepSamples[(i + 1)];
		}

		stepSamples[numSamples - 1] = sps;			// Store the new sps sample

		tasps = 0;									// clear the old total average (or it will increment for ever)
		
		for (int i = 0; i < numSamples; i++)
		{
			tasps += stepSamples[i];				// Total all the steps
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
	
	public void clearSimulationStats()
	{
		stepTotalTime = 0;
		simulationSteps = 0;
		simulationRuntime = 0;
		averageStepsPerSecond = 0;
		tasps = 0;
		sps = 0;
		
		stepSamples = new float[numSamples];
	}
	
	
		
}
