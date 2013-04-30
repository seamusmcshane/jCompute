package alifeSim.datastruct.knn;

import java.util.LinkedList;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import alifeSim.Alife.SimpleAgent.SimpleAgent;

public class thirdGenKDWrapper<T> implements KNNInf<T>
{

	private int treenodes;
	
	/** References for Agent Tasks */
	private KdTree<SimpleAgent> tree;
	
	private MaxHeap<SimpleAgent> agentNeighborList;
	
	/** The distance function object. */
	private final SquareEuclideanDistanceFunction distanceKD = new SquareEuclideanDistanceFunction();
	
	public thirdGenKDWrapper()
	{
		treenodes = 0;
	}

	@Override
	public void init(int dim)
	{		
		tree = null;
		treenodes = 0;
		tree = new KdTree<SimpleAgent>(dim);
	}
	
	@Override
	public SimpleAgent nearestNeighbor(int kd[])
	{
		return nearestNeighbor(kd);
	}
	
	@Override
	public SimpleAgent nearestNeighbor(float kd[])
	{	
		return nearestNeighbor(kd);
	}

	@Override
	public SimpleAgent nearestNeighbor(double kd[])
	{		
		// Get two agents - due to the closest agent to its self being its self, but one plant
		agentNeighborList = tree.findNearestNeighbors(kd, 2, distanceKD);

		// Max is the next closest - Self is 0			
		return agentNeighborList.getMax();
	}	
	
	@Override
	public LinkedList<SimpleAgent> nearestNeighbors(double kd[])
	{
		return null;
	}

	@Override
	public void add(int kd[], SimpleAgent agent)
	{
		add(kd,agent);
	}

	@Override
	public void add(float kd[], SimpleAgent agent)
	{
		add(kd,agent);		
	}
	
	@Override
	public void add(double kd[],SimpleAgent agent)
	{	
		tree.addPoint(kd, agent);		
		treenodes++;		
	}
	
	public int size()
	{
		return treenodes;
	}

	@Override
	public LinkedList<SimpleAgent> nearestNeighbors(int kd[])
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<SimpleAgent> nearestNeighbors(float kd[])
	{
		// TODO Auto-generated method stub
		return null;
	}

}
