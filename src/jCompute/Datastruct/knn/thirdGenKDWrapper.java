package jCompute.Datastruct.knn;

import java.util.ArrayList;
import java.util.List;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;

public class ThirdGenKDWrapper<Datatype>
{
	private int treenodes;
	
	/** References for Agent Tasks */
	private KdTree<Datatype> tree;
	
	/** The distance function object. */
	private final SquareEuclideanDistanceFunction distanceKD = new SquareEuclideanDistanceFunction();
	
	public ThirdGenKDWrapper(int dim)
	{		
		treenodes = 0;
		tree = new KdTree<Datatype>(dim,24);
	}
	
	public void add(double kd[],Datatype agent)
	{	
		tree.addPoint(kd, agent);		
		treenodes++;		
	}
	
	public int size()
	{
		return treenodes;
	}

	public Datatype nearestNeighbour(double kd[])
	{		
		// Max is the next closest - Self is 0 (if same tree)	
		return (Datatype) tree.findNearestNeighbors(kd, 1, distanceKD).getMax();
	}	

	public List<Datatype> nearestNeighbours(double kd[], int maxNeighbours)
	{
		ArrayList <Datatype> list = new ArrayList<Datatype>(maxNeighbours);
		
		MaxHeap<Datatype> heap = tree.findNearestNeighbors(kd, maxNeighbours, distanceKD);
		
		while(heap.size() > 0)
		{
			list.add(heap.getMax());
			heap.removeMax();
		}
		return list;	
	}
	
	public Datatype nearestNNeighbour(double[] pos, int n)
	{
		// Get nth nearest neighbours	
		return (Datatype) tree.findNearestNeighbors(pos, n, distanceKD).getMax();
	}

}
