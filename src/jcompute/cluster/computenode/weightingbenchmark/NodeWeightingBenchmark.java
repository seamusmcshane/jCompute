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
		tree = new ThirdGenKDWrapperFloat<KNNFloatPosInf>(2, bucketSize);
		
		for(KNNFloatPosInf object : list)
		{
			tree.add(object, object);
		}
	}
	
	private void searchTree()
	{
		for(KNNFloatPosInf object : list)
		{
			TreeBenchObject cto = (TreeBenchObject) object;
			TreeBenchObject no = (TreeBenchObject) tree.kn1NearestNeighbour(object, null);
			
			cto.setNearestObject(no);
		}
	}
	
	public long warmUp(int warmup)
	{
		generateObjects();
		
		TimerObj add = new TimerObj();
		TimerObj search = new TimerObj();
		
		long addTime = 0;
		long searchTime = 0;
		
		for(int w = 0; w < warmup; w++)
		{
			if(!cancelled)
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
			else
			{
				break;
			}
		}
		
		return addTime + searchTime;
	}
	
	public long weightingBenchmark(int runs)
	{
		running = true;
		long fullWeighting = 0;
		long weightings[] = new long[runs];
		
		for(int r = 0; r < runs; r++)
		{
			if(!cancelled)
			{
				weightings[r] = singleBenchmark();
				fullWeighting += weightings[r];
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
	
	public long singleBenchmark()
	{
		TimerObj add = new TimerObj();
		TimerObj search = new TimerObj();
		
		long addTime = 0;
		long searchTime = 0;
		
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
		
		return addTime + searchTime;
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
