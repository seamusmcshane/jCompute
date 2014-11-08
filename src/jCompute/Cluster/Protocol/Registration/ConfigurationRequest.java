package jCompute.Cluster.Protocol.Registration;

import jCompute.Cluster.Protocol.NCP;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ConfigurationRequest
{
	private int bench;
	private int objects;
	private int iterations;
	private int warmup;
	private int runs;

	public ConfigurationRequest(int bench, int objects, int iterations, int warmup, int runs)
	{
		this.bench = bench;
		this.objects = objects;
		this.iterations = iterations;
		this.warmup = warmup;
		this.runs = runs;
	}

	// Construct from an input stream
	public ConfigurationRequest(ByteBuffer source) throws IOException
	{
		bench = source.getInt();

		if(bench > 0)
		{
			objects = source.getInt();
			iterations = source.getInt();
			warmup = source.getInt();
			runs = source.getInt();
		}
	}

	public int getBench()
	{
		return bench;
	}

	public int getObjects()
	{
		return objects;
	}

	public int getIterations()
	{
		return iterations;
	}

	public int getWarmup()
	{
		return warmup;
	}

	public int getRuns()
	{
		return runs;
	}

	public byte[] toBytes()
	{
		int dataLen;

		if(bench > 0)
		{
			// All Fields + bench
			dataLen = 5 * 4;
		}
		else
		{
			// Just Bench
			dataLen = 4;
		}

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);

		// Header
		tbuffer.putInt(NCP.ConfReq);
		tbuffer.putInt(dataLen);

		// Data
		tbuffer.putInt(bench);

		if(bench > 0)
		{
			tbuffer.putInt(objects);
			tbuffer.putInt(iterations);
			tbuffer.putInt(warmup);
			tbuffer.putInt(runs);
		}

		return tbuffer.array();
	}
}
