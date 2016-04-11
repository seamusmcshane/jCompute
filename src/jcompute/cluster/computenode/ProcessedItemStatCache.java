package jcompute.cluster.computenode;

import java.util.HashMap;

import jcompute.stats.StatExporter;

public class ProcessedItemStatCache
{
	private HashMap<Integer,StatExporter> statMap;
	
	public ProcessedItemStatCache()
	{
		statMap = new HashMap<Integer,StatExporter>();
	}

	public synchronized void put(int simId,StatExporter exporter)
	{
		statMap.put(simId, exporter);
	}
	
	public synchronized StatExporter remove(int simId)
	{
		StatExporter exporter = statMap.get(simId);
		
		statMap.remove(simId);
		
		return exporter;
	}
	
	public synchronized int getStatsStore()
	{
		return statMap.size();
	}
	
}