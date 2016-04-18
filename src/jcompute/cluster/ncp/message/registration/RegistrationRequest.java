package jcompute.cluster.ncp.message.registration;

import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.message.NCPMessage;

public class RegistrationRequest extends NCPMessage
{
	private final int protocolVersion;
	
	// Standard Constructor
	public RegistrationRequest()
	{
		protocolVersion = NCP.NCP_PROTOCOL_VERSION;
	}
	
	public RegistrationRequest(ByteBuffer data)
	{
		protocolVersion = data.getInt();
	}
	
	public int getProtocolVersion()
	{
		return protocolVersion;
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
		tbuffer.putInt(protocolVersion);
		
		return tbuffer.array();
	}
}
