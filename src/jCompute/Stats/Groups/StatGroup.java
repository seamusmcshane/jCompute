package jCompute.Stats.Groups;

import jCompute.Stats.StatGroupSetting;
import jCompute.Stats.Trace.SingleStat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class StatGroup
{
	// Group Name
	private String groupName;
	
	// Stat List in this group - we do not check for duplicates
	private ArrayList<SingleStat> statList;
	
	// Group lock
	private Semaphore statsGroupLock = new Semaphore(1);
	
	// Group Settings
	private StatGroupSetting setting;
	private int notifiyCalls = 0;
	
	// The Listeners for changes in this stat groups stats
	private List<StatGroupListenerInf> statGroupListeners = new ArrayList<StatGroupListenerInf>();
	
	// Lock for the listeners
	private Semaphore listenersLock = new Semaphore(1, false);
	
	public StatGroup(String groupName)
	{
		this.groupName = groupName;
		
		setting = new StatGroupSetting(groupName);
		
		statList = new ArrayList<SingleStat>();
	}
	
	// Add a new stat to the stat manager
	private void registerStat(SingleStat stat)
	{
			statList.add(stat);
	}
	
	// Add a list of stats to the stat manager
	public void registerStats(List<SingleStat> statList)
	{
		statsGroupLock.acquireUninterruptibly();	
		
		for (SingleStat stat : statList) 
		{
			registerStat(stat);
		}
		
		statsGroupLock.release();
	}
	
	// returns a stat based on the stat name requested
	public SingleStat getStat(String statName)
	{
		statsGroupLock.acquireUninterruptibly();
		
			Iterator<SingleStat> listItr = statList.iterator();
		
			SingleStat stat = null;
			
			while(listItr.hasNext())
			{
				SingleStat tempStat =  listItr.next();
				if( tempStat.getStatName().equals(statName))
				{
					stat = tempStat;
					
					// Found Sample Exit loop
					break;
				}
			}
					
		statsGroupLock.release();
		
		return stat;
	}
	
	// An sorted list of the Group names in the manager
	public List<String> getStatList()
	{
		List<String> list = new ArrayList<String>();
		
		Iterator<SingleStat> listItr = statList.iterator();
		
		while(listItr.hasNext())
		{
			list.add(listItr.next().getStatName());
		}		
		
		Collections.sort(list,new sortComparator());
		
		return list;
	}

	/*
	 * Private comparator for the stat name list, 
	 * accounts for stats named in ranges ie > 100, > 200 etc
	 */
	private class sortComparator implements Comparator<String> 
	{

		@Override
		public int compare(String string1, String string2)
		{
			Integer int1;
			Integer int2;
			if(string1.contains("> "))
			{
				int1 = rangeSubString(string1);
				int2 = rangeSubString(string2);
				
				return int1.compareTo(int2);
			}
			
			return string1.compareTo(string2);
		}
		
		private Integer rangeSubString(String text)
		{
			String text2[] = 	text.split("> ");
			
			return Integer.parseInt(text2[1]);
		}
		
	}
	
	public void setGroupSettings(StatGroupSetting setting)
	{
		this.setting = setting;
	}
	
	public StatGroupSetting getGroupSettings()
	{
		return setting;
	}
	
	public String getName()
	{
		return groupName;
	}
	
	public void addStatGroupListener(StatGroupListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
			statGroupListeners.add(listener);
    	listenersLock.release();
	}
	
	public void removeStatGroupListener(StatGroupListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
			statGroupListeners.remove(listener);
    	listenersLock.release();
	}
	
	public void notifyStatGroupListeners()
	{
		listenersLock.acquireUninterruptibly();
		
		if(notifiyCalls % setting.getStatSampleRate() == 0)
		{
			for (StatGroupListenerInf listener : statGroupListeners)
		    {
		    	listener.groupStatsUpdated(statList);
		    }
		}
		
		notifiyCalls++;
		
	    listenersLock.release();
	}

	/*
	 * Bypass the sample rate so that the last stat output update always happens
	 */
	public void endEventNotifyStatGroupListeners()
	{
		listenersLock.acquireUninterruptibly();
		
		for (StatGroupListenerInf listener : statGroupListeners)
	    {
	    	listener.groupStatsUpdated(statList);
	    }
				
	    listenersLock.release();		
	}
	
}
