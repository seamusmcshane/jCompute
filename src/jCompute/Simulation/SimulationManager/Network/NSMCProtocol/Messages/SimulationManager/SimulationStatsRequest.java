package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Stats.StatExporter.ExportFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SimulationStatsRequest
{
	private int simId;
	private ExportFormat format;
	
	public SimulationStatsRequest(int simId, ExportFormat format)
	{
		this.simId = simId;
		this.format = format;
	}
	
	// Construct from an input stream
	public SimulationStatsRequest(ByteBuffer source) throws IOException
	{		
		simId = source.getInt();
		format = ExportFormat.fromInt(source.getInt());
	}

	public byte[] toBytes()
	{
		int dataLen = 8;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NSMCP.HEADER_SIZE);  
		
		// Header
		tbuffer.putInt(NSMCP.SimStatsReq);
		tbuffer.putInt(dataLen);

		// Data
		tbuffer.putInt(simId);
		tbuffer.putInt(format.ordinal());
		
		return tbuffer.array();
	}
	
	public int getSimId()
	{
		return simId;
	}
	
	public ExportFormat getFormat()
	{
		return format;
	}
	
}

