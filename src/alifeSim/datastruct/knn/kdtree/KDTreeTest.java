package alifeSim.datastruct.knn.kdtree;

import java.util.ArrayList;
import java.util.Random;

import alifeSim.datastruct.knn.TreeBenchObject;

public class KDTreeTest
{
	public static void main(String args[])
	{
		System.out.println("Start");
		
		int num = 25;
		
		System.out.println("Object count " + num);

		ArrayList<TreeBenchObject> list = new ArrayList<TreeBenchObject>(num);
		
		Random r = new Random();
		
		for(int o=0;o<num;o++)
		{
			list.add(new TreeBenchObject(o,r.nextFloat()*25,r.nextFloat()*25));
		}
				
		KDTreeBulk<TreeBenchObject> tree = new KDTreeBulk<TreeBenchObject>(2);
		
		tree.load(list);
		
		tree.dump();
		
		
	}
	
}