package jCompute.Cluster.Controller.Event;

import jCompute.Cluster.Node.NodeInfo;

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
