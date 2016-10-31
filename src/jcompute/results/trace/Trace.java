package jcompute.results.trace;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jcompute.results.trace.samples.DoubleTraceSample;
import jcompute.results.trace.samples.IntegerTraceSample;
import jcompute.results.trace.samples.TraceSample;

public class Trace
{
	// Identifier for the type of Information recorded
	public final String name;
	
	public final TraceDataType type;
	
	public Color color;
	
	/* Raw Samples */
	private List<TraceSample> allSamples;
	
	// When used time in the sample == step number
	private int sampleCount = 0;
	
	/* Quick ref to last sample */
	private TraceSample lastSample;
	
	public Trace(String name, TraceDataType type)
	{
		this.name = name;
		this.type = type;
		
		color = new Color(Color.HSBtoRGB(ThreadLocalRandom.current().nextFloat(), 0.9f, 1f));
		resetStats();
	}
	
	public void setColor(Color color)
	{
		this.color = color;
	}
	
	/*
	 * ***************************************************************************************************
	 * Double
	 *****************************************************************************************************/
	
	// Record a step==time sync'd sample
	public void addSample(double sample)
	{
		if(type != TraceDataType.Decimal)
		{
			return;
		}
		
		// Increment before add!
		sampleCount++;
		
		// Record the quick ref to last sample
		lastSample = new DoubleTraceSample(sampleCount, sample);
		
		// Add the sample
		allSamples.add(lastSample);
	}
	
	// Record a sample with a specific time index
	public void addSample(double time, double sample)
	{
		// Record the quick ref to last sample
		lastSample = new DoubleTraceSample(time, sample);
		
		// Add the sample
		allSamples.add(lastSample);
	}
	
	/*
	 * ***************************************************************************************************
	 * Integer Recording
	 *****************************************************************************************************/
	
	// Record a step==time sync'd sample
	public void addSample(int sample)
	{
		if(type != TraceDataType.Integer)
		{
			return;
		}
		
		// Increment before add!
		sampleCount++;
		
		// Record the quick ref to last sample
		lastSample = new IntegerTraceSample(sampleCount, sample);
		
		// Add the sample
		allSamples.add(lastSample);
	}
	
	// Record a sample with a specific time index
	public void addSample(double time, int sample)
	{
		if(type != TraceDataType.Integer)
		{
			return;
		}
		
		// Record the quick ref to last sample
		lastSample = new IntegerTraceSample(time, sample);
		
		// Add the sample
		allSamples.add(lastSample);
	}
	
	public TraceSample getLastSample()
	{
		return lastSample;
	}
	
	public void resetStats()
	{
		/* All Samples */
		allSamples = new LinkedList<TraceSample>();
	}
	
	public List<TraceSample> getHistory()
	{
		return allSamples;
	}
	
	public TraceSample[] getHistoryAsArray()
	{
		TraceSample array[] = new TraceSample[allSamples.size()];
		
		array = allSamples.toArray(array);
		
		return array;
	}
	
	public int getHistoryLength()
	{
		return allSamples.size();
	}
	
	public enum TraceDataType
	{
		Integer, Decimal, String, Binary
	}
}
