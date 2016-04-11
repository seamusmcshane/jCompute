package jcompute.cluster.ncp.command;

import java.io.IOException;
import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;

public class RemoveSimAck
{
	private int simId;
	
	// Standard Constructor
	public RemoveSimAck(int simId)
	{
		this.simId = simId;
	}
	
	// Construct from an input stream
	public RemoveSimAck(ByteBuffer source) throws IOException
	{		
		simId = source.getInt();	
	}
	
	public int getSimId()
	{
		return simId;
	}
	
	public byte[] toBytes()
	{
		int dataLen = 4;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);  
		
		// Header
		tbuffer.putInt(NCP.RemSimAck);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
}
