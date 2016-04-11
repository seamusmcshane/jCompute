package jcompute.stats.groups;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import jcompute.stats.trace.ImageStat;

public class ImageStatGroup
{
	private String name;
	
	private ArrayList<ImageStat> statList;
	
	// Group lock
	private Semaphore statsGroupLock = new Semaphore(1);
	
	public ImageStatGroup(String name)
	{
		this.name = name;
		
		statList = new ArrayList<ImageStat>();

	}

	public String getName()
	{
		return name;
	}

	// Add a new stat to the stat manager
	public void registerStat(ImageStat stat)
	{
		statsGroupLock.acquireUninterruptibly();		
			statList.add(stat);
		statsGroupLock.release();
	}
	
	// Add a list of stats to the stat manager
	public void registerStats(List<ImageStat> statList)
	{
		for (ImageStat stat : statList) 
		{
			registerStat(stat);
		}
	}
	
	// returns a stat based on the stat name requested
	public ImageStat getStat(String statName)
	{
		statsGroupLock.acquireUninterruptibly();
		
			Iterator<ImageStat> listItr = statList.iterator();
		
			ImageStat stat = null;
			
			while(listItr.hasNext())
			{
				ImageStat tempStat =  listItr.next();
				if( tempStat.getName().equals(statName))
				{
					stat = tempStat;
					
					// Found Sample Exit loop
					break;
				}
			}
					
		statsGroupLock.release();
		
		return stat;
	}
	
	// An TODO sorted list of the Group names in the manager
	public List<String> getStatList()
	{
		List<String> list = new ArrayList<String>();
		
		Iterator<ImageStat> listItr = statList.iterator();
		
		while(listItr.hasNext())
		{
			list.add(listItr.next().getName());
		}		
		
		// Collections.sort(list,new sortComparator());
		
		return list;
	}
	
}
