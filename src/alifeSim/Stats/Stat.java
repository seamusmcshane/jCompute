package alifeSim.Stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Stat
{	
	// Identifier for the type of Information recorded 
	private String name;
	
	/* Raw Samples - count always == num of steps */
	private List<Integer> allSamples;
	
	/* Last n samples */
	private List<Number> drawWindow;
	private int drawWindowLength = 1000;
	private Semaphore drawLock = new Semaphore(1);
	
	/* Running total for average of last 5000 samples */
	private int runningTotal;	
	
	/* FILO of 1000avg sample averages */
	private List<Number> avgWindow;
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
	
	public void addSample(int value)
	{		
		drawLock.acquireUninterruptibly();
		// Add the new sample 
		allSamples.add(value);
		
		// Add the new sample
		drawWindow.add(value);
		
		// Update the Running Average
		runningTotal +=value;		
		
		if(drawWindow.size() == drawWindowLength)
		{
			// Remove the last sample
			Number temp = drawWindow.remove(0);
			runningTotal -=temp.intValue();	 // This corrects the averages after avgWindowLenght samples
			
			if(avgWindow.size() == avgWindowLenght)
			{
				// Just remove the last avgWindowLenght sample average
				avgWindow.remove(0);
			}
			
			// Add the new 1000 sample average
			avgWindow.add( runningTotal / avgWindowLenght );

		}
		drawLock.release();
		
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
		drawWindow = new ArrayList<Number>();
		addSample(0);
		
		/* Running Total for each list */
		runningTotal = 0;
		
		/* FILO 1000avg (for Drawing) */
		avgWindow = new ArrayList<Number>();		
	}
	
	public int sampleCount()
	{
		return allSamples.size();
	}
	
	public Collection<Number> getDrawWindowCollection()
	{	
		return drawWindow;	
	}
	
	public List<Number> getDrawWindow()
	{	
		List drawWindowCopy = new ArrayList<Number>(drawWindow);
		
		Collections.reverse(drawWindowCopy);
		
		return drawWindowCopy;	
	}	
	
	
	public List<Number> getAvgWindow()
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
