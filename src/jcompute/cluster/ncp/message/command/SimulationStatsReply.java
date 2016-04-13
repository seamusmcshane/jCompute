package jcompute.cluster.ncp.message.command;

import java.nio.ByteBuffer;

import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.message.NCPMessage;
import jcompute.stats.StatExporter;
import jcompute.stats.StatExporter.ExportFormat;

public class SimulationStatsReply extends NCPMessage
{
	private int simId;
	
	private StatExporter exporter;
	private ByteBuffer unprocessedExporter;
	
	public SimulationStatsReply(int simId, StatExporter exporter)
	{
		this.simId = simId;
		this.exporter = exporter;
	}
	
	// Construct from a ByteBuffer
	public SimulationStatsReply(ByteBuffer source)
	{
		this.simId = source.getInt();
		
		// Store the buffer as we do not know at this stage how to create the exporter
		unprocessedExporter = source;
	}
	
	public int getSimId()
	{
		return simId;
	}
	
	public StatExporter getStatExporter(ExportFormat format, String fileNameSuffix)
	{
		// Create the exporter
		exporter = new StatExporter(format, fileNameSuffix);
		exporter.populateFromByteBuffer(unprocessedExporter);
		
		return exporter;
	}
	
	@Override
	public int getType()
	{
		return NCP.SimStatsReply;
	}
	
	@Override
	public byte[] toBytes()
	{
		byte[] data = exporter.toBytes();
		
		int size = data.length;
		
		// simId
		size += 4;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(size + NCP.HEADER_SIZE);
		
		// NCP Header (type/len)
		tbuffer.putInt(NCP.SimStatsReply);
		tbuffer.putInt(size);
		
		// SimId
		tbuffer.putInt(simId);
		
		// Add Data
		tbuffer.put(data);
		
		return tbuffer.array();
	}
}
