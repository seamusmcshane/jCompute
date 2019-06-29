package jcompute.ncp.message.control;

import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;

public class NodeOrderlyShutdownReply extends NCPMessage
{
	public NodeOrderlyShutdownReply()
	{
		
	}
	
	@Override
	public int getType()
	{
		return NCPDefinitions.NodeOrderlyShutdownReply;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 0;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCPDefinitions.HEADER_SIZE);
		
		// Header Only
		tbuffer.putInt(NCPDefinitions.NodeOrderlyShutdownReply);
		tbuffer.putInt(dataLen);
		
		return tbuffer.array();
	}
}
