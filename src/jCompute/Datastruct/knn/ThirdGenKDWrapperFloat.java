package jCompute.Datastruct.knn;

import java.util.ArrayList;
import java.util.List;

import ags.utils.dataStructures.singlePrecision.kdTree.thirdGen.ThirdGenKdTreeFloat;
import ags.utils.dataStructures.singlePrecision.kdTree.thirdGen.SquareEuclideanDistanceFunctionFloat;
import ags.utils.dataStructures.doublePrecision.kdTree.thirdGen.ThirdGenKdTreeDouble;
import ags.utils.dataStructures.singlePrecision.MaxHeapFloat;

public class ThirdGenKDWrapperFloat<Datatype>
{
	private int treenodes;
	
	/** References for Agent Tasks */
	private ThirdGenKdTreeFloat<Datatype> tree;
	
	/** The distance function object. */
	private final SquareEuclideanDistanceFunctionFloat distanceKD = new SquareEuclideanDistanceFunctionFloat();
	
	public ThirdGenKDWrapperFloat(int dim)
	{
		treenodes = 0;
		tree = new ThirdGenKdTreeFloat<Datatype>(dim, 24);
	}
	
	public ThirdGenKDWrapperFloat(int dim, int bucketSize)
	{
		treenodes = 0;
		tree = new ThirdGenKdTreeFloat<Datatype>(dim, bucketSize);
	}
	
	public void add(float kd[], Datatype agent)
	{
		tree.addPoint(kd, agent);
		treenodes++;
	}
	
	public int size()
	{
		return treenodes;
	}
	
	public Datatype nearestNeighbour(float kd[])
	{
		// Max is the next closest - Self is 0 (if same tree)
		return (Datatype) tree.findNearestNeighbors(kd, 1, distanceKD).getMax();
	}
	
	public List<Datatype> nearestNeighbours(float kd[], int maxNeighbours)
	{
		ArrayList<Datatype> list = new ArrayList<Datatype>(maxNeighbours);
		
		MaxHeapFloat<Datatype> heap = tree.findNearestNeighbors(kd, maxNeighbours, distanceKD);
		
		while(heap.size() > 0)
		{
			list.add(heap.getMax());
			heap.removeMax();
		}
		return list;
	}
	
	public Datatype nearestNNeighbour(float[] pos, int n)
	{
		// Get nth nearest neighbours
		return (Datatype) tree.findNearestNeighbors(pos, n, distanceKD).getMax();
	}
	
}
