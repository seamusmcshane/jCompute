package jCompute.Cluster.Controller.Event;

import jCompute.Cluster.Node.NodeStats;

public class NodeStatsUpdate
{
	private int nodeId;
	private int sequenceNum;
	private NodeStats stats;

	public NodeStatsUpdate(int nodeId, int sequenceNum, NodeStats stats)
	{
		super();
		this.nodeId = nodeId;
		this.sequenceNum = sequenceNum;
		this.stats = stats;
	}

	public int getNodeId()
	{
		return nodeId;
	}

	public int getSequenceNum()
	{
		return sequenceNum;
	}

	public NodeStats getStats()
	{
		return stats;
	}

}
