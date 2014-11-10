package jCompute.Cluster.Controller.Event;

import jCompute.Cluster.Node.NodeConfiguration;

public class NodeAdded
{
	private NodeConfiguration nodeConfig;

	public NodeAdded(NodeConfiguration nodeConfig)
	{
		this.nodeConfig = nodeConfig;
	}

	public NodeConfiguration getNodeConfiguration()
	{
		return nodeConfig;
	}

}
