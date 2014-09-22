package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AddSimReply
{	
	private int simId;
	
	public AddSimReply(int simId)
	{
		this.simId = simId;
	}
	
	// Construct from an input stream
	public AddSimReply(ByteBuffer source) throws IOException
	{
		simId = source.getInt();
	}
	
	public byte[] toBytes()
	{
		int dataLen = 4;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NSMCP.HEADER_SIZE);  
		
		// Header
		tbuffer.putInt(NSMCP.AddSimReply);		
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
	
	public int getSimId()
	{
		return simId;
	}
	
}
