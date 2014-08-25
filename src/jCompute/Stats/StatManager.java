package jCompute.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Statistics manager - Not Thread Safe.
 *
 */
public class StatManager
{
	private String managerName;
	
	private HashMap<String, StatGroup> map;
	
	private ArrayList<StatGroup> statGroupList;
	
	private Semaphore statsManagerLock = new Semaphore(1);
	
	public StatManager(String managerName)
	{
		this.managerName = managerName;
		map = new HashMap<String, StatGroup>();
	}
	
	// Add a new stat to the stat manager
	public void registerGroup(StatGroup group)
	{		
		statsManagerLock.acquireUninterruptibly();
			map.put(group.getName(), group);
		statsManagerLock.release();
	}
	
	// Add a list of group to the stat manager
	public void registerGroups(List<StatGroup> groupList)
	{
		for (StatGroup group : groupList) 
		{
			registerGroup(group);
		}			
	}
	
	public void setGroupSettings(String groupName,StatGroupSetting setting)
	{
		statsManagerLock.acquireUninterruptibly();
			StatGroup group = map.get(groupName);
			group.setGroupSettings(setting);
		statsManagerLock.release();
	}
	
	// returns a group based on the group name requested
	public StatGroup getStatGroup(String groupName)
	{
		statsManagerLock.acquireUninterruptibly();
			StatGroup group = map.get(groupName);
		statsManagerLock.release();
		
		return group;
	}
	
	public int getStatGroupCount(String groupName)
	{
		statsManagerLock.acquireUninterruptibly();
			int statCount = map.size();
		statsManagerLock.release();
		return statCount;
	}
	
	// An unsorted list of the Stat names in the manager
	public Set<String> getGroupList()
	{
		return map.keySet();
	}
	
	public String getname()
	{
		return managerName;
	}
		
	public boolean containsGroup(String name)
	{
		statsManagerLock.acquireUninterruptibly();
			boolean status = map.containsKey(name);
		statsManagerLock.release();
		
		return status;
	}
	
	private List<StatGroup> getStatGroupList()
	{
		if(statGroupList == null)
		{
			statGroupList = new ArrayList<StatGroup>();
		}		
		
		if(statGroupList.size() != map.size())
		{
			statGroupList = new ArrayList<StatGroup>();
			
			Set<String> groupList = getGroupList();
			
			for(String group : groupList)
			{
				statGroupList.add(map.get(group));
			}			
		}
		
		return statGroupList;
	}
	
	public void notifiyStatListeners()
	{
		for (StatGroup group : getStatGroupList()) 
		{
			group.notifyStatGroupListeners();
		}
	}
	
	public void endEventNotifiyStatListeners()
	{
		for (StatGroup group : getStatGroupList()) 
		{
			group.endEventNotifyStatGroupListeners();
		}
	}

}
