package jCompute.Cluster.Node;

public class NodeStats
{
	private int cpuUsage;
	private int freeMemory;
	private long simulationsProcessed;

	public void setCpuUsage(int cpuUsage)
	{
		this.cpuUsage = cpuUsage;
	}

	public void setFreeMemory(int freeMemory)
	{
		this.freeMemory = freeMemory;
	}

	public void setSimulationsProcessed(long simulationsProcessed)
	{
		this.simulationsProcessed = simulationsProcessed;
	}

	public int getCpuUsage()
	{
		return cpuUsage;
	}

	public int getFreeMemory()
	{
		return freeMemory;
	}

	public long getSimulationsProcessed()
	{
		return simulationsProcessed;
	}

}
