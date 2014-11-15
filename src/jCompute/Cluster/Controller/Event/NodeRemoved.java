package jCompute.Cluster.Controller.Event;

import jCompute.Cluster.Node.NodeInfo;

public class NodeRemoved
{
	private NodeInfo nodeConfig;

	public NodeRemoved(NodeInfo nodeConfig)
	{
		this.nodeConfig = nodeConfig;
	}

	public NodeInfo getNodeConfiguration()
	{
		return nodeConfig;
	}

}
