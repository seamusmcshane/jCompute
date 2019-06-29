package jcompute.ncp.message.command;

import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;
import jcompute.results.export.ExportFormat;

public class SimulationResultsRequest extends NCPMessage
{
	private int simId;
	private ExportFormat format;
	
	public SimulationResultsRequest(int simId, ExportFormat format)
	{
		this.simId = simId;
		this.format = format;
	}
	
	// Construct from an input stream
	public SimulationResultsRequest(ByteBuffer source)
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
		return NCPDefinitions.SimResultsReq;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen = 8;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCPDefinitions.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCPDefinitions.SimResultsReq);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(simId);
		tbuffer.putInt(format.ordinal());
		
		return tbuffer.array();
	}
}
