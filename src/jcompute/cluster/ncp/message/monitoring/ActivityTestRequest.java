package jcompute.cluster.ncp.message.monitoring;

import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.message.NCPMessage;

public class ActivityTestRequest extends NCPMessage
{
	private int sequenceNum;
	private long sentTime;
	
	public ActivityTestRequest(int sequenceNum)
	{
		this.sequenceNum = sequenceNum;
		this.sentTime = System.nanoTime();
	}
	
	public ActivityTestRequest(ByteBuffer source)
	{
		sequenceNum = source.getInt();
		sentTime = source.getLong();
	}
	
	public int getSequenceNum()
	{
		return sequenceNum;
	}
	
	public long getSentTime()
	{
		return sentTime;
	}
	
	@Override
	public int getType()
	{
		return NCP.ActivityTestRequest;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 20;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.ActivityTestRequest);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(sequenceNum);
		tbuffer.putLong(sentTime);
		
		return tbuffer.array();
	}
}
