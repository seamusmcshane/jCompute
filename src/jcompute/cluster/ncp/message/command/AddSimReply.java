package jcompute.cluster.ncp.message.command;

import java.io.IOException;
import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.message.NCPMessage;
import jcompute.simulationmanager.returnables.AddSimStatus;

public class AddSimReply extends NCPMessage
{
	private long requestId;
	private AddSimStatus addSimStatus;
	
	public AddSimReply(long requestId, AddSimStatus addSimStatus)
	{
		this.requestId = requestId;
		this.addSimStatus = addSimStatus;
	}
	
	// Construct from an input stream
	public AddSimReply(ByteBuffer source) throws IOException
	{
		requestId = source.getLong();
		
		int simId = source.getInt();
		
		boolean needData = (source.getInt() == 0) ? true : false;
		
		int numfileNames = source.getInt();
		
		String[] filenames = new String[numfileNames];
		
		for(int i = 0; i < numfileNames; i++)
		{
			int len = source.getInt();
			
			byte[] dst = new byte[len];
			
			source.get(dst, 0, len);
			
			filenames[i] = new String(dst);
		}
		
		addSimStatus = new AddSimStatus(simId, needData, filenames);
	}
	
	public long getRequestId()
	{
		return requestId;
	}
	
	public AddSimStatus getAddSimStatus()
	{
		return addSimStatus;
	}
	
	@Override
	public int getType()
	{
		return NCP.AddSimReply;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 0;
		
		// requestId
		dataLen += 8;
		
		// simId
		dataLen += 4;
		int simId = addSimStatus.simId;
		
		// needData
		dataLen += 4;
		int ineedData = (addSimStatus.needData) ? 0 : 1;
		
		// numfileNames
		dataLen += 4;
		
		int numfileNames = addSimStatus.fileNames.length;
		String[] filenames = addSimStatus.fileNames;
		
		for(int f = 0; f < numfileNames; f++)
		{
			// Len Field
			dataLen += 4;
			
			// Data Len
			dataLen += filenames[f].getBytes().length;
		}
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.AddSimReply);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putLong(requestId);
		tbuffer.putInt(simId);
		tbuffer.putInt(ineedData);
		tbuffer.putInt(numfileNames);
		
		for(int f = 0; f < numfileNames; f++)
		{
			byte[] bytes = filenames[f].getBytes();
			
			tbuffer.putInt(bytes.length);
			tbuffer.put(bytes);
		}
		
		return tbuffer.array();
	}
}
