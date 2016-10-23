package jcompute.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import jcompute.stats.groups.TraceGroup;

/**
 * Statistics manager - Not Thread Safe.
 */
public class StatisticsManager
{
	private String managerName;
	
	private HashMap<String, TraceGroup> groupMap;
	private ArrayList<TraceGroup> traceGroupList;
	
	private Semaphore statsManagerLock = new Semaphore(1);
	
	public StatisticsManager(String managerName)
	{
		this.managerName = managerName;
		groupMap = new HashMap<String, TraceGroup>();
	}
	
	/**
	 * Add a new trace group to the manager
	 * 
	 * @param group
	 */
	public void registerGroup(TraceGroup group)
	{
		statsManagerLock.acquireUninterruptibly();
		groupMap.put(group.getName(), group);
		statsManagerLock.release();
	}
	
	/**
	 * Add a list of trace groups to the manager
	 * 
	 * @param groupList
	 */
	public void registerGroups(List<TraceGroup> groupList)
	{
		for(TraceGroup group : groupList)
		{
			registerGroup(group);
		}
	}
	
	public void setGroupSettings(String groupName, StatGroupSetting setting)
	{
		statsManagerLock.acquireUninterruptibly();
		TraceGroup group = groupMap.get(groupName);
		group.setGroupSettings(setting);
		statsManagerLock.release();
	}
	
	/**
	 * @param groupName
	 * @return A trace group matching the group name requested or null if none exists.
	 */
	public TraceGroup getStatGroup(String groupName)
	{
		statsManagerLock.acquireUninterruptibly();
		TraceGroup group = groupMap.get(groupName);
		statsManagerLock.release();
		
		return group;
	}
	
	/**
	 * An unsorted list of the group names in the manager, or if there are nested groups then the top level group names.
	 * 
	 * @return
	 */
	public Set<String> getGroupList()
	{
		return groupMap.keySet();
	}
	
	public String getname()
	{
		return managerName;
	}
	
	public boolean containsGroup(String name)
	{
		statsManagerLock.acquireUninterruptibly();
		boolean status = groupMap.containsKey(name);
		statsManagerLock.release();
		
		return status;
	}
	
	/**
	 * @return A list of the groups in this manager.
	 */
	private List<TraceGroup> getStatGroupList()
	{
		if(traceGroupList == null)
		{
			traceGroupList = new ArrayList<TraceGroup>();
		}
		
		if(traceGroupList.size() != groupMap.size())
		{
			traceGroupList = new ArrayList<TraceGroup>();
			
			Set<String> groupList = getGroupList();
			
			for(String group : groupList)
			{
				traceGroupList.add(groupMap.get(group));
			}
		}
		
		return traceGroupList;
	}
	
	/**
	 * Send a notification to trace group listeners of this manager.
	 */
	public void notifiyTraceListeners()
	{
		for(TraceGroup group : getStatGroupList())
		{
			group.notifyTraceGroupListeners();
		}
	}
	
	/**
	 * Send a notification to end event listeners of this manager.
	 */
	public void endEventNotifiyStatListeners()
	{
		for(TraceGroup group : getStatGroupList())
		{
			group.endEventNotifyTraceGroupListeners();
		}
	}
}
