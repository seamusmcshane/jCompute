package alifeSim.datastruct.knn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import alifeSim.Simulation.SimulationPerformanceStats;

public class TreeBenchmarks
{
	static int MaxLogLevel = 1;
	
	static int threads = Runtime.getRuntime().availableProcessors()*Runtime.getRuntime().availableProcessors();
	
	static int minList = 0;
	
	static int iterations = 100000;
	static int startObjects = 1;
	
	static int areaSize = 1024;
	
	static ArrayList<TreeBenchObject> objectListGB;
	
	/* Tree 1 */
	static KNNInf<TreeBenchObject> thirdGenObjectKDTreeGB;

	static Random r = new Random();
	
	static long[][] results;		
	
	public static void main(String []args)
	{
		if(threads==1)
		{
			threads=2;
		}
		
		
		int runs = 19;
		
		logger(1,"Benchmark\tThreads\tRuns\tIterations");
		logger(1,"\t\t"+threads+"\t"+runs+"\t"+iterations);
		
		results = new long[runs][3];
		
		int i=0;

		logger(1,"Run\tObjects\tAddTime\t(%)\tSearch Time\t(%)\tTotal Time");

		
		for(i=0;i<runs;i++)
		{		
			//logger(1,"Iteration\t"+i+"\tObjects\t"+(startObjects<<i));
			minList = (( startObjects<<i)/threads);//+1;
			
			if(minList < threads)
			{
				minList = threads;
			}
			
			//logger(1,"Min List" + minList);
			
			generateObjectList( startObjects<<i,areaSize);
			
			System.gc();
			
			results[i] = benchMarkThirdGenTree();
			
			logger(1, i+ "\t" + (startObjects<<i) + "\t" + results[i][0] + "\t" + ((double)results[i][3]/100) + "\t" + results[i][1] + "\t\t" + ((double)results[i][4]/100) +"\t" + results[i][2]);

		}

	}
	
	private static void generateObjectList(int num,int size)
	{
		logger(2,"Objects : " + num + " Area : " + size + " " +(new Exception()).getStackTrace()[0].getMethodName());
		
		objectListGB = new ArrayList<TreeBenchObject>();
		
		TreeBenchObject temp;
		for(int i = 0;i< num;i++)
		{
			temp = new TreeBenchObject(i,r.nextInt(size) ,r.nextInt(size));
			//logger(1,"temp" + temp.getX() + " " + temp.getY());
			objectListGB.add(temp);	
		}
	}
	
	
	
	private static void logger(int level,String string)
	{
		if(level<=MaxLogLevel)
		{
			System.out.println(string);	
		}		
	}
	
	private static long[] benchMarkThirdGenTree()
	{
		String treeName = "thirdGenObjectKDTreeGB";
		
		ForkJoinPool pool = new ForkJoinPool(threads);
		
		logger(2,">> Start : " + treeName + " " + (new Exception()).getStackTrace()[0].getMethodName());

		timerObj statsAdd = new timerObj();
		timerObj statsSearch = new timerObj();
		
		long[] time = new long[5];
		
		int i=0;
		
		while(i<iterations)
		{
			// Add Time
			statsAdd.resetTimer();
			statsAdd.startTimer();
				generateThirdGenTree(objectListGB,thirdGenObjectKDTreeGB,treeName);
			statsAdd.stopTimer();
			time[0]+=statsAdd.getTimeTaken();	
					
			// Bench time
			statsSearch.resetTimer();
			statsSearch.startTimer();
				searchTree(pool,objectListGB,thirdGenObjectKDTreeGB,treeName);
			statsSearch.stopTimer();
			time[1]+=statsSearch.getTimeTaken();
			
			i++;
		}	
		// Total Time
		time[2] += (time[0]+time[1]);
		
		// Add %
		time[3] = (long) (((double)time[0]/(double)time[2])*10000);
		time[4] = (long) (((double)time[1]/(double)time[2])*10000);

		
		logger(2,"Total Time : " + time[2]);
		logger(2,"Add Time :\t" + time[0] + "\t%"+ time[3]);
		logger(2,"Search Time :\t" + time[1] + "\t%" + time[4]);

		return time;
	}
	

	
	private static void generateThirdGenTree(List<TreeBenchObject> objectList,KNNInf<TreeBenchObject> tree,String treeName)
	{
		thirdGenObjectKDTreeGB = new thirdGenKDWrapper<TreeBenchObject>(2);	
		addTree(objectListGB,thirdGenObjectKDTreeGB,treeName);
	}
	
	private static void addTree(List<TreeBenchObject> objectList,KNNInf<TreeBenchObject> tree,String treeName)
	{	
		double[] pos;
		
		for (TreeBenchObject currentObject : objectList) 
		{
			pos = new double[2];
			pos[0] = currentObject.getX();
			pos[1] =  currentObject.getY();
			tree.add(pos, currentObject);
		}
	}
		
	public static void searchTree(ForkJoinPool pool,ArrayList<TreeBenchObject> objectList,KNNInf<TreeBenchObject> tree,String treeName)
	{
		PoolTask poolTask = new PoolTask(objectList, tree,threads);
		
		pool.invoke(poolTask);			
	}
	
	
	public static class timerObj
	{
		private long stepStartTime;
		private long stepEndTime;
		private long stepTotalTime;

		public timerObj()
		{
			
		}
		
		public void startTimer()
		{
			stepStartTime = System.currentTimeMillis(); // Start time for the average step		
		}
		
		public void stopTimer()
		{
			stepEndTime = System.currentTimeMillis();
		}
		
		public long getTimeTaken()
		{
			return stepEndTime - stepStartTime;
		}
		
		public void resetTimer()
		{
			stepTotalTime=0;
		}
	}
	
	/*
	 * Private Thread Pool Task class
	 */
	public static class PoolTask extends RecursiveAction
	{
		private static final long serialVersionUID = -7561532113121448135L;
		
		private List<TreeBenchObject> objectList;
		private KNNInf<TreeBenchObject> objectKDTree;
		
		public PoolTask(List<TreeBenchObject> objectList, KNNInf<TreeBenchObject> objectKDTree,int threads)
		{
			this.objectList = objectList;
			this.objectKDTree = objectKDTree;
		}
		
		private PoolTask(List<TreeBenchObject> objectList, KNNInf<TreeBenchObject> objectKDTree)
		{
			this.objectList = objectList;
			this.objectKDTree = objectKDTree;
		}
		
		@Override
		protected void compute()
		{
			//logger((new Exception()).getStackTrace()[0].getMethodName());

		    if (objectList.size() < minList) 
		    {
		    	performComputation();
		        return;
		    }
		    
		    int split = objectList.size() / 2;

		    invokeAll(new PoolTask(objectList.subList(0,split), objectKDTree),new PoolTask(objectList.subList(split,objectList.size()), objectKDTree));

		}
		
		private void performComputation()
		{
			double[] pos = new double[2];
			TreeBenchObject nearestObject;
			for (TreeBenchObject currentObject : objectList) 
			{
				//System.out.println(currentObject.getId());
				pos[0] = currentObject.getX();
				pos[1] = currentObject.getY();
				nearestObject = objectKDTree.nearestNeighbour(pos);
				
				currentObject.setNearestObject(nearestObject);
								
			}		
		}
	}
		
}
