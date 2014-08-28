package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

import java.io.DataInputStream;
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
	public AddSimReq(DataInputStream source) throws IOException
	{		
		initialStepRate = source.readInt();
		int len = source.readInt();		
		byte[] bytes= new byte[len];
		source.readFully(bytes, 0, len);
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
		int len = scenarioText.getBytes().length;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(len+12); 
		
		// AddSim Req
		tbuffer.putInt(NSMCP.AddSimReq);
		
		// Intial Step Rate
		tbuffer.putInt(initialStepRate);
		
		// Config follows (len is chars)
		tbuffer.putInt(len);
		
		tbuffer.put(scenarioText.getBytes());
		
		return tbuffer.array();
	}
	
}
