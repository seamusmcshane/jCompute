package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ConfigurationAck
{
	int maxSims;
	
	public ConfigurationAck(int maxSims)
	{
		this.maxSims = maxSims;
	}
	
	// Construct from an input stream
	public ConfigurationAck(DataInputStream source) throws IOException
	{		
		maxSims = source.readInt();
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
	
	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(8);  

		// Conf Ack
		tbuffer.putInt(NSMCP.ConfAck);
		
		// maxSims
		tbuffer.putInt(maxSims);
		
		return tbuffer.array();
	}
}
