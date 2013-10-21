package alifeSim.Stats;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Stats
{

	int numTypes = 0;
	
	String[] listNames;
	/* Raw Samples - count always == num of steps */
	List[] allSamples;
	
	/* Last 1000 samples */
	Deque[] last1000samples;
	
	/* Runnning Average of last 1000 samples */
	int[] runningAvg;	
	
	/* FILO of 1000avg sample averages */
	Deque[] avg1000;	
	
	int[] max;
	int[] min;
	int[] avg;

	public Stats(int numTypes)
	{
		
		this.numTypes = numTypes;
		
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
		
		allSamples[listNum].add(value);
		
		if(last1000samples[listNum].size() == 1000)
		{
			// Remove the last sample
			int temp = (int) last1000samples[listNum].removeLast();
			runningAvg[listNum] +=temp;	// This corrects the averages after 1000 samples and avoids looping over the sample window
			
			if(avg1000[listNum].size() == 1000)
			{
				// Just remove the last sample
				avg1000[listNum].removeLast();
			}
			
			// Add the new sample
			avg1000[listNum].add(value);

		}
		
		// Add the new sample
		last1000samples[listNum].add(value);
		
		// Update the Running Average
		runningAvg[listNum] +=value;
		
		
		
		return true;
		
	}
	
	public void resetStats()
	{
		/* Names of the Lists */
		listNames = new String[numTypes];
		for(int i=0;i<numTypes;i++)
		{
			listNames[i] = new String();
		}
		
		/* All Samples */
		allSamples = new ArrayList[numTypes];
		for(int i=0;i<numTypes;i++)
		{
			allSamples[i] = new ArrayList<Integer>();
		}
		
		/* Last 1000 samples (for drawing) */
		last1000samples = new Deque[numTypes];
		for(int i=0;i<numTypes;i++)
		{
			allSamples[i] = new LinkedList<Integer>();
		}
		
		/* Running Averages for each list */
		runningAvg = new int[numTypes];
		
		/* FILO 1000avg (for Drawing) */
		avg1000 = new Deque[numTypes];
		for(int i=0;i<numTypes;i++)
		{
			avg1000[i] = new LinkedList<Integer>();
		}
		
	}
	
	
}
