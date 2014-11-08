package jCompute.Cluster.Protocol.Command;

import java.io.IOException;
import java.nio.ByteBuffer;

import jCompute.Cluster.Protocol.NCP;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;

public class SimulationStatsReply
{
	int simId;
	public StatExporter exporter;

	public SimulationStatsReply(int simId, StatExporter exporter)
	{
		this.simId = simId;
		this.exporter = exporter;
	}
	
	// Construct from a ByteBuffer
	public SimulationStatsReply(int simId, ByteBuffer source, ExportFormat format, String fileNameSuffix) throws IOException
	{		
		this.simId = simId;
		exporter = new StatExporter(format, fileNameSuffix);
		exporter.populateFromByteBuffer(source);
	}

	public byte[] toBytes() throws IOException
	{
		byte[] data = exporter.toBytes();
		
		int size = data.length;

		// simId
		size += 4;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(size+NCP.HEADER_SIZE);

		// NCP Header (type/len)
		tbuffer.putInt(NCP.SimStats);
		tbuffer.putInt(size);

		// SimId
		tbuffer.putInt(simId);

		// Add Data
		tbuffer.put(data);

		return tbuffer.array();
	}

	public StatExporter getStatExporter()
	{
		return exporter;
	}
	
}
