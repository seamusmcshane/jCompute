package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

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

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NSMCP.HEADER_SIZE);  
		
		// Header
		tbuffer.putInt(NSMCP.RemSimReq);
		tbuffer.putInt(dataLen);

		// Data
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
}
