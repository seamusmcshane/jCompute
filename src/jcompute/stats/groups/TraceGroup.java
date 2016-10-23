package jcompute.stats.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import jcompute.stats.StatGroupSetting;
import jcompute.stats.trace.Trace;

public class TraceGroup
{
	// Group Name
	private String groupName;
	
	// Traces in this group - we do not check for duplicates
	private ArrayList<Trace> traceList;
	
	// Group lock
	private Semaphore groupLock = new Semaphore(1);
	
	// Group Settings
	private StatGroupSetting setting;
	private int notifiyCalls = 0;
	
	// The Listeners for changes in this stat groups stats
	private List<StatGroupListenerInf> statGroupListeners = new ArrayList<StatGroupListenerInf>();
	
	// Lock for the listeners
	private Semaphore listenersLock = new Semaphore(1, false);
	
	public TraceGroup(String groupName)
	{
		this.groupName = groupName;
		
		setting = new StatGroupSetting(groupName);
		
		traceList = new ArrayList<Trace>();
	}
	
	/**
	 * Add a new trace to the group.
	 * 
	 * @param trace
	 */
	private void registerTrace(Trace trace)
	{
		traceList.add(trace);
	}
	
	/**
	 * Add a list of traces to the group.
	 * 
	 * @param trace
	 */
	public void registerTraces(List<Trace> list)
	{
		groupLock.acquireUninterruptibly();
		
		for(Trace trace : list)
		{
			registerTrace(trace);
		}
		
		groupLock.release();
	}
	
	/**
	 * @param name
	 * The name of the trace to look up.
	 * @return If it exists - a trace matching the name provided.
	 */
	public Trace getTrace(String traceName)
	{
		groupLock.acquireUninterruptibly();
		
		Iterator<Trace> listItr = traceList.iterator();
		
		Trace stat = null;
		
		while(listItr.hasNext())
		{
			Trace tempStat = listItr.next();
			if(tempStat.name.equals(traceName))
			{
				stat = tempStat;
				
				// Found Sample Exit loop
				break;
			}
		}
		
		groupLock.release();
		
		return stat;
	}
	
	/**
	 * @return A sorted list of the trace names in the group.
	 */
	public List<String> getTraceList()
	{
		List<String> list = new ArrayList<String>();
		
		Iterator<Trace> listItr = traceList.iterator();
		
		while(listItr.hasNext())
		{
			list.add(listItr.next().name);
		}
		
		Collections.sort(list, new sortComparator());
		
		return list;
	}
	
	/**
	 * Private comparator to handle special trace names.
	 * Currently accounts for traces named as integer ranges i.e "> 100", "> 200" etc
	 * 
	 * @author Seamus McShane
	 */
	private class sortComparator implements Comparator<String>
	{
		
		@Override
		public int compare(String string1, String string2)
		{
			if(string1.contains("> "))
			{
				Integer int1 = rangeSubString(string1);
				Integer int2 = rangeSubString(string2);
				
				return int1.compareTo(int2);
			}
			
			return string1.compareTo(string2);
		}
		
		private Integer rangeSubString(String text)
		{
			String text2[] = text.split("> ");
			
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
	
	public void notifyTraceGroupListeners()
	{
		listenersLock.acquireUninterruptibly();
		
		if(notifiyCalls % setting.getStatSampleRate() == 0)
		{
			for(StatGroupListenerInf listener : statGroupListeners)
			{
				listener.groupStatsUpdated(traceList);
			}
		}
		
		notifiyCalls++;
		
		listenersLock.release();
	}
	
	/*
	 * Bypass the sample rate so that the last stat output update always happens
	 */
	public void endEventNotifyTraceGroupListeners()
	{
		listenersLock.acquireUninterruptibly();
		
		for(StatGroupListenerInf listener : statGroupListeners)
		{
			listener.groupStatsUpdated(traceList);
		}
		
		listenersLock.release();
	}
}
