package alifeSim.datastruct.knn;

import java.util.LinkedList;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import alifeSim.Alife.SimpleAgent.SimpleAgent;

public class thirdGenKDWrapper<Datatype> implements KNNInf<Datatype>
{

	private int treenodes;
	
	/** References for Agent Tasks */
	private KdTree<Datatype> tree;
	
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
		tree = new KdTree<Datatype>(dim);
	}
	
	@Override
	public Datatype nearestNeighbor(int kd[])
	{
		return nearestNeighbor(kd);
	}
	
	@Override
	public Datatype nearestNeighbor(float kd[])
	{	
		return nearestNeighbor(kd);
	}

	@Override
	public Datatype nearestNeighbor(double kd[])
	{		
		// Get two agents - due to the closest agent to its self being its self, but one plant
		// Max is the next closest - Self is 0			
		return (Datatype) tree.findNearestNeighbors(kd, 2, distanceKD).getMax();
	}	
	
	@Override
	public LinkedList<Datatype> nearestNeighbors(double kd[])
	{
		return null;
	}

	@Override
	public void add(int kd[], Datatype agent)
	{
		add(kd,agent);
	}

	@Override
	public void add(float kd[], Datatype agent)
	{
		add(kd,agent);		
	}
	
	@Override
	public void add(double kd[],Datatype agent)
	{	
		tree.addPoint(kd, agent);		
		treenodes++;		
	}
	
	public int size()
	{
		return treenodes;
	}

	@Override
	public LinkedList<Datatype> nearestNeighbors(int kd[])
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<Datatype> nearestNeighbors(float kd[])
	{
		// TODO Auto-generated method stub
		return null;
	}

}
