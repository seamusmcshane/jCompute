package alifeSim.Stats;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class StatManager
{
	private String managerName;
	HashMap<String, StatGroup> map;
	Semaphore statsManagerLock = new Semaphore(1);
	
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
	
	// returns a group based on the group name requested
	public StatGroup getStatGroup(String groupName)
	{
		statsManagerLock.acquireUninterruptibly();
			StatGroup group = map.get(groupName);
		statsManagerLock.release();
		
		return group;
	}
	
	// An unsorted list of the Stat names in the manager
	public Set<String> getStatList()
	{
		return map.keySet();
	}

	public String getname()
	{
		return managerName;
	}
	
}