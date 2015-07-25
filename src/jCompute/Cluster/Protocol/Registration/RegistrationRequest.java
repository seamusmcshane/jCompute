package jCompute.Cluster.Protocol.Registration;

import jCompute.Cluster.Protocol.NCP;

import java.nio.ByteBuffer;

public class RegistrationRequest
{
	
	// Standard Constructor
	public RegistrationRequest()
	{
		
	}
	
	public byte[] toBytes()
	{
		int dataLen = 4;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header Only
		tbuffer.putInt(NCP.RegReq);
		tbuffer.putInt(dataLen);
		tbuffer.putInt(NCP.NCP_PROTOCOL_VERSION);
		
		return tbuffer.array();
	}
}
