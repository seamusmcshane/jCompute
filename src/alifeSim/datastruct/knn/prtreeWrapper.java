package alifeSim.datastruct.knn;

import java.util.LinkedList;

import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import alifeSim.Alife.AlifeBodyMBRConverter;

public class prtreeWrapper<Datatype> implements KNNInf<Datatype>
{
	PRTree<Datatype> tree;
	DistanceFunctions disFunc;
	
	public prtreeWrapper()
	{
		disFunc = new DistanceFunctions();
		//tree = new PRTree<AlifeBody>((MBRConverter<AlifeBody>) new AlifeBodyMBRConverter(2), 0);
	}
	
	@Override
	public void add(double[] pos, Datatype data)
	{
		
	}

	@Override
	public Datatype nearestNeighbour(double[] pos)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datatype nearestNNeighbour(double[] pos, int n)
	{
		//tree.nearestNeighbour(arg0, arg1, arg2, arg3)
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<Datatype> nearestNeighbours(double[] pos)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
