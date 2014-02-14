package alifeSim.Stats;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class SingleStat implements StatInf
{	
	// Identifier for the type of Information recorded 
	private String name;
	
	private final String type = "Single";
	
	private Color statColor;
	/* Raw Samples - count always == num of steps */
	private List<Double> allSamples;
		
	public SingleStat(String name)
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
	
	public void addSample(Double value)
	{		
		// Add the new sample 
		allSamples.add(value);
	}
	
	public void addSample(int value)
	{		
		// Add the new sample 
		allSamples.add((double)value);
	}
	
	public Double getLastSample()
	{
		return allSamples.get(allSamples.size()-1);		
	}
	
	public void resetStats()
	{		
		/* All Samples */
		allSamples = new LinkedList<Double>();	
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

	public List<Double> getHistory()
	{
		return allSamples;
	}
	
	public int getHistoryLength()
	{
		return allSamples.size();
	}
}
