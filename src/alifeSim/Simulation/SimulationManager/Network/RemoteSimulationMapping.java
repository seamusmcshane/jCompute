package alifeSim.Simulation.SimulationManager.Network;

public class RemoteSimulationMapping
{
	private int localSimId;
	private int remoteSimId;
	private int nodeUid;
	
	public RemoteSimulationMapping(int localSimId,int remoteSimId,int nodeUid)
	{
		this.localSimId = localSimId;
		this.remoteSimId = remoteSimId;
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

}
