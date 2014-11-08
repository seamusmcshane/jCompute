package jCompute.Cluster.Node;

public class NodeConfiguration
{
	private int maxSims = -1;
	private int uid = -1;
	private long weighting = Long.MAX_VALUE;
	private String address = "0.0.0.0";
	private long simulationsProcessed = 0;

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

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public void incrementSimulationsProcessed()
	{
		simulationsProcessed++;
	}

	public long getSimulationsProcessed()
	{
		return simulationsProcessed;
	}
}
