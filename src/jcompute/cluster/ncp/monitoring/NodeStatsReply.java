package jcompute.cluster.ncp.monitoring;

import java.nio.ByteBuffer;

import jcompute.cluster.computenode.nodedetails.NodeStatsSample;
import jcompute.cluster.ncp.NCP;

public class NodeStatsReply
{
	private int sequenceNum;
	private NodeStatsSample nodeStatsSample;
	
	public NodeStatsReply(int sequenceNum, NodeStatsSample nodeStatsSample)
	{
		this.sequenceNum = sequenceNum;
		this.nodeStatsSample = nodeStatsSample;
	}
	
	public NodeStatsReply(ByteBuffer source)
	{
		this.sequenceNum = source.getInt();
		
		nodeStatsSample = new NodeStatsSample();
		
		nodeStatsSample.setCpuUsage(source.getInt());
		nodeStatsSample.setSimulationsProcessed(source.getLong());
		nodeStatsSample.setSimulationsActive(source.getInt());
		nodeStatsSample.setStatisticsPendingFetch(source.getInt());
		nodeStatsSample.setJvmMemoryUsedPercentage(source.getInt());
		nodeStatsSample.setBytesTX(source.getLong());
		nodeStatsSample.setTXS(source.getLong());
		nodeStatsSample.setBytesRX(source.getLong());
		nodeStatsSample.setRXS(source.getLong());
	}
	
	public NodeStatsSample getNodeStats()
	{
		return this.nodeStatsSample;
	}
	
	public int getSequenceNum()
	{
		return sequenceNum;
	}
	
	public byte[] toBytes()
	{
		int dataLen = 60;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.NodeStatsReply);
		tbuffer.putInt(dataLen);
		
		tbuffer.putInt(sequenceNum);
		
		tbuffer.putInt(nodeStatsSample.getCpuUsage());
		tbuffer.putLong(nodeStatsSample.getSimulationsProcessed());
		tbuffer.putInt(nodeStatsSample.getSimulationsActive());
		tbuffer.putInt(nodeStatsSample.getStatisticsPendingFetch());
		tbuffer.putInt(nodeStatsSample.getJvmMemoryUsedPercentage());
		tbuffer.putLong(nodeStatsSample.getBytesTX());
		tbuffer.putLong(nodeStatsSample.getTXS());
		tbuffer.putLong(nodeStatsSample.getBytesRX());
		tbuffer.putLong(nodeStatsSample.getRXS());
		
		return tbuffer.array();
	}
	
}
