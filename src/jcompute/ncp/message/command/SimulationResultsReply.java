package jcompute.ncp.message.command;

import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;
import jcompute.results.export.ExportFormat;
import jcompute.results.export.Result;

public class SimulationResultsReply extends NCPMessage
{
	private int simId;
	
	private Result exporter;
	private ByteBuffer unprocessedExporter;
	
	public SimulationResultsReply(int simId, Result exporter)
	{
		this.simId = simId;
		this.exporter = exporter;
	}
	
	// Construct from a ByteBuffer
	public SimulationResultsReply(ByteBuffer source)
	{
		this.simId = source.getInt();
		
		// Store the buffer as we do not know at this stage how to create the exporter
		unprocessedExporter = source;
	}
	
	public int getSimId()
	{
		return simId;
	}
	
	public Result getStatExporter(ExportFormat format, String fileNameSuffix)
	{
		// Create the exporter
		exporter = new Result(format, fileNameSuffix);
		exporter.populateFromByteBuffer(unprocessedExporter);
		
		return exporter;
	}
	
	@Override
	public int getType()
	{
		return NCPDefinitions.SimResultsReply;
	}
	
	@Override
	public byte[] toBytes()
	{
		byte[] data = exporter.toBytes();
		
		int size = data.length;
		
		// simId
		size += 4;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(size + NCPDefinitions.HEADER_SIZE);
		
		// NCP Header (type/len)
		tbuffer.putInt(NCPDefinitions.SimResultsReply);
		tbuffer.putInt(size);
		
		// SimId
		tbuffer.putInt(simId);
		
		// Add Data
		tbuffer.put(data);
		
		return tbuffer.array();
	}
}
