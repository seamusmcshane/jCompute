package jCompute.Datastruct.knn;

import java.util.List;

import jk.mega.KDTree;
import jk.mega.KDTree.SearchResult;

public class JKMegaWrapper<Datatype> implements KNNInf<Datatype>
{
	private KDTree<Datatype> tree;
	private int treenodes;

	public JKMegaWrapper(int dim)
	{		
		treenodes = 0;
		tree = new KDTree.Euclidean<Datatype>(dim);
	}
	
	@Override
	public void add(double kd[], Datatype agent)
	{
		tree.addPoint(kd, agent);		
		treenodes++;		
	}

	@Override
	public Datatype nearestNeighbour(double[] pos)
	{		
		List<SearchResult<Datatype>> list = tree.nearestNeighbours(pos, 1);

		return (Datatype) list.get(0).payload;
	}

	@Override
	public Datatype nearestNNeighbour(double[] pos, int n)
	{
		return (Datatype) tree.nearestNeighbours(pos, n).get(n-1).payload;
	}

	@Override
	public List nearestNeighbours(double[] pos)
	{
		// Get nth nearest neighbours	
		return tree.nearestNeighbours(pos, 1);
	}

	@Override
	public int size()
	{
		return treenodes;
	}

}
