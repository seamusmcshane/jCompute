package jcompute.cluster.ncp.message.command;

import java.nio.ByteBuffer;

import jcompute.cluster.controlnode.mapping.RemoteSimulationMapping;
import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.message.NCPMessage;
import jcompute.stats.StatExporter.ExportFormat;

public class SimulationStatsRequest extends NCPMessage
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
	
	public int getSimId()
	{
		return simId;
	}
	
	public ExportFormat getFormat()
	{
		return format;
	}
	
	@Override
	public int getType()
	{
		return NCP.SimStatsReq;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 8;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.SimStatsReq);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(simId);
		tbuffer.putInt(format.ordinal());
		
		return tbuffer.array();
	}
}
