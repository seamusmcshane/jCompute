package jCompute.Simulation.SimulationManager.Network.Node;

public class NodeConfiguration
{
	private int maxSims = -1;
	private int uid = -1;
	private long weighting = Long.MAX_VALUE;
	
	public void setUid(int uid)
	{
		this.uid = uid;
	}
	
	public int getUid()
	{
		return uid;
	}
	
	public void setMaxSims(int maxSims)
	{
		this.maxSims = maxSims;
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
	
	public long getWeighting()
	{
		return weighting;
	}

	public void setWeighting(long weighting)
	{
		this.weighting = weighting;
	}
}
