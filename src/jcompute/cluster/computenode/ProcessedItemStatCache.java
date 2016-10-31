package jcompute.cluster.computenode;

import java.util.HashMap;

import jcompute.results.ResultExporter;

public class ProcessedItemStatCache
{
	private HashMap<Integer,ResultExporter> statMap;
	
	public ProcessedItemStatCache()
	{
		statMap = new HashMap<Integer,ResultExporter>();
	}

	public synchronized void put(int simId,ResultExporter exporter)
	{
		statMap.put(simId, exporter);
	}
	
	public synchronized ResultExporter remove(int simId)
	{
		ResultExporter exporter = statMap.get(simId);
		
		statMap.remove(simId);
		
		return exporter;
	}
	
	public synchronized int getStatsStore()
	{
		return statMap.size();
	}
}
