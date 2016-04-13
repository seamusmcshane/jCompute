package jcompute.cluster.ncp.message.monitoring;

import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.message.NCPMessage;

public class NodeStatsRequest extends NCPMessage
{
	private int sequenceNum;
	
	public NodeStatsRequest(int sequenceNum)
	{
		this.sequenceNum = sequenceNum;
	}
	
	public NodeStatsRequest(ByteBuffer source)
	{
		this.sequenceNum = source.getInt();
	}
	
	public int getSequenceNum()
	{
		return sequenceNum;
	}
	
	@Override
	public int getType()
	{
		return NCP.NodeStatsRequest;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 4;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.NodeStatsRequest);
		tbuffer.putInt(dataLen);
		
		tbuffer.putInt(sequenceNum);
		
		return tbuffer.array();
	}
}
