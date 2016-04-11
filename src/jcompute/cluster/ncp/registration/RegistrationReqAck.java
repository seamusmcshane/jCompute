package jcompute.cluster.ncp.registration;

import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;

public class RegistrationReqAck
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

	public byte[] toBytes()
	{
		int dataLen = 4;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.RegAck);
		tbuffer.putInt(dataLen);

		// Data - uid
		tbuffer.putInt(uid);
		
		return tbuffer.array();
	}
}
