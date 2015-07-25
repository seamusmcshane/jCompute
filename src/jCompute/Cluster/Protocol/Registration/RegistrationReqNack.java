package jCompute.Cluster.Protocol.Registration;

import java.nio.ByteBuffer;

import jCompute.Cluster.Protocol.NCP;

public class RegistrationReqNack
{
	private int reason;
	private int value;
	
	// Standard Constructor
	public RegistrationReqNack(int reason, int value)
	{
		this.reason = reason;
		this.value = value;
	}
	
	public byte[] toBytes()
	{
		int dataLen = 8;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);
		
		// Header Only
		tbuffer.putInt(NCP.RegNack);
		tbuffer.putInt(dataLen);
		tbuffer.putInt(reason);
		tbuffer.putInt(value);
		
		return tbuffer.array();
	}
}
