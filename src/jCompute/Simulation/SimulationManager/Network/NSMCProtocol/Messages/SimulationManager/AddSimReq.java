package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Debug.DebugLogger;
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
		DebugLogger.output("Add Sim Req");
		
		initialStepRate = source.readInt();
		int len = source.readInt();		

		StringBuffer config = new StringBuffer();
		
		for(int c=0;c<len;c++)
		{
			config.append(source.readChar());
		}
		
		scenarioText = config.toString();
		
		DebugLogger.output("StepRate : " + initialStepRate);
		DebugLogger.output("ScenarioText : " + scenarioText);

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
		int slen = scenarioText.length();
		
		// Unicode 16 -2bytes chart
		ByteBuffer tbuffer = ByteBuffer.allocate((slen*2)+12); 
		
		System.out.println("TBuffer Len " + tbuffer.limit());
		System.out.println("scenarioText Len " + scenarioText.length());
		
		// AddSim Req
		tbuffer.putInt(NSMCP.AddSimReq);
		
		// Intial Step Rate
		tbuffer.putInt(initialStepRate);
		
		// Config follows (len is chars)
		tbuffer.putInt(slen);
		
		for(int c=0;c<slen;c++)
		{
			tbuffer.putChar(scenarioText.charAt(c));
		}
		
		System.out.println("slen " + slen);
		
		return tbuffer.array();
	}
	
}
