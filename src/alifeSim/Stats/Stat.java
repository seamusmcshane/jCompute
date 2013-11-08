package alifeSim.Stats;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Stat
{	
	// Identifier for the type of Information recorded 
	private String name;
	
	/* Raw Samples - count always == num of steps */
	private List<Integer> allSamples;
	
	/* Last n samples */
	private Deque<Integer> drawWindow;
	private int drawWindowLength = 1000;
	
	/* Running total for average of last 1000 samples */
	private int runningTotal;	
	
	/* FILO of 1000avg sample averages */
	private Deque<Integer> avgWindow;
	private int avgWindowLenght = 1000;
	
	public Stat()
	{		
		resetStats();	
	}
	
	public Stat(String name)
	{
		this.name = name;
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
	
	public void addSample(int listNum,int value)
	{		
		// Add the new sample 
		allSamples.add(value);
		
		// Add the new sample
		drawWindow.add(value);
		
		// Update the Running Average
		runningTotal +=value;		
		
		if(drawWindow.size() == drawWindowLength)
		{
			// Remove the last sample
			int temp = (int) drawWindow.removeLast();
			runningTotal -=temp;	 // This corrects the averages after 1000 samples and avoids looping over the sample window		
			
			if(avgWindow.size() == avgWindowLenght)
			{
				// Just remove the last 1000 sample average
				avgWindow.removeLast();
			}
			
			// Add the new 1000 sample average
			avgWindow.add( runningTotal / 1000 );

		}
		
	}
		
	public void resetStats()
	{		
		/* All Samples */
		allSamples = new ArrayList<Integer>();
		
		/* Last 1000 samples (for drawing) */
		drawWindow = new LinkedList<Integer>();
		
		/* Running Total for each list */
		runningTotal = 0;
		
		/* FILO 1000avg (for Drawing) */
		avgWindow = new LinkedList<Integer>();		
	}
	
	public Deque<Integer> getDrawWindow()
	{	
		return drawWindow;	
	}
	
	public Deque<Integer> getAvgWindow()
	{
		return avgWindow;		
	}
	
	public int DrawWindowMax()
	{
		return drawWindowLength;
	}
	
	public int AvgWindowMax()
	{
		return avgWindowLenght;
	}

}
