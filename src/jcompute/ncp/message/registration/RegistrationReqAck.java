package jcompute.ncp.message.registration;

import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;

public class RegistrationReqAck extends NCPMessage
{
	private int uid;
	// Standard Constructor
	public RegistrationReqAck(int uid)
	{
		this.uid = uid;
	}
	
	// Construct from an input stream
	public RegistrationReqAck(ByteBuffer source)
	{		
		uid = source.getInt();
	}
	
	public int getUid()
	{
		return uid;
	}

	@Override
	public int getType()
	{
		return NCPDefinitions.RegAck;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 4;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCPDefinitions.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCPDefinitions.RegAck);
		tbuffer.putInt(dataLen);

		// Data - uid
		tbuffer.putInt(uid);
		
		return tbuffer.array();
	}
}
