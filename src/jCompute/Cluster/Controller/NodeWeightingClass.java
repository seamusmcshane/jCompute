package jCompute.Cluster.Controller;

import java.util.ArrayList;

public class NodeWeightingClass
{
	private int weightClass;
	private ArrayList<NodeManager> nodes;
	
	private int lastNode;
	
	public NodeWeightingClass(int weightClass)
	{
		this.weightClass = weightClass;
		nodes = new ArrayList<NodeManager>();
		lastNode = 0;
	}
	
	public int getWeightClass()
	{
		return weightClass;
	}
	
	public void addNode(NodeManager node)
	{
		nodes.add(node);
	}
	
	public void removeNode(NodeManager node)
	{
		nodes.remove(node);
		
		if(lastNode>nodes.size())
		{
			lastNode = 0;
		}
		
	}
	
	// Cycles through the list
	public NodeManager selectBestFreeNodeManager()
	{
		NodeManager node = null;
		int checkCount = 0;
		int nodeNum;		
		
		// Ensure we alternate over the nodes but also check all the nodes if the first one is full
		while(checkCount < nodes.size())
		{
			nodeNum  = (lastNode+1) % nodes.size();
			
			node = nodes.get(nodeNum);
			
			if(node.hasFreeSlot())
			{
				break;
			}
			
			checkCount++;
		}
		
		return node;
	}
	
	public boolean isEmpty()
	{
		return nodes.size() > 0 ? false : true;
	}
	
}
