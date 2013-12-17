package alifeSim.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class StatGroup
{
	private String groupName;
	private HashMap<String, StatInf> map;
	private Semaphore statsGroupLock = new Semaphore(1);
	private StatGroupSetting setting;
	
	public StatGroup(String groupName)
	{
		this.groupName = groupName;
		
		setting = new StatGroupSetting(groupName);
		
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
	
	// An sorted list of the Group names in the manager
	public List<String> getStatList()
	{
		List list = new ArrayList<String>(map.keySet());
		
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
	
}
