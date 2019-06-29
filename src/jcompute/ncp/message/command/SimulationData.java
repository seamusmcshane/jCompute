package jcompute.ncp.message.command;

import java.io.IOException;
import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;

public class SimulationData extends NCPMessage
{
	private int simId;
	private byte[][] fileData;
	
	public SimulationData(int simId, byte[][] fileData)
	{
		this.simId = simId;
		this.fileData = fileData;
	}
	
	public SimulationData(ByteBuffer source) throws IOException
	{
		simId = source.getInt();
		
		int numFiles = source.getInt();
		
		if(numFiles > 0)
		{
			fileData = new byte[numFiles][];
			
			for(int i = 0; i < numFiles; i++)
			{
				int len = source.getInt();
				
				byte[] dst = new byte[len];
				
				source.get(dst, 0, len);
				
				fileData[i] = dst;
			}
		}
	}
	
	public int getSimId()
	{
		return simId;
	}
	
	public byte[][] getFileData()
	{
		return fileData;
	}
	
	@Override
	public int getType()
	{
		return NCPDefinitions.SimData;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 0;
		
		// simId
		dataLen += 4;
		
		// numfiles
		dataLen += 4;
		
		// Check none null
		int numfiles = (fileData == null) ? 0 : fileData.length;
		
		for(int f = 0; f < numfiles; f++)
		{
			// Len Field
			dataLen += 4;
			
			// Data Len
			dataLen += fileData[f].length;
		}
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCPDefinitions.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCPDefinitions.SimData);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(simId);
		tbuffer.putInt(numfiles);
		
		for(int f = 0; f < numfiles; f++)
		{
			byte[] bytes = fileData[f];
			
			tbuffer.putInt(bytes.length);
			tbuffer.put(bytes);
		}
		
		return tbuffer.array();
	}
	
}
