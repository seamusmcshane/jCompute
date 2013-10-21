package alife.Simulation.GlobalStats;

import java.util.HashMap;
import java.util.Map;

public class GlobalStatisticsManager
{
	/* A Hash Map to store our statistics */
	Map<String, Integer> map;
	
	public GlobalStatisticsManager()
	{
		map  = new HashMap<String, Integer>();
	}
	
	public void addNewStatistic(String name)
	{
		setStatValue(name,0);
	}
		
	public void setStatValue(String name,int value)
	{
		map.put(name, value);
	}

	public int getStatValue(String name)
	{
		return map.get(name);
	}
	
}
