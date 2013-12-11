package alifeSim.Stats;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class BucketStat implements StatInf
{
	// Identifier for the type of Information recorded 
	private String name;
	
	private final String type = "Bucket";
	
	private Color statColor;
	
	/* Raw Samples - count always == num of steps */
	private List<int[]> allSamples;
	private int numBuckets;
	
	/**
	 * A collection that holds several stats in a bucket arrangement
	 * @param name
	 * @param numBuckets
	 */
	public BucketStat(String name, int numBuckets)
	{
		this.name = name;
		
		this.numBuckets = numBuckets;
		
		statColor = new Color(Color.HSBtoRGB((float)Math.random(),0.9f,1f));
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
	
	public void addSample(int[] value)
	{		
		// Add the new sample 
		allSamples.add(value);
	}
	
	public int[] getLastSample()
	{
		return allSamples.get(allSamples.size()-1);		
	}
	
	public void resetStats()
	{		
		/* All Samples */
		allSamples = new LinkedList<int[]>();
		
		/* Last 1000 samples (for drawing) */
		addSample(new int[numBuckets]);	
	
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

}
