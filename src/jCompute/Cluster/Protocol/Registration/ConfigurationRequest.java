package jCompute.Cluster.Protocol.Registration;

import jCompute.Cluster.Protocol.NCP;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ConfigurationRequest
{
	private int bench;
	
	public ConfigurationRequest(int bench)
	{		
		this.bench = bench;
	}
	
	// Construct from an input stream
	public ConfigurationRequest(ByteBuffer source) throws IOException
	{		
		bench = source.getInt();
	}
	
	public int getBench()
	{
		return bench;
	}
	
	public byte[] toBytes()
	{
		int dataLen = 4;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.ConfReq);
		tbuffer.putInt(dataLen);

		// Data
		tbuffer.putInt(bench);
		
		return tbuffer.array();
	}
}
