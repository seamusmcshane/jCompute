package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Debug.DebugLogger;
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
		DebugLogger.output("Add Sim Reply");
		
		simId = source.readInt();
		
		DebugLogger.output("SimId : " + simId);

	}
	
	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(12);  
		
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
