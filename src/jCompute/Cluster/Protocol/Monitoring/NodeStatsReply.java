package jCompute.Cluster.Protocol.Monitoring;

import java.nio.ByteBuffer;

import jCompute.Cluster.Node.NodeStats;
import jCompute.Cluster.Protocol.NCP;

public class NodeStatsReply
{
	private int sequenceNum;
	private NodeStats stats;
	
	public NodeStatsReply(int sequenceNum, NodeStats stats)
	{
		this.sequenceNum = sequenceNum;
		this.stats = stats;
	}
	
	public NodeStatsReply(ByteBuffer source)
	{
		this.sequenceNum = source.getInt();
		
		stats = new NodeStats();
		
		stats.setCpuUsage(source.getInt());
		stats.setFreeMemory(source.getInt());
		stats.setSimulationsProcessed(source.getLong());
		stats.setSimulationsActive(source.getInt());
		stats.setStatisticsPendingFetch(source.getInt());		
	}
	
	public NodeStats getNodeStats()
	{
		return this.stats;
	}
	
	public int getSequenceNum()
	{
		return sequenceNum;
	}
	
	public byte[] toBytes()
	{
		int dataLen = 28;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);  
		
		// Header
		tbuffer.putInt(NCP.NodeStatsReply);
		tbuffer.putInt(dataLen);
		
		tbuffer.putInt(sequenceNum);
		
		tbuffer.putInt(stats.getCpuUsage());
		tbuffer.putInt(stats.getFreeMemory());
		tbuffer.putLong(stats.getSimulationsProcessed());
		tbuffer.putInt(stats.getSimulationsActive());
		tbuffer.putInt(stats.getStatisticsPendingFetch());
		
		return tbuffer.array();
	}
	
}
