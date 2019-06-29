package jcompute.ncp.message.registration;

import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;

public class RegistrationReqNack extends NCPMessage
{
	private int reason;
	private int value;
	
	// Standard Constructor
	public RegistrationReqNack(int reason, int value)
	{
		this.reason = reason;
		this.value = value;
	}
	
	// Construct from an input stream
	public RegistrationReqNack(ByteBuffer source)
	{
		reason = source.getInt();
		value = source.getInt();
	}
	
	public int getReason()
	{
		return reason;
	}
	
	public int getValue()
	{
		return value;
	}
	
	@Override
	public int getType()
	{
		return NCPDefinitions.RegNack;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 8;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCPDefinitions.HEADER_SIZE);
		
		// Header Only
		tbuffer.putInt(NCPDefinitions.RegNack);
		tbuffer.putInt(dataLen);
		tbuffer.putInt(reason);
		tbuffer.putInt(value);
		
		return tbuffer.array();
	}
}
