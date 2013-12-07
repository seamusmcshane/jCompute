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
	static int startObjects = 16;
	
	static int areaSize = 1024;
	
	static ArrayList<TreeBenchObject> objectListGB;
	
	/* Tree 1 */
	static KNNInf<TreeBenchObject> thirdGenObjectKDTreeGB;

	static Random r = new Random();
	
	static SimulationPerformanceStats stats = new SimulationPerformanceStats(null);
	static SimulationPerformanceStats statsOverall = new SimulationPerformanceStats(null);

	static long[][] results;
	
	
	public static void main(String []args)
	{
		int runs = 12;
		
		logger(1,"Benchmark\tThreads\tRuns\tIterations");
		logger(1,"\t\t"+threads+"\t"+runs+"\t"+iterations);

		
		results = new long[runs][3];
		
		int i=0;

		logger(1,"Results\tObjects\tAddTime\t(%)\tSearch Time\t(%)\tTotal Time");

		
		for(i=0;i<runs;i++)
		{		
			//logger(1,"Iteration\t"+i+"\tObjects\t"+(startObjects<<i));
			minList = (( startObjects<<i)/threads);//+1;
			
			if(minList < threads)
			{
				minList = threads;
			}
			
			//logger(1,"Min List" + minList);
			
			objectListGB = new ArrayList<TreeBenchObject>();
			generateObjectList( startObjects<<i,areaSize);
			
			results[i] = benchMarkThirdGenTree();
			
			logger(1, i+ "\t" + (startObjects<<i) + "\t" + results[i][0] + "\t" + results[i][3]+"\t" + results[i][1] + "\t\t" + results[i][4] +"\t" + results[i][2]);

		}

	}
	
	private static void generateObjectList(int num,int size)
	{
		logger(2,"Objects : " + num + " Area : " + size + " " +(new Exception()).getStackTrace()[0].getMethodName());
		
		TreeBenchObject temp;
		for(int i = 0;i< num;i++)
		{
			temp = new TreeBenchObject(i,r.nextInt(size) ,r.nextInt(size));
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
		
		logger(2,">> Start : " + treeName + " " + (new Exception()).getStackTrace()[0].getMethodName());

		long[] time = new long[5];
		
		// Add Time
		time[0] = generateThirdGenTree(objectListGB,thirdGenObjectKDTreeGB,treeName);
		// Bench time
		time[1] = benchTree(objectListGB,thirdGenObjectKDTreeGB,treeName);
		
		// Total Time
		time[2] = time[0]+time[1];
		
		// Add %
		time[3] = (long) (((double)time[0]/(double)time[2])*100);
		time[4] = (long) (((double)time[1]/(double)time[2])*100);
		
		logger(2,"Total Time : " + time[2]);
		logger(2,"Add Time :\t" + time[0] + "\t%"+ time[3]);
		logger(2,"Search Time :\t" + time[1] + "\t%" + time[4]);

		return time;
	}
	

	
	private static long generateThirdGenTree(List<TreeBenchObject> objectList,KNNInf<TreeBenchObject> tree,String treeName)
	{
		logger(2,">> Start : " + treeName + " " + (new Exception()).getStackTrace()[0].getMethodName() + " Objects : " + objectList.size());

		long totaltime = 0;
		int i=0;
		
		while(i<iterations)
		{
			thirdGenObjectKDTreeGB = new thirdGenKDWrapper<TreeBenchObject>(2);
			
			totaltime+=addTree(objectListGB,thirdGenObjectKDTreeGB,treeName);
			
			i++;
		}
		
		logger(2,"<< End :" + treeName + " " + (new Exception()).getStackTrace()[0].getMethodName());
		logger(2,"== Time Taken : " + totaltime + " || (" + (totaltime/1000)%60+"s)");
		
		return totaltime;
	}
	
	private static long addTree(List<TreeBenchObject> objectList,KNNInf<TreeBenchObject> tree,String treeName)
	{	
		logger(3,">> Start : " + treeName + " " + (new Exception()).getStackTrace()[0].getMethodName() + " Objects : " + objectList.size());

		stats.clearSimulationStats();
		stats.setStepStartTime();
		
		
		
		double[] pos;
		
		for (TreeBenchObject currentObject : objectList) 
		{
			pos = new double[2];
			pos[0] = currentObject.getX();
			pos[1] =  currentObject.getY();
			tree.add(pos, currentObject);
		}

		//stats.incrementSimulationSteps();
		
		//stats.calcStepsPerSecond();
		
		stats.setStepEndTime();
		
		logger(3,"<< End :" + treeName + " " + (new Exception()).getStackTrace()[0].getMethodName());
		logger(3,"== Time Taken : " + stats.getTotalTime() + " || (" + (stats.getTotalTime()/1000)%60+"s)");
		return stats.getTotalTime();
	}
		
	public static long benchTree(ArrayList<TreeBenchObject> objectList,KNNInf<TreeBenchObject> tree,String treeName)
	{
		logger(3,">> Start Tree :" + treeName + " " + (new Exception()).getStackTrace()[0].getMethodName());
		stats.clearSimulationStats();
		
		ForkJoinPool pool = new ForkJoinPool(threads);
		PoolTask poolTask;
		int i=0;
		
		stats.setStepStartTime();
		
		while(i<iterations)
		{
			
			poolTask = new PoolTask(objectList, tree,threads);
			pool.invoke(poolTask);			
			
			i++;
		}
		
		stats.setStepEndTime();
		
		int totalSearches = objectList.size() * iterations;
		logger(3,"<< End Tree :" + treeName + " " + (new Exception()).getStackTrace()[0].getMethodName() + " Searches : " + objectList.size() + " Iterations x"+iterations );
		logger(3,"== Time Taken : " + stats.getTotalTime() + " || (" + (stats.getTotalTime()/1000)%60+"s)" + " for : " + totalSearches + " Searches") ;
		logger(3,"== Iterations Per Second " + ((stats.getTotalTime()/1000)%60)/iterations);
		return stats.getTotalTime();
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
