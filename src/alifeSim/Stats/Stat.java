package alifeSim.Stats;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class Stat
{	
	// Identifier for the type of Information recorded 
	private String name;
	private Color statColor;
	/* Raw Samples - count always == num of steps */
	private List<Integer> allSamples;
		
	public Stat(String name)
	{
		this.name = name;
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
	
	public void addSample(int value)
	{		
		// Add the new sample 
		allSamples.add(value);
	}
	
	public int getLastSample()
	{
		return allSamples.get(allSamples.size()-1);		
	}
	
	public void resetStats()
	{		
		/* All Samples */
		allSamples = new LinkedList<Integer>();
		
		/* Last 1000 samples (for drawing) */
		addSample(0);	
	
	}

	public void setColor(Color color)
	{
		this.statColor = color;
	}
	
	public Color getColor()
	{
		return statColor;
	}
}
