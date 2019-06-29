package jcompute.ncp.message.registration;

import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;

public class ConfigurationRequest extends NCPMessage
{
	private int benchmark;
	private int objects;
	private int iterations;
	private int warmupIterations;
	private int runs;
	
	public ConfigurationRequest(int benchmark, int objects, int iterations, int warmupIterations, int runs)
	{
		this.benchmark = benchmark;
		this.objects = objects;
		this.iterations = iterations;
		this.warmupIterations = warmupIterations;
		this.runs = runs;
	}
	
	// Construct from an input stream
	public ConfigurationRequest(ByteBuffer source)
	{
		benchmark = source.getInt();
		
		if(benchmark > 0)
		{
			objects = source.getInt();
			iterations = source.getInt();
			warmupIterations = source.getInt();
			runs = source.getInt();
		}
	}
	
	public int getBench()
	{
		return benchmark;
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
		return warmupIterations;
	}
	
	public int getRuns()
	{
		return runs;
	}
	
	@Override
	public int getType()
	{
		return NCPDefinitions.ConfReq;
	}
	
	@Override
	public byte[] toBytes()
	{
		int dataLen;
		
		if(benchmark > 0)
		{
			// All Fields + bench
			dataLen = 5 * 4;
		}
		else
		{
			// Just Bench
			dataLen = 4;
		}
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCPDefinitions.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCPDefinitions.ConfReq);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(benchmark);
		
		if(benchmark > 0)
		{
			tbuffer.putInt(objects);
			tbuffer.putInt(iterations);
			tbuffer.putInt(warmupIterations);
			tbuffer.putInt(runs);
		}
		
		return tbuffer.array();
	}
}
