package jcompute.ncp.message.monitoring;

import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;

public class ActivityTestReply extends NCPMessage
{
	private int sequenceNum;
	private long sentTime;
	
	public ActivityTestReply(ActivityTestRequest req)
	{
		this.sequenceNum = req.getSequenceNum();
		this.sentTime = req.getSentTime();
	}
	
	public ActivityTestReply(ByteBuffer source)
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
		return NCPDefinitions.ActivityTestReply;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 20;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCPDefinitions.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCPDefinitions.ActivityTestReply);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(sequenceNum);
		tbuffer.putLong(sentTime);
		
		return tbuffer.array();
	}
}
