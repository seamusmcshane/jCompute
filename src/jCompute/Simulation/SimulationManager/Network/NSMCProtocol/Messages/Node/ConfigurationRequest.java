package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ConfigurationRequest
{
	private int bench;
	
	public ConfigurationRequest(int bench)
	{		
		this.bench = bench;
	}
	
	// Construct from an input stream
	public ConfigurationRequest(DataInputStream source) throws IOException
	{		
		bench = source.readInt();
	}
	
	public int getBench()
	{
		return bench;
	}
	
	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(8);  
		
		tbuffer.putInt(NSMCP.ConfReq);
		
		tbuffer.putInt(bench);
		
		return tbuffer.array();
	}
}
