package jcompute.cluster.ncp.command;

import java.io.IOException;
import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;

public class AddSimReply
{
	private long requestId;
	private int simId;
	
	public AddSimReply(long requestId, int simId)
	{
		this.requestId = requestId;
		this.simId = simId;
	}
	
	// Construct from an input stream
	public AddSimReply(ByteBuffer source) throws IOException
	{
		requestId = source.getLong();
		simId = source.getInt();
	}
	
	public byte[] toBytes()
	{
		int dataLen = 12;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.AddSimReply);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putLong(requestId);
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
	
	public long getRequestId()
	{
		return requestId;
	}
	
	public int getSimId()
	{
		return simId;
	}
	
}
