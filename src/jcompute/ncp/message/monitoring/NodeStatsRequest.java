package jcompute.ncp.message.monitoring;

import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;

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
		return NCPDefinitions.NodeStatsRequest;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 4;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCPDefinitions.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCPDefinitions.NodeStatsRequest);
		tbuffer.putInt(dataLen);
		
		tbuffer.putInt(sequenceNum);
		
		return tbuffer.array();
	}
}
