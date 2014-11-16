package jCompute.Cluster.Controller;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.Event.NodeAdded;
import jCompute.Cluster.Controller.Event.NodeRemoved;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodesDatabase
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(NodesDatabase.class);

	private ArrayList<NodeWeightingClass> nodeWeightingClasses;
	private ArrayList<NodeManager> allNodes = new ArrayList<NodeManager>();

	/*
	 * List of priority re-scheduled Simulations (recovered from nodes that
	 * disappear)
	 */
	private ArrayList<Integer> recoveredSimIds;

	public NodesDatabase()
	{
		log.info("NodesDatabase created");

		nodeWeightingClasses = new ArrayList<NodeWeightingClass>();
		recoveredSimIds = new ArrayList<Integer>();
	}

	private NodeWeightingClass findWeightingClass(int weightClass)
	{
		for(NodeWeightingClass current : nodeWeightingClasses)
		{
			if(current.getWeightClass() == weightClass)
			{
				return current;
			}
		}

		return null;
	}

	public void add(NodeManager node)
	{
		long weighting = node.getWeighting();
		int weightClass = (int) (weighting / 1000);

		NodeWeightingClass tClass = findWeightingClass(weightClass);

		if(tClass == null)
		{
			tClass = new NodeWeightingClass(weightClass);
			nodeWeightingClasses.add(tClass);
		}

		tClass.addNode(node);
		allNodes.add(node);

		log.info("Added new Node " + node.getUid() + " " + weightClass);

		// Sort
		Collections.sort(nodeWeightingClasses, new WeightingClassComparator());

		JComputeEventBus.post(new NodeAdded(node.getNodeConfig()));
	}

	public NodeManager getNodeManager(int nid)
	{
		Iterator<NodeManager> itr = allNodes.iterator();
		NodeManager temp = null;
		NodeManager nodeManager = null;

		while(itr.hasNext())
		{
			temp = itr.next();

			if(temp.getUid() == nid)
			{
				nodeManager = temp;

				break;
			}

		}

		return nodeManager;
	}

	public void removeNode(NodeManager node)
	{
		long weighting = node.getWeighting();
		int weightClass = (int) (weighting / 1000);

		NodeWeightingClass tClass = findWeightingClass(weightClass);

		tClass.removeNode(node);

		if(tClass.isEmpty())
		{
			nodeWeightingClasses.remove(tClass);
		}

		allNodes.remove(node);
	}

	/*
	 * public NodeManager selectBestFreeNode() { NodeManager tNode = null;
	 * Iterator<NodeManager> itr = allNodes.iterator(); while(itr.hasNext()) {
	 * tNode = itr.next();
	 * 
	 * if(tNode.hasFreeSlot()) { break; } }
	 * 
	 * return tNode; }
	 */

	public NodeManager selectBestFreeNode()
	{
		NodeManager node = null;
		for(NodeWeightingClass current : nodeWeightingClasses)
		{
			node = current.selectBestFreeNodeManager();

			if(node != null)
			{
				if(node.hasFreeSlot())
				{
					break;
				}
			}
		}

		return node;
	}

	public int nodeCount()
	{
		return allNodes.size();
	}

	public int refreshNodes(int timerCount)
	{
		int maxSims = 0;
		Iterator<NodeManager> itr = allNodes.iterator();

		while(itr.hasNext())
		{
			NodeManager node = itr.next();

			if(!node.isActive())
			{
				ArrayList<Integer> nodeRecoveredSimIds = node.getRecoverableSimsIds();

				Iterator<Integer> nRSIdsIter = nodeRecoveredSimIds.iterator();
				while(nRSIdsIter.hasNext())
				{
					recoveredSimIds.add(nRSIdsIter.next());

				}

				// InActive Node Removed
				JComputeEventBus.post(new NodeRemoved(node.getNodeConfig()));

				log.debug("Node " + node.getUid() + " no longer Active");
				node.destroy("Node no longer active");
				itr.remove();
			}
			else
			{
				maxSims += node.getMaxSims();

				node.triggerNodeStatRequest(timerCount);
			}

		}

		return maxSims;
	}

	public boolean hasRecoverableSims()
	{
		if(recoveredSimIds.size() > 0)
		{
			return true;
		}

		return false;
	}

	public boolean hasFreeSlot()
	{
		boolean sFree = false;

		Iterator<NodeManager> itr = allNodes.iterator();
		while(itr.hasNext())
		{
			NodeManager node = itr.next();

			sFree |= node.hasFreeSlot();
		}

		return sFree;
	}

	public ArrayList<Integer> getRecoverableSimIds()
	{
		ArrayList<Integer> simIds = new ArrayList<Integer>();

		Iterator<Integer> itr = recoveredSimIds.iterator();
		while(itr.hasNext())
		{
			simIds.add(itr.next());
		}

		recoveredSimIds = new ArrayList<Integer>();

		return simIds;
	}

	private class WeightingClassComparator implements Comparator<NodeWeightingClass>
	{
		@Override
		public int compare(NodeWeightingClass wc1, NodeWeightingClass wc2)
		{
			if(wc1.getWeightClass() < wc2.getWeightClass())
			{
				return -1;
			}
			else if(wc1.getWeightClass() > wc2.getWeightClass())
			{
				return 1;
			}

			return 0;
		}
	}
}
