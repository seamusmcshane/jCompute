package jCompute.Cluster.Controller.Event;

import jCompute.Cluster.Node.NodeConfiguration;

public class NodeRemoved
{
	private NodeConfiguration nodeConfig;

	public NodeRemoved(NodeConfiguration nodeConfig)
	{
		this.nodeConfig = nodeConfig;
	}

	public NodeConfiguration getNodeConfiguration()
	{
		return nodeConfig;
	}

}
