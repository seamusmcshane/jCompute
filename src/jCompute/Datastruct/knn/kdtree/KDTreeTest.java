package jCompute.datastruct.knn.kdtree;

import jCompute.Cluster.Node.WeightingBenchmark.TreeBenchObject;
import jCompute.datastruct.knn.KNNFloatPosInf;

import java.util.ArrayList;
import java.util.Random;

public class KDTreeTest
{
	public static void main(String args[])
	{
		System.out.println("Start");
		
		int num = 25;
		
		System.out.println("Object count " + num);

		ArrayList<KNNFloatPosInf> list = new ArrayList<KNNFloatPosInf>(num);
		
		Random r = new Random();
		float[] pos;
		for(int o=0;o<num;o++)
		{
			pos = new float[]{r.nextFloat()*25.0f,r.nextFloat()*25.0f};
			list.add(new TreeBenchObject(o,pos));
		}
				
		KDTreeBulk tree = new KDTreeBulk(2);
		
		tree.load(list);
		
		tree.dump();
		
		
	}
	
}
