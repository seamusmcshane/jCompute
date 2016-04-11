package jcompute.datastruct.knn;

import java.util.ArrayList;
import java.util.List;

import ags.utils.dataStructures.singlePrecision.MaxHeapFloat;
import ags.utils.dataStructures.singlePrecision.kdTree.thirdGen.SquareEuclideanDistanceFunctionFloat;
import ags.utils.dataStructures.singlePrecision.kdTree.thirdGen.ThirdGenKdTreeFloat;

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

	public void add(KNNFloatPosInf pos, Datatype agent)
	{
		tree.addPoint(pos.updateAndGetPos(), agent);
		treenodes++;
	}

	public int size()
	{
		return treenodes;
	}

	public Datatype nearestNeighbour(KNNFloatPosInf pos)
	{
		// Max is the next closest - Self is 0 (if same tree)
		return tree.findNearestNeighbors(pos.getLatchedPos(), 1, distanceKD).getMax();
	}

	public List<Datatype> nearestNeighbours(KNNFloatPosInf pos, int maxNeighbours)
	{
		ArrayList<Datatype> list = new ArrayList<Datatype>(maxNeighbours);

		MaxHeapFloat<Datatype> heap = tree.findNearestNeighbors(pos.getLatchedPos(), maxNeighbours, distanceKD);

		while(heap.size() > 0)
		{
			list.add(heap.getMax());
			heap.removeMax();
		}
		return list;
	}

	public Datatype nearestNNeighbour(KNNFloatPosInf pos, int n)
	{
		// Get nth nearest neighbours
		return tree.findNearestNeighbors(pos.getLatchedPos(), n, distanceKD).getMax();
	}

}
