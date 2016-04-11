package jcompute.datastruct.knn;

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

	public void add(KNNDoublePosInf pos, Datatype agent)
	{
		tree.addPoint(pos.updateAndGetPos(), agent);
		treenodes++;
	}

	public int size()
	{
		return treenodes;
	}

	public Datatype nearestNeighbour(KNNDoublePosInf pos)
	{
		// Max is the next closest - Self is 0 (if same tree)
		return tree.findNearestNeighbors(pos.getLatchedPos(), 1, distanceKD).getMax();
	}

	public List<Datatype> nearestNeighbours(KNNDoublePosInf pos, int maxNeighbours)
	{
		ArrayList<Datatype> list = new ArrayList<Datatype>(maxNeighbours);

		MaxHeapDouble<Datatype> heap = tree.findNearestNeighbors(pos.getLatchedPos(), maxNeighbours, distanceKD);

		while(heap.size() > 0)
		{
			list.add(heap.getMax());
			heap.removeMax();
		}
		return list;
	}

	public Datatype nearestNNeighbour(KNNDoublePosInf pos, int n)
	{
		// Get nth nearest neighbours
		return tree.findNearestNeighbors(pos.getLatchedPos(), n, distanceKD).getMax();
	}

}
