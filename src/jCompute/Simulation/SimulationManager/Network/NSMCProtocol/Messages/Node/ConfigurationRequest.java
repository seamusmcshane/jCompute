package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.nio.ByteBuffer;

public class ConfigurationRequest
{
	
	// Standard Constructor
	public ConfigurationRequest()
	{
		
	}
	
	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(4);  
		
		tbuffer.putInt(NSMCP.ConfReq);
		
		return tbuffer.array();
	}
}
