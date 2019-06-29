package jcompute.ncp.message.registration;

import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;

public class RegistrationRequest extends NCPMessage
{
	private final int protocolVersion;
	
	// Standard Constructor
	public RegistrationRequest()
	{
		protocolVersion = NCPDefinitions.NCP_PROTOCOL_VERSION;
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
		return NCPDefinitions.RegReq;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 4;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCPDefinitions.HEADER_SIZE);
		
		// Header Only
		tbuffer.putInt(NCPDefinitions.RegReq);
		tbuffer.putInt(dataLen);
		tbuffer.putInt(protocolVersion);
		
		return tbuffer.array();
	}
}
