package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager;

import jCompute.Debug.DebugLogger;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Stats.StatExporter.ExportFormat;

import java.io.DataInputStream;
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
	public SimulationStatsRequest(DataInputStream source) throws IOException
	{		
		simId = source.readInt();
		format = ExportFormat.fromInt(source.readInt());
	}

	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(12);
	
		tbuffer.putInt(NSMCP.SimStatsReq);
		
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

