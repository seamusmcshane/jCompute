package jCompute.Datastruct.knn;

import java.util.ArrayList;
import java.util.List;

import ags.utils.dataStructures.doublePrecision.MaxHeapDouble;
import ags.utils.dataStructures.doublePrecision.kdTree.thirdGen.SquareEuclideanDistanceFunctionDouble;
import ags.utils.dataStructures.doublePrecision.kdTree.thirdGen.ThirdGenKdTreeDouble;

public class ThirdGenKDWrapperDouble<Datatype>
{
	private int treenodes;

	/** References for Agent Tasks */
	private ThirdGenKdTreeDouble<Datatype> tree;

	/** The distance function object. */
	private final SquareEuclideanDistanceFunctionDouble distanceKD = new SquareEuclideanDistanceFunctionDouble();

	public ThirdGenKDWrapperDouble(int dim)
	{
		treenodes = 0;
		tree = new ThirdGenKdTreeDouble<Datatype>(dim, 24);
	}

	public ThirdGenKDWrapperDouble(int dim, int bucketSize)
	{
		treenodes = 0;
		tree = new ThirdGenKdTreeDouble<Datatype>(dim, bucketSize);
	}

	public void add(double kd[], Datatype agent)
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
		return tree.findNearestNeighbors(kd, 1, distanceKD).getMax();
	}

	public List<Datatype> nearestNeighbours(double kd[], int maxNeighbours)
	{
		ArrayList<Datatype> list = new ArrayList<Datatype>(maxNeighbours);

		MaxHeapDouble<Datatype> heap = tree.findNearestNeighbors(kd, maxNeighbours, distanceKD);

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
		return tree.findNearestNeighbors(pos, n, distanceKD).getMax();
	}

}
