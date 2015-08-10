package jCompute.Cluster.Node;

import jCompute.Stats.StatExporter;
import java.util.HashMap;

public class NodeProcessedItemStatCache
{
	private HashMap<Integer,StatExporter> statMap;
	
	public NodeProcessedItemStatCache()
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
