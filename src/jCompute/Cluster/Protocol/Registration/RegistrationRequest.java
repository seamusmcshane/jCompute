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
		int dataLen = 0;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);
		
		// Header Only
		tbuffer.putInt(NCP.RegReq);
		tbuffer.putInt(dataLen);

		return tbuffer.array();
	}
}
