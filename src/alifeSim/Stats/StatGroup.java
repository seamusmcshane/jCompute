package alifeSim.Stats;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class StatGroup
{
	private String groupName;
	HashMap<String, StatInf> map;
	Semaphore statsGroupLock = new Semaphore(1);
	
	public StatGroup(String groupName)
	{
		this.groupName = groupName;
		map = new HashMap<String, StatInf>();
	}
	
	// Add a new stat to the stat manager
	public void registerStat(StatInf stat)
	{
		statsGroupLock.acquireUninterruptibly();
			map.put(stat.getStatName(), stat);
		statsGroupLock.release();
	}
	
	// Add a list of stats to the stat manager
	public void registerStats(List<StatInf> statList)
	{
		for (StatInf stat : statList) 
		{
			registerStat(stat);
		}			
	}
	
	// returns a stat based on the stat name requested
	public StatInf getStat(String statName)
	{
		statsGroupLock.acquireUninterruptibly();
			StatInf stat = map.get(statName);
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
