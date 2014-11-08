package jCompute.Cluster.Protocol.Registration;

import java.io.IOException;
import java.nio.ByteBuffer;

import jCompute.Cluster.Protocol.NCP;

public class RegistrationReqAck
{
	private int uid;
	// Standard Constructor
	public RegistrationReqAck(int uid)
	{
		this.uid = uid;
	}
	
	// Construct from an input stream
	public RegistrationReqAck(ByteBuffer source) throws IOException
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