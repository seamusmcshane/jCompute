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
		int dataLen = 0;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NSMCP.HEADER_SIZE);
		
		// Header Only
		tbuffer.putInt(NSMCP.RegReq);
		tbuffer.putInt(dataLen);

		return tbuffer.array();
	}
}
