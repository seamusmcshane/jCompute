package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationManager.Network.Node.NodeConfiguration;

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
	public ConfigurationAck(ByteBuffer source) throws IOException
	{		
		maxSims = source.getInt();
		weighting = source.getLong();
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
		int dataLen = 12;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NSMCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NSMCP.ConfAck);
		tbuffer.putInt(dataLen);

		// Data
		tbuffer.putInt(maxSims);
		tbuffer.putLong(weighting);
		
		return tbuffer.array();
	}
}
