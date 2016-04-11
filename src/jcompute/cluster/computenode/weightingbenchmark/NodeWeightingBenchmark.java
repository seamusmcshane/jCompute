package jcompute.cluster.computenode.weightingbenchmark;

import java.util.ArrayList;

import jcompute.datastruct.knn.KNNFloatPosInf;
import jcompute.datastruct.knn.ThirdGenKDWrapperFloat;
import jcompute.timing.TimerObj;

public class NodeWeightingBenchmark
{
	private ThirdGenKDWrapperFloat<KNNFloatPosInf> tree;
	private int objectCount;

	private ArrayList<TreeBenchObject> list;

	private int iterations;

	// Default
	private int bucketSize = 24;

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
	
	public NodeWeightingBenchmark(int objectCount, int iterations, int buckSize)
	{
		this.objectCount = objectCount;
		this.iterations = iterations;
		this.bucketSize = buckSize;
	}
	
	private void generateObjects()
	{
		int xMax = (int) Math.sqrt(objectCount);
		int yMax = (int) Math.sqrt(objectCount);

		list = new ArrayList<TreeBenchObject>();

		float[] pos;
		for(int y = 0; y < yMax; y++)
		{
			for(int x = 0; x < xMax; x++)
			{
				pos = new float[]{x,y};
				list.add(new TreeBenchObject(x * y + x, pos));
			}
		}
	}

	private void populateTree()
	{
		tree = new ThirdGenKDWrapperFloat<KNNFloatPosInf>(2,bucketSize);

		for(KNNFloatPosInf object : list)
		{
			tree.add(object, object);
		}
	}

	private void searchTree()
	{
		for(KNNFloatPosInf object : list)
		{
			TreeBenchObject cto = (TreeBenchObject)object;
			TreeBenchObject no = (TreeBenchObject)tree.nearestNeighbour(object);
			
			cto.setNearestObject(no);
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