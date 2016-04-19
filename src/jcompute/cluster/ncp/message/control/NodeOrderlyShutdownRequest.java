package jcompute.cluster.ncp.message.control;

import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.message.NCPMessage;

public class NodeOrderlyShutdownRequest extends NCPMessage
{
	public NodeOrderlyShutdownRequest()
	{
		
	}
	
	@Override
	public int getType()
	{
		return NCP.NodeOrderlyShutdownRequest;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 0;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header Only
		tbuffer.putInt(NCP.NodeOrderlyShutdownRequest);
		tbuffer.putInt(dataLen);
		
		return tbuffer.array();
	}
}