package alifeSim.Simulation;

public interface SimulationPerformanceStatsOutputInf
{
	public void setASPS(int averageStepsPerSecond);
	public void setStepNo(long simulationSteps);
	public void setTime(long stepTotalTime);
	public void clearStats();
}
