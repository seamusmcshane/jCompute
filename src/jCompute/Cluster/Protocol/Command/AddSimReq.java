package jCompute.Cluster.Protocol.Command;

import jCompute.Cluster.Protocol.NCP;
import java.nio.ByteBuffer;

public class AddSimReq
{
	private long requestId;
	private String scenarioText;
	
	// Standard Constructor
	public AddSimReq(long requestId, String scenarioText)
	{
		this.requestId = requestId;
		this.scenarioText = scenarioText;
	}
	
	// Construct from an input stream
	public AddSimReq(ByteBuffer source)
	{
		requestId = source.getLong();
		int configLen = source.getInt();
		byte[] bytes = new byte[configLen];
		source.get(bytes, 0, configLen);
		scenarioText = new String(bytes);
	}
	
	public String getScenarioText()
	{
		return scenarioText;
	}
	
	public long getRequestId()
	{
		return requestId;
	}
	
	public byte[] toBytes()
	{
		int configLen = scenarioText.getBytes().length;
		int dataLen = configLen + 12;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.AddSimReq);
		tbuffer.putInt(dataLen);
		
		// Data
		// Request Id
		tbuffer.putLong(requestId);
		
		// Config follows (len is chars)
		tbuffer.putInt(configLen);
		
		tbuffer.put(scenarioText.getBytes());
		
		return tbuffer.array();
	}
	
}
