package jCompute.Datastruct.knn.benchmark;

import java.util.ArrayList;

import jCompute.Datastruct.knn.ThirdGenKDWrapper;

public class NodeWeightingBenchmark
{
	private ThirdGenKDWrapper<TreeBenchObject> tree;
	private int objectCount;

	private ArrayList<TreeBenchObject> list;

	private int iterations;

	private TimerObj add = new TimerObj();
	private long addTime = 0;
	private TimerObj search = new TimerObj();
	private long searchTime = 0;

	private boolean running = false;
	private boolean cancelled = false;
	
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

		double[] pos;
		for(int y = 0; y < yMax; y++)
		{
			for(int x = 0; x < xMax; x++)
			{
				pos = new double[]{x,y};
				list.add(new TreeBenchObject(x * y + x, pos));
			}
		}
	}

	private void populateTree()
	{
		tree = new ThirdGenKDWrapper<TreeBenchObject>(2);

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
			if(!cancelled)
			{
				populateTree();
				searchTree();
			}
			else
			{
				break;
			}
		}
	}

	public void singleBenchmark()
	{
		addTime = 0;
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
		running = true;
		long fullWeighting = 0;
		long weightings[] = new long[runs];
		
		for(int r=0;r<runs;r++)
		{
			if(!cancelled)
			{
				singleBenchmark();
				weightings[r] = getWeighting();
				fullWeighting=fullWeighting+weightings[r];
			}
			else
			{
				fullWeighting = -1;
				break;
			}
		}
		
		running = false;
		
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
	
	public void cancel()
	{
		cancelled = true;
	}

	public boolean running()
	{
		return running;
	}

}
