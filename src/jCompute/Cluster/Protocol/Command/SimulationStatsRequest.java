package jCompute.Cluster.Protocol.Command;

import jCompute.Cluster.Controller.Mapping.RemoteSimulationMapping;
import jCompute.Cluster.Protocol.NCP;
import jCompute.Stats.StatExporter.ExportFormat;

import java.io.IOException;
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
	public SimulationStatsRequest(ByteBuffer source) throws IOException
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

