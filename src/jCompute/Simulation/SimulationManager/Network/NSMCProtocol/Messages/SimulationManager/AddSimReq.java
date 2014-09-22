package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AddSimReq
{
	String scenarioText;
	int initialStepRate;
	
	// Standard Constructor
	public AddSimReq(String scenarioText, int initialStepRate)
	{
		this.scenarioText = scenarioText;
		this.initialStepRate = initialStepRate;
	}

	// Construct from an input stream
	public AddSimReq(ByteBuffer source) throws IOException
	{		
		initialStepRate = source.getInt();
		int len = source.getInt();		
		byte[] bytes= new byte[len];
		source.get(bytes, 0, len);
		scenarioText = new String(bytes);
	}
	
	public String getScenarioText()
	{
		return scenarioText;
	}

	public int getInitialStepRate()
	{
		return initialStepRate;
	}
	
	public byte[] toBytes()
	{
		int configLen = scenarioText.getBytes().length;
		int dataLen = configLen + 8;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NSMCP.HEADER_SIZE);  
		
		// Header
		tbuffer.putInt(NSMCP.AddSimReq);
		tbuffer.putInt(dataLen);

		// Data		
		// Intial Step Rate
		tbuffer.putInt(initialStepRate);		
		// Config follows (len is chars)
		tbuffer.putInt(configLen);
		
		tbuffer.put(scenarioText.getBytes());
		
		return tbuffer.array();
	}
	
}
