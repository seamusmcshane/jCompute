package alifeSim.Stats;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Stats
{

	private int numTypes = 0;
	
	private String[] listNames;
	/* Raw Samples - count always == num of steps */
	private List[] allSamples;
	
	/* Last n samples */
	private Deque[] drawWindow;
	private int drawWindowLength = 1000;
	
	/* Runnning total for average of last 1000 samples */
	private int[] runningTotal;	
	
	/* FILO of 1000avg sample averages */
	private Deque[] avgWindow;
	private int avgWindowLenght = 1000;
	
	public Stats(int numTypes)
	{
		
		if(numTypes <=0)
		{
			numTypes = 0;
		}
		
		this.numTypes = numTypes;
		
		resetListNames();
		resetStats();
		
	}
	
	public boolean setListName(int listNum,String name)
	{
		
		if(numTypes == 0)
		{
			return false;
		}
		
		if( listNum >=numTypes )
		{
			return false;
		}
		
		if( listNum < 0 )
		{
			return false;
		}		
		
		/* Name the List */
		listNames[listNum] = name;
		
		return true;
	}
	
	public boolean addSample(int listNum,int value)
	{
		if(numTypes == 0)
		{
			return false;
		}
		
		if( listNum >=numTypes )
		{
			return false;
		}
		
		if( listNum < 0 )
		{
			return false;
		}	
		
		// Add the new sample 
		allSamples[listNum].add(value);
		
		// Add the new sample
		drawWindow[listNum].add(value);
		
		// Update the Running Average
		runningTotal[listNum] +=value;		
		
		if(drawWindow[listNum].size() == drawWindowLength)
		{
			// Remove the last sample
			int temp = (int) drawWindow[listNum].removeLast();
			runningTotal[listNum] -=temp;	 // This corrects the averages after 1000 samples and avoids looping over the sample window		
			
			if(avgWindow[listNum].size() == avgWindowLenght)
			{
				// Just remove the last 1000 sample average
				avgWindow[listNum].removeLast();
			}
			
			// Add the new 1000 sample average
			avgWindow[listNum].add( runningTotal[listNum] / 1000 );

		}
		

		
		
		
		return true;
		
	}
	
	public void resetListNames()
	{
		/* Names of the Lists */
		listNames = new String[numTypes];
		for(int i=0;i<numTypes;i++)
		{
			listNames[i] = new String();
		}
	}
	
	public void resetStats()
	{		
		/* All Samples */
		allSamples = new ArrayList[numTypes];
		for(int i=0;i<numTypes;i++)
		{
			allSamples[i] = new ArrayList<Integer>();
		}
		
		/* Last 1000 samples (for drawing) */
		drawWindow = new Deque[numTypes];
		for(int i=0;i<numTypes;i++)
		{
			allSamples[i] = new LinkedList<Integer>();
		}
		
		/* Running Total for each list */
		runningTotal = new int[numTypes];
		
		/* FILO 1000avg (for Drawing) */
		avgWindow = new Deque[numTypes];
		for(int i=0;i<numTypes;i++)
		{
			avgWindow[i] = new LinkedList<Integer>();
		}
		
	}
	
	public Deque getDrawWindow(int listNum)
	{
		if(numTypes == 0)
		{
			return null;
		}
		
		if( listNum >=numTypes )
		{
			return null;
		}
		
		if( listNum < 0 )
		{
			return null;
		}	
		
		return drawWindow[listNum];
		
	}
	
	public Deque getAvgWindow(int listNum)
	{
		if(numTypes == 0)
		{
			return null;
		}
		
		if( listNum >=numTypes )
		{
			return null;
		}
		
		if( listNum < 0 )
		{
			return null;
		}	
		
		return avgWindow[listNum];		
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
