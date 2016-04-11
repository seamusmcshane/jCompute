package jcompute.cluster.ncp.control;

import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;

public class NodeOrderlyShutdown
{
	public NodeOrderlyShutdown()
	{
		
	}
	
	public byte[] toBytes()
	{
		int dataLen = 0;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header Only
		tbuffer.putInt(NCP.NodeOrderlyShutdown);
		tbuffer.putInt(dataLen);
		
		return tbuffer.array();
	}
}
