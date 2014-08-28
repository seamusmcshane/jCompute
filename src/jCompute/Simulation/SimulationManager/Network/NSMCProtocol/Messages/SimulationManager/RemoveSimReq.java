package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.io.DataInputStream;
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
	public RemoveSimReq(DataInputStream source) throws IOException
	{		
		simId = source.readInt();		
	}
	
	public int getSimid()
	{
		return simId;
	}
	
	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(8);  
		
		tbuffer.putInt(NSMCP.RemSimReq);
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
}
