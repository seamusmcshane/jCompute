package jCompute.Stats.Trace;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SingleStat
{	
	// Identifier for the type of Information recorded 
	private String name;
	
	private final String type = "Single";
	
	private Color statColor;
	
	/* Raw Samples */
	private List<StatSample> allSamples;
		
	// When used time in the sample == step number
	private int sampleCount = 0;
	
	/* Quick ref to last sample */
	private StatSample lastSample;
		
	public SingleStat(String name)
	{
		this.name = name;
		statColor = new Color(Color.HSBtoRGB(ThreadLocalRandom.current().nextFloat(),0.9f,1f));
		resetStats();
	}
	
	public void setStatName(String name)
	{		
		this.name = name;
	}
	
	public String getStatName()
	{
		return name;
	}
	
	// Record a step==time sync'd sample
	public void addSample(double sample)
	{
		// Increment before add!
		sampleCount++;
		
		// Record the quick ref to last sample
		lastSample = new StatSample(sampleCount,sample);
		
		// Add the sample 
		allSamples.add(lastSample);
	}
	
	// Record a sample with a specific time index
	public void addSample(double time, double sample)
	{		
		// Record the quick ref to last sample
		lastSample = new StatSample(time,sample);
		
		// Add the sample 
		allSamples.add(lastSample);
	}
	
	public StatSample getLastSample()
	{
		return lastSample;
	}
	
	public void resetStats()
	{		
		/* All Samples */
		allSamples = new LinkedList<StatSample>();	
	}

	public void setColor(Color color)
	{
		this.statColor = color;
	}
	
	public Color getColor()
	{
		return statColor;
	}

	public String getType()
	{
		return type;
	}

	public List<StatSample> getHistory()
	{
		return allSamples;
	}
	
	public int getHistoryLength()
	{
		return allSamples.size();
	}
}
