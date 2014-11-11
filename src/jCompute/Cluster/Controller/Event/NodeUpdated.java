package jCompute.Cluster.Controller.Event;

import jCompute.Cluster.Node.NodeConfiguration;

public class NodeUpdated
{
	private NodeConfiguration nodeConfig;

	public NodeUpdated(NodeConfiguration nodeConfig)
	{
		this.nodeConfig = nodeConfig;
	}

	public NodeConfiguration getNodeConfiguration()
	{
		return nodeConfig;
	}

}
