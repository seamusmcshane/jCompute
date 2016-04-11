package jCompute.cluster.computenode.nodedetails;

public class NodeInfo
{
	private int maxSims = -1;
	private int uid = -1;
	private long weighting = Long.MAX_VALUE;
	private String address = "0.0.0.0";
	private String desc = "";
	
	private String operatingSystem;
	private String systemArch;
	private int totalThreads;
	private int totalOSMemory;
	private int maxJVMMemory;
	
	public void setDescription(String desc)
	{
		this.desc = desc;
	}
	
	public String getDescription()
	{
		return desc;
	}
	
	public int getTotalOSMemory()
	{
		return totalOSMemory;
	}
	
	public void setTotalOSMemory(int totalOSMemory)
	{
		this.totalOSMemory = totalOSMemory;
	}
	
	public int getMaxJVMMemory()
	{
		return maxJVMMemory;
	}
	
	public void setMaxJVMMemory(int maxJVMMemory)
	{
		this.maxJVMMemory = maxJVMMemory;
	}
	
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
	
	public String getOperatingSystem()
	{
		return operatingSystem;
	}
	
	public String getSystemArch()
	{
		return systemArch;
	}
	
	public int getHWThreads()
	{
		return totalThreads;
	}
	
	public void setOperatingSystem(String operatingSystem)
	{
		this.operatingSystem = operatingSystem;
	}
	
	public void setSystemArch(String systemArch)
	{
		this.systemArch = systemArch;
	}
	
	public void setHWThreads(int totalThreads)
	{
		this.totalThreads = totalThreads;
	}
}
