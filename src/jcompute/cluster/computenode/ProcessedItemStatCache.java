package jcompute.cluster.computenode;

import java.util.HashMap;

import jcompute.results.export.Result;

public class ProcessedItemStatCache
{
	private HashMap<Integer,Result> statMap;
	
	public ProcessedItemStatCache()
	{
		statMap = new HashMap<Integer,Result>();
	}

	public synchronized void put(int simId,Result exporter)
	{
		statMap.put(simId, exporter);
	}
	
	public synchronized Result remove(int simId)
	{
		Result exporter = statMap.get(simId);
		
		statMap.remove(simId);
		
		return exporter;
	}
	
	public synchronized int getStatsStore()
	{
		return statMap.size();
	}
}
