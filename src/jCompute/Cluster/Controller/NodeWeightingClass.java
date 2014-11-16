package jCompute.Cluster.Controller;

import java.util.ArrayList;

public class NodeWeightingClass
{
	private int weightClass;
	private ArrayList<NodeManager> nodes;
	
	private int lastNode;
	
	public NodeWeightingClass(int weightClass)
	{
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
	public NodeManager selectNodeManager()
	{
		int nodeNum = (lastNode+1) % nodes.size();
		
		return nodes.get(nodeNum);
	}
	
	public boolean isEmpty()
	{
		return nodes.size() > 0 ? true : false;
	}
	
}
