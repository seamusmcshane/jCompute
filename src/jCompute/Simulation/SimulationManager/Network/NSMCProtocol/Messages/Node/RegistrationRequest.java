package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.nio.ByteBuffer;

public class RegistrationRequest
{
	
	// Standard Constructor
	public RegistrationRequest()
	{
		
	}
	
	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(4);  
		
		tbuffer.putInt(NSMCP.RegReq);
		
		return tbuffer.array();
	}
}
