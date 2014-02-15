package alifeSim.datastruct.knn;

import java.util.ArrayList;
import java.util.LinkedList;

import jk.mega.KDTree.Euclidean;
import jk.mega.KDTree.SearchResult;

public class JKMegaKDWrapper<Datatype> implements KNNInf<Datatype>
{

	private int treenodes;
	
	/** References for Agent Tasks */
	private Euclidean<Datatype> tree;
		
	public JKMegaKDWrapper(int dim)
	{		
		tree = null;
		treenodes = 0;
		tree = new Euclidean<Datatype>(dim);
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
	public Datatype nearestNeighbour(double kd[])
	{		
		// Max is the next closest - Self is 0 (if same tree)	
		return (Datatype) tree.nearestNeighbours(kd, 1).get(0).payload;
	}	
	
	@Override
	public LinkedList<Datatype> nearestNeighbours(double kd[])
	{
		/* NOT IMPLEMENTED */
		return null;
	}
	
	@Override
	public Datatype nearestNNeighbour(double[] pos, int n)
	{
	
		ArrayList<SearchResult<Datatype>> temp = tree.nearestNeighbours(pos, n);
		
		Datatype result = null;
		
		if(temp.size() >=2)
		{
			result = temp.get(1).payload;
		}
		
		
		
		// Get nth nearest neighbours	
		return  result;
	}

}
