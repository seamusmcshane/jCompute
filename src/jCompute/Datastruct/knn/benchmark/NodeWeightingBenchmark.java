package jCompute.Datastruct.knn.benchmark;

import java.util.ArrayList;

import jCompute.Datastruct.knn.KNNInf;
import jCompute.Datastruct.knn.thirdGenKDWrapper;
import jCompute.Datastruct.knn.benchmark.TreeBenchmarks.TimerObj;

public class NodeWeightingBenchmark
{
	private KNNInf<TreeBenchObject> tree;
	private int objectCount;

	private ArrayList<TreeBenchObject> list;

	private int iterations;

	private TimerObj add = new TimerObj();
	private long addTime = 0;
	private TimerObj search = new TimerObj();
	private long searchTime = 0;

	public NodeWeightingBenchmark(int objectCount, int iterations)
	{
		this.objectCount = objectCount;
		this.iterations = iterations;
	}
	
	private void generateObjects()
	{
		int xMax = (int) Math.sqrt(objectCount);
		int yMax = (int) Math.sqrt(objectCount);

		list = new ArrayList<TreeBenchObject>();

		for(int y = 0; y < yMax; y++)
		{
			for(int x = 0; x < xMax; x++)
			{
				list.add(new TreeBenchObject(x * y + x, x, y));
			}
		}
	}

	private void populateTree()
	{
		tree = new thirdGenKDWrapper(2);

		for(TreeBenchObject object : list)
		{
			tree.add(object.getPos(), object);
		}
	}

	private void searchTree()
	{
		for(TreeBenchObject object : list)
		{
			object.setNearestObject(tree.nearestNeighbour(object.getPos()));
		}
	}

	public void warmUp(int warmup)
	{
		generateObjects();

		for(int w = 0; w < warmup; w++)
		{
			populateTree();
			searchTree();
		}
	}

	public void singleBenchmark()
	{
		add.resetTimer();
		addTime = 0;
		search.resetTimer();
		searchTime = 0;
		for(int b = 0; b < iterations; b++)
		{
			add.startTimer();
			populateTree();
			add.stopTimer();
			addTime += add.getTimeTaken();

			search.startTimer();
			searchTree();
			search.stopTimer();
			searchTime += search.getTimeTaken();
		}
	}
	
	public long weightingBenchmark(int runs)
	{
		long fullWeighting = 0;
		long weightings[] = new long[runs];
		
		for(int r=0;r<runs;r++)
		{
			singleBenchmark();
			weightings[r] = getWeighting();
			fullWeighting=fullWeighting+weightings[r];
		}
		
		return fullWeighting;
	}
	
	public void outputResults()
	{
		System.out.println("Objects\t" + objectCount);
		System.out.println("Iterations\t" + iterations);
		System.out.println("Add Time\t" + addTime);
		System.out.println("Search Time\t" + searchTime);
		System.out.println("Weighting\t" + getWeighting());
	}
	
	public long getWeighting()
	{
		return addTime+searchTime;
	}

}
