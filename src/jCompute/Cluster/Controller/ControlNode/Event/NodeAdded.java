package jCompute.Cluster.Controller.ControlNode.Event;

import jCompute.Cluster.Node.NodeDetails.NodeInfo;

public class NodeAdded
{
	private NodeInfo nodeConfig;
	
	public NodeAdded(NodeInfo nodeConfig)
	{
		this.nodeConfig = nodeConfig;
	}
	
	public NodeInfo getNodeConfiguration()
	{
		return nodeConfig;
	}
	
}
