package jCompute.Cluster.Protocol.Command;

import jCompute.Cluster.Protocol.NCP;

import java.nio.ByteBuffer;

public class StartSimCMD
{
	private int simId;
	
	// Standard Constructor
	public StartSimCMD(int simId)
	{
		this.simId = simId;
	}
	
	// Construct from an input stream
	public StartSimCMD(ByteBuffer source)
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
		tbuffer.putInt(NCP.StartSimCMD);
		tbuffer.putInt(dataLen);

		// Data
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
}
