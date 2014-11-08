package jCompute.Datastruct.knn.kdtree;

import jCompute.Datastruct.knn.benchmark.TreeBenchObject;

import java.util.ArrayList;
import java.util.Random;

public class KDTreeTest
{
	public static void main(String args[])
	{
		System.out.println("Start");
		
		int num = 25;
		
		System.out.println("Object count " + num);

		ArrayList<TreeBenchObject> list = new ArrayList<TreeBenchObject>(num);
		
		Random r = new Random();
		float[] pos;
		for(int o=0;o<num;o++)
		{
			pos = new float[]{r.nextFloat()*25,r.nextFloat()*25};
			list.add(new TreeBenchObject(o,pos));
		}
				
		KDTreeBulk<TreeBenchObject> tree = new KDTreeBulk<TreeBenchObject>(2);
		
		tree.load(list);
		
		tree.dump();
		
		
	}
	
}
