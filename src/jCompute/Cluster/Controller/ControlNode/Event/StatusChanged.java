package jCompute.Cluster.Controller.ControlNode.Event;

public class StatusChanged
{
	private String address;
	private String port;
	private String connectingNodes;
	private String activeNodes;
	private String maxActiveSims;
	private String AddedSims;
	
	public StatusChanged(String address, String port, String connectingNodes, String activeNodes, String maxActiveSims, String addedSims)
	{
		super();
		this.address = address;
		this.port = port;
		this.connectingNodes = connectingNodes;
		this.activeNodes = activeNodes;
		this.maxActiveSims = maxActiveSims;
		AddedSims = addedSims;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public String getPort()
	{
		return port;
	}
	
	public String getConnectingNodes()
	{
		return connectingNodes;
	}
	
	public String getActiveNodes()
	{
		return activeNodes;
	}
	
	public String getMaxActiveSims()
	{
		return maxActiveSims;
	}
	
	public String getAddedSims()
	{
		return AddedSims;
	}
	
}
