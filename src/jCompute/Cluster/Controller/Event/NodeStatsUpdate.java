package jCompute.Cluster.Controller.Event;

import jCompute.Cluster.Node.NodeDetails.NodeStatsSample;

public class NodeStatsUpdate
{
	private int nodeId;
	private int sequenceNum;
	private NodeStatsSample stats;

	public NodeStatsUpdate(int nodeId, int sequenceNum, NodeStatsSample stats)
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

	public NodeStatsSample getStats()
	{
		return stats;
	}

}
