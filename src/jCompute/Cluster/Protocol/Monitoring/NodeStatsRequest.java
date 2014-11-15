package jCompute.Cluster.Protocol.Monitoring;

import jCompute.Cluster.Protocol.NCP;

import java.nio.ByteBuffer;

public class NodeStatsRequest
{
	private int sequenceNum;
	public NodeStatsRequest(int sequenceNum)
	{
		this.sequenceNum = sequenceNum;
	}
	
	public byte[] toBytes()
	{
		int dataLen = 4;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);  
		
		// Header
		tbuffer.putInt(NCP.NodeStatsRequest);
		tbuffer.putInt(dataLen);
		
		tbuffer.putInt(sequenceNum);
		
		return tbuffer.array();
	}
}
