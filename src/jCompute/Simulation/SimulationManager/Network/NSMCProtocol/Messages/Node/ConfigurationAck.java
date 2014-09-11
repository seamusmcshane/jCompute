package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationManager.Network.Node.NodeConfiguration;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ConfigurationAck
{
	int maxSims;
	long weighting;
	
	public ConfigurationAck(NodeConfiguration conf)
	{
		this.maxSims = conf.getMaxSims();
		this.weighting = conf.getWeighting();
	}
	
	// Construct from an input stream
	public ConfigurationAck(DataInputStream source) throws IOException
	{		
		maxSims = source.readInt();
		weighting = source.readLong();
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
	
	public long getWeighting()
	{
		return weighting;
	}
	
	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(16);  

		// Conf Ack
		tbuffer.putInt(NSMCP.ConfAck);
		
		// maxSims
		tbuffer.putInt(maxSims);
		
		tbuffer.putLong(weighting);
		
		return tbuffer.array();
	}
}
