package jcompute.cluster.controlnode.computenodemanager.event;

import jcompute.cluster.computenode.nodedetails.NodeStatsSample;

public class ComputeNodeStatsUpdate
{
	private int nodeId;
	private int sequenceNum;
	private NodeStatsSample stats;
	
	public ComputeNodeStatsUpdate(int nodeId, int sequenceNum, NodeStatsSample stats)
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
