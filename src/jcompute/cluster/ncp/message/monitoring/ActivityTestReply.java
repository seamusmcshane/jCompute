package jcompute.cluster.ncp.message.monitoring;

import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.message.NCPMessage;

public class ActivityTestReply extends NCPMessage
{
	private int sequenceNum;
	private long sentTime;
	private long receiveTime;
	
	public ActivityTestReply(ActivityTestRequest req, long receiveTime)
	{
		this.sequenceNum = req.getSequenceNum();
		this.sentTime = req.getSentTime();
		this.receiveTime = receiveTime;
	}
	
	public ActivityTestReply(ByteBuffer source)
	{
		sequenceNum = source.getInt();
		sentTime = source.getLong();
		receiveTime = source.getLong();
	}
	
	public int getSequenceNum()
	{
		return sequenceNum;
	}
	
	public long getSentTime()
	{
		return sentTime;
	}
	
	public long getReceiveTime()
	{
		return receiveTime;
	}
	
	@Override
	public int getType()
	{
		return NCP.ActivityTestReply;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 28;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.ActivityTestReply);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(sequenceNum);
		tbuffer.putLong(sentTime);
		tbuffer.putLong(receiveTime);
		
		return tbuffer.array();
	}
}
