package alife;

import java.util.LinkedList;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;

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
	public SimpleAgent nearestNeighbor(int x, int y)
	{
		return nearestNeighbor((float)x,(float)y);
	}
	
	@Override
	public SimpleAgent nearestNeighbor(float x, float y)
	{	
		return nearestNeighbor((double)x,(double)y);
	}

	@Override
	public SimpleAgent nearestNeighbor(double x, double y)
	{
		// Convert our vector to the format for the tree
		double[] pos = new double[2];
		pos[0] = x;
		pos[1] = y;
		
		// Get two agents - due to the closest agent to its self being its self, but one plant
		agentNeighborList = tree.findNearestNeighbors(pos, 2, distanceKD);

		// Max is the next closest - Self is 0			
		return agentNeighborList.getMax();
	}	
	
	@Override
	public LinkedList<SimpleAgent> nearestNeighbors(double x, double y)
	{
		return null;
	}

	@Override
	public void add(int x, int y, SimpleAgent agent)
	{
		add((float)x,(float)y,agent);
	}

	@Override
	public void add(float x, float y, SimpleAgent agent)
	{
		add((double)x,(double)y,agent);		
	}
	
	@Override
	public void add(double x, double y,SimpleAgent agent)
	{
		double[] pos = new double[2];
		pos[0] = x;
		pos[1] = y;
		
		//System.out.println("B) X " + x + " Y " + y);
		
		tree.addPoint(pos, agent);
		
		treenodes++;		
	}
	
	public int size()
	{
		return treenodes;
	}

	@Override
	public LinkedList<SimpleAgent> nearestNeighbors(int x, int y)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<SimpleAgent> nearestNeighbors(float x, float y)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
