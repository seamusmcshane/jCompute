package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Debug.DebugLogger;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.io.DataInputStream;
import java.io.IOException;
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
	public StartSimCMD(DataInputStream source) throws IOException
	{
		DebugLogger.output("StartSimCMD");
		
		simId = source.readInt();
		
		DebugLogger.output("SimId : " + simId);
	}
	
	public int getSimid()
	{
		return simId;
	}
	
	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(8);  
		
		tbuffer.putInt(NSMCP.StartSimCMD);
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
}
