package jCompute.Cluster.Node.NodeDetails;

public class NodeAveragedStats
{
	private int[] cpuUsage;
	private int[] simulationsActive;
	private int[] statisticsPendingFetch;
	private int[] jvmMemoryUsedPercentage;
	
	private int history;
	private int updateValue;
	
	public NodeAveragedStats(int history)
	{
		// Index 0
		this.history = history;
		
		reset();
	}
	
	public synchronized void update(int cpuUsage, int simulationsActive, int statisticsPendingFetch, int jvmMemoryUsedPercentage)
	{
		this.cpuUsage[updateValue] = cpuUsage;
		this.simulationsActive[updateValue] = simulationsActive;
		this.statisticsPendingFetch[updateValue] = statisticsPendingFetch;
		this.jvmMemoryUsedPercentage[updateValue] = jvmMemoryUsedPercentage;
		
		// Circular
		updateValue = (updateValue + 1) % history;
	}
	
	public synchronized void populateStatSample(NodeStatsSample sample)
	{
		double cpuUsageAvg = 0;
		double simulationsActiveAvg = 0;
		double statisticsPendingFetchAvg = 0;
		double jvmMemoryUsedPercentageAvg = 0;
		
		double cpuUsageTotal = 0;
		double simulationsActiveTotal = 0;
		double statisticsPendingFetchTotal = 0;
		double jvmMemoryUsedPercentageTotal = 0;
		
		// Create the Averages
		for(int i = 0; i < history; i++)
		{
			cpuUsageTotal += cpuUsage[i];
			simulationsActiveTotal += simulationsActive[i];
			statisticsPendingFetchTotal += statisticsPendingFetch[i];
			jvmMemoryUsedPercentageTotal += jvmMemoryUsedPercentage[i];
		}
		
		cpuUsageAvg = cpuUsageTotal / history;
		simulationsActiveAvg = simulationsActiveTotal / history;
		statisticsPendingFetchAvg = statisticsPendingFetchTotal / history;
		jvmMemoryUsedPercentageAvg = jvmMemoryUsedPercentageTotal / history;
		
		sample.setCpuUsage((int)Math.round(cpuUsageAvg));
		sample.setSimulationsActive((int)Math.round(simulationsActiveAvg));
		sample.setStatisticsPendingFetch((int)Math.round(statisticsPendingFetchAvg));
		sample.setJvmMemoryUsedPercentage((int)Math.round(jvmMemoryUsedPercentageAvg));

	}
	
	public synchronized void reset()
	{
		// Index 0
		updateValue = 0;
		
		cpuUsage = new int[history];
		simulationsActive = new int[history];
		statisticsPendingFetch = new int[history];
		jvmMemoryUsedPercentage = new int[history];
	}
}
