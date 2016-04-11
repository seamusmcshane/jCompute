package jCompute.cluster.ncp.command;

import jCompute.Stats.StatExporter.ExportFormat;
import jCompute.cluster.controlnode.mapping.RemoteSimulationMapping;
import jCompute.cluster.ncp.NCP;

import java.nio.ByteBuffer;

public class SimulationStatsRequest
{
	private int simId;
	private ExportFormat format;
	
	public SimulationStatsRequest(RemoteSimulationMapping mapping)
	{
		this.simId = mapping.getRemoteSimId();
		this.format = mapping.getExportFormat();
	}
	
	// Construct from an input stream
	public SimulationStatsRequest(ByteBuffer source)
	{		
		simId = source.getInt();
		format = ExportFormat.fromInt(source.getInt());
	}

	public byte[] toBytes()
	{
		int dataLen = 8;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);  
		
		// Header
		tbuffer.putInt(NCP.SimStatsReq);
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

