package jCompute.Simulation.SimulationManager.Network.Manager;

public class RemoteSimulationMapping
{
	private int localSimId;
	private int remoteSimId;
	private int nodeUid;
	
	public RemoteSimulationMapping(int nodeUid)
	{
		this.nodeUid = nodeUid;
	}	
	
	public int getNodeUid()
	{
		return nodeUid;
	}
	
	public int getLocalSimId()
	{
		return localSimId;
	}
	
	public int getRemoteSimId()
	{
		return remoteSimId;
	}

	public void setLocalSimId(int localSimId)
	{
		this.localSimId = localSimId;		
	}

	public void setRemoteSimId(int remoteSimId)
	{
		this.remoteSimId = remoteSimId;		
	}

	public String info()
	{
		return "Mapping - Node : " + nodeUid + " Lsid " + localSimId + " rSid " + remoteSimId;
	}

}
