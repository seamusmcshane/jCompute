package jCompute.Cluster.Protocol.Command;

import jCompute.Cluster.Protocol.NCP;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RemoveSimReq
{
	private int simId;
	
	// Standard Constructor
	public RemoveSimReq(int simId)
	{
		this.simId = simId;
	}
	
	// Construct from an input stream
	public RemoveSimReq(ByteBuffer source) throws IOException
	{		
		simId = source.getInt();
	}
	
	public int getSimid()
	{
		return simId;
	}
	
	public byte[] toBytes()
	{
		int dataLen = 4;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);  
		
		// Header
		tbuffer.putInt(NCP.RemSimReq);
		tbuffer.putInt(dataLen);

		// Data
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
}
