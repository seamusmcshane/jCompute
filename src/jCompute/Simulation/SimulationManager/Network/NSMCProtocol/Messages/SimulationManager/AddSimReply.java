package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.io.DataInputStream;
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
	public AddSimReply(DataInputStream source) throws IOException
	{		
		simId = source.readInt();
	}
	
	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(8);  
		
		// Reg Req
		tbuffer.putInt(NSMCP.AddSimReply);
		
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
	
	public int getSimId()
	{
		return simId;
	}
	
}
