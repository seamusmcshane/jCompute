package alifeSim.Stats;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import alifeSim.Alife.SimpleAgent.SimpleAgent;

public class StatGroup
{
	private String groupName;
	HashMap<String, Stat> map;
	Semaphore statsGroupLock = new Semaphore(1);
	
	public StatGroup(String groupName)
	{
		this.groupName = groupName;
		map = new HashMap<String, Stat>();
	}
	
	// Add a new stat to the stat manager
	public void registerStat(Stat stat)
	{
		statsGroupLock.acquireUninterruptibly();
			map.put(stat.getStatName(), stat);
		statsGroupLock.release();
	}
	
	// Add a list of stats to the stat manager
	public void registerStats(List<Stat> statList)
	{
		for (Stat stat : statList) 
		{
			registerStat(stat);
		}			
	}
	
	// returns a stat based on the stat name requested
	public Stat getStat(String statName)
	{
		statsGroupLock.acquireUninterruptibly();
			Stat stat = map.get(statName);
		statsGroupLock.release();
		
		return stat;
	}
	
	// An unsorted list of the Group names in the manager
	public Set<String> getStatList()
	{
		return map.keySet();
	}

	public String getName()
	{
		return groupName;
	}
	
}
