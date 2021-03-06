package jcompute.cluster.computenode.nodedetails;

public class NodeStatsSample
{
	// Historical Averaged
	private int cpuUsage;
	private int simulationsActive;
	private int statisticsPendingFetch;
	private int jvmMemoryUsedPercentage;
	
	// Instant
	private long simulationsProcessed;
	private long bytesTX;
	private long txS;
	private long bytesRX;
	private long rxS;
	private long avgRTT;
	
	public void setBytesTX(long bytesTX)
	{
		this.bytesTX = bytesTX;
	}
	
	public long getBytesTX()
	{
		return bytesTX;
	}
	
	public void setBytesRX(long bytesRX)
	{
		this.bytesRX = bytesRX;
	}
	
	public long getBytesRX()
	{
		return bytesRX;
	}
	
	public void setCpuUsage(int cpuUsage)
	{
		this.cpuUsage = cpuUsage;
	}
	
	public void setSimulationsProcessed(long simulationsProcessed)
	{
		this.simulationsProcessed = simulationsProcessed;
	}
	
	public int getCpuUsage()
	{
		return cpuUsage;
	}
	
	public long getSimulationsProcessed()
	{
		return simulationsProcessed;
	}
	
	public int getSimulationsActive()
	{
		return simulationsActive;
	}
	
	public int getStatisticsPendingFetch()
	{
		return statisticsPendingFetch;
	}
	
	public void setSimulationsActive(int simulationsActive)
	{
		this.simulationsActive = simulationsActive;
	}
	
	public void setStatisticsPendingFetch(int statisticsPendingFetch)
	{
		this.statisticsPendingFetch = statisticsPendingFetch;
	}
	
	public void setJvmMemoryUsedPercentage(int jvmMemoryUsedPercentage)
	{
		this.jvmMemoryUsedPercentage = jvmMemoryUsedPercentage;
	}
	
	public int getJvmMemoryUsedPercentage()
	{
		return jvmMemoryUsedPercentage;
	}
	
	public void setTXS(long txS)
	{
		this.txS = txS;
	}
	
	public long getTXS()
	{
		return txS;
	}
	
	public void setRXS(long rxS)
	{
		this.rxS = rxS;
	}
	
	public long getRXS()
	{
		return rxS;
	}
	
	public void setAvgRTT(long avgRTT)
	{
		this.avgRTT = avgRTT;
	}
	
	public long getAvgRTT()
	{
		return avgRTT;
	}
	
	public void reset()
	{
		cpuUsage = 0;
		simulationsProcessed = 0;
		simulationsActive = 0;
		statisticsPendingFetch = 0;
		jvmMemoryUsedPercentage = 0;
		
		bytesTX = 0;
		txS = 0;
		bytesRX = 0;
		rxS = 0;
		
		avgRTT = 0;
	}
}
