package jcompute.datastruct.knn;

import ags.utils.dataStructures.KNN1.KNN1KdTreeFloat;
import ags.utils.dataStructures.singlePrecision.kdTree.thirdGen.SquareEuclideanDistanceFunctionFloat;

public class ThirdGenKDWrapperFloat<Datatype>
{
	private int treenodes;
	
	/** References for Agent Tasks */
	private KNN1KdTreeFloat<Datatype> tree;
	
	/** The distance function object. */
	private final SquareEuclideanDistanceFunctionFloat distanceKD = new SquareEuclideanDistanceFunctionFloat();
	
	public ThirdGenKDWrapperFloat(int dim)
	{
		treenodes = 0;
		tree = new KNN1KdTreeFloat<Datatype>(dim, 24);
	}
	
	public ThirdGenKDWrapperFloat(int dim, int bucketSize)
	{
		treenodes = 0;
		tree = new KNN1KdTreeFloat<Datatype>(dim, bucketSize);
	}
	
	public void add(KNNFloatPosInf pos, Datatype data)
	{
		// TODO FIX
		float[] posF = new float[]
		{
			pos.getXY().x, pos.getXY().y
		};
		
		tree.addPoint(posF, data);
		treenodes++;
	}
	
	public void add(float[] pos, Datatype data)
	{
		tree.addPoint(pos, data);
		treenodes++;
	}
	
	public int size()
	{
		return treenodes;
	}
	
	public Datatype kn1NearestNeighbour(KNNFloatPosInf pos, Datatype data)
	{
		// KNN1AgentSearchRule agentSearch = new KNN1AgentSearchRule(pos.getLatchedPos(), data);
		
		// TODO FIX
		float[] posF = new float[]
		{
			pos.getXY().x, pos.getXY().y
		};
		
		return tree.kn1NearestNeighbor(posF, data, distanceKD);
		// return tree.findNearestNeighbors(pos.getLatchedPos(), 2, distanceKD).getMax();
	}
	
	public Datatype kn1NearestNeighbour(float[] pos, Datatype data)
	{
		// KNN1AgentSearchRule agentSearch = new KNN1AgentSearchRule(pos.getLatchedPos(), data);
		
		return tree.kn1NearestNeighbor(pos, data, distanceKD);
		// return tree.findNearestNeighbors(pos.getLatchedPos(), 2, distanceKD).getMax();
	}
	
	// public Datatype kn1NearestNeighbourPlant(KNNFloatPosInf pos)
	// {
	// // KNN1PlantSearchRule plantSearch = new KNN1PlantSearchRule(pos.getLatchedPos());
	//
	// return tree.kn1NearestNeighbor(pos.getLatchedPos(), null, distanceKD);
	// // return tree.findNearestNeighbors(pos.getLatchedPos(), 1, distanceKD).getMax();
	// }
	
	// public Datatype nearestNeighbour(KNNFloatPosInf pos)
	// {
	// // Max is the next closest - Self is 0 (if same tree)
	// return tree.findNearestNeighbors(pos.getLatchedPos(), 1, distanceKD).getMax();
	// //return tree.findNearestNeighbors(pos.getLatchedPos(), 1, distanceKD).getMax();
	// }
	//
	// public List<Datatype> nearestNeighbours(KNNFloatPosInf pos, int maxNeighbours)
	// {
	// ArrayList<Datatype> list = new ArrayList<Datatype>(maxNeighbours);
	//
	// MaxHeapFloat<Datatype> heap = tree.findNearestNeighbors(pos.getLatchedPos(), maxNeighbours, distanceKD);
	//
	// while(heap.size() > 0)
	// {
	// list.add(heap.getMax());
	// heap.removeMax();
	// }
	// return list;
	// }
	//
	// public Datatype nearestNNeighbour(KNNFloatPosInf pos, int n)
	// {
	// // Get nth nearest neighbours
	// return tree.findNearestNeighbors(pos.getLatchedPos(), n, distanceKD).getMax();
	// }
	
}
