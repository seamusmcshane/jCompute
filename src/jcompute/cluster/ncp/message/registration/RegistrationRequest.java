package jcompute.cluster.ncp.message.registration;

import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.message.NCPMessage;

public class RegistrationRequest extends NCPMessage
{
	// Standard Constructor
	public RegistrationRequest()
	{
	}
	
	@Override
	public int getType()
	{
		return NCP.RegReq;
	}
	
	@Override
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
