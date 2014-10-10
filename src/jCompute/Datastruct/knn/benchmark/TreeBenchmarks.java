package jCompute.Datastruct.knn.benchmark;

import jCompute.Datastruct.knn.KNNInf;
import jCompute.Datastruct.knn.thirdGenKDWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TreeBenchmarks
{
	static int MaxLogLevel = 1;
	
	static int threadsPerProcessor = 1;
	static int processors = Runtime.getRuntime().availableProcessors();
	static int threads;
	
	static int minList = 1;
	
	static int iterations = 100000;
	static int startObjects = 1;
	
	static int areaSize = 1024;
	
	static ArrayList<TreeBenchObject> objectListGB;
	
	/* Tree 1 */
	static KNNInf<TreeBenchObject> thirdGenObjectKDTreeGB;

	/* Tree 2 */
	static KNNInf<TreeBenchObject> jkMegaKDObjectKDTreeGB;

	static Random r = new Random();
	
	static long[][] results;		
	
	static Scanner scanner;
	
	public static void main(String []args)
	{		
		threads = processors*threadsPerProcessor;
		if(threads == 1)
		{
			// Do not support single threaded operation
			System.out.println("Detected Single Core Processor (Threads Per Processor increased to 2)");
			threadsPerProcessor = 2;
			threads = processors*threadsPerProcessor;
		}
		
		System.out.println("Processors\t: " + processors);
		System.out.println("Threads per Processor\t: " + threadsPerProcessor);
		System.out.println("Total Threads\t: " + threads);
		System.out.println("Max Memory\t: " + Runtime.getRuntime().maxMemory()/1024/1024);
		System.out.println("Total Memory\t: " + Runtime.getRuntime().totalMemory()/1024/1024);
		System.out.println("Avail Memory\t: " + Runtime.getRuntime().freeMemory()/1024/1024);
		System.out.println("Used Memory\t: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024/1024);
		
		scanner = new Scanner(System.in);
		
		System.out.println("Choose Benchmark");
		System.out.println("1 : 5 Runs @ 16384 objects with various list sizes splits from 32 to 1024 at ^2 increments ");
		System.out.println("2: (r) Runs at various object sizes with list size (l)");
		System.out.println("3: Node Weight Benchmark (auto)");
		
		System.out.print("Choose Option : ");
		int option = scanner.nextInt();
		
		if(option == 1)
		{
			startObjects = 16384;
			for(int i=5;i<10;i++)
			{
				minList = 1<<i;
				runThirdGenBench(1);
			}
		}
		else if(option == 2)
		{
		    System.out.print("Enter Max Object Count as 2^n : n=");
		    int runs = scanner.nextInt()+1;
		    System.out.print("Enter min list size : ");
		    minList = scanner.nextInt();
		    runThirdGenBench(runs);
		}
		else if(option == 3)
		{
			System.out.println("Node Benchmark");
			System.out.println("1) Custom Weighting : ");
			System.out.println("2) Node Weighting : ");
		   
			int mode = scanner.nextInt();
			
			
			if(mode == 1)
			{
				System.out.println("Custom Weighting");
			    System.out.print("Number of Objects : ");
			    int objects = scanner.nextInt();
			    System.out.print("Iterations : ");
			    int iterations = scanner.nextInt();
			    NodeWeightingBenchmark bench = new NodeWeightingBenchmark(objects,iterations);
			    
			    System.out.println("Doing Warm up");
			    bench.warmUp(1000);
			    System.out.println("Warm up finished");
			    System.out.println("Benchmarking");
			    bench.singleBenchmark();
			    bench.outputResults();
			}
			else
			{
				NodeWeightingBenchmark bench = new NodeWeightingBenchmark(8192,1000);
				bench.warmUp(1000);				
				System.out.println("Weighting\t " + bench.weightingBenchmark(5));
			}

		}
		else
		{
			System.out.println("Exiting");
			System.exit(-1);;
		}
		
	}
	
	private static void runThirdGenBench(int runs)
	{

		logger(1,"Benchmark\tThreads\tRuns\tIterations");
		logger(1,"\t\t"+threads+"\t"+runs+"\t"+iterations);
		
		results = new long[runs][3];
		
		logger(1, "benchMarkThirdGenTree");
		logger(1,"Run\tObjects\tAddTime\t(%)\tSearch Time\t(%)\tTotal Time\tMin List");

		
		for(int i=0;i<runs;i++)
		{
			//logger(1,"Iteration\t"+i+"\tObjects\t"+(startObjects<<i));

			//logger(1,"Min List" + minList);
			
			generateObjectList( startObjects<<i,areaSize);
			
			System.gc();
			
			results[i] = benchMarkThirdGenTree();
						
			logger(1, i + "\t" + (startObjects<<i) + "\t" + results[i][0] + "\t" + ((float)results[i][3]/100) + "\t" + results[i][1] + "\t\t" + ((float)results[i][4]/100) +"\t" + results[i][2] + "\t\t" + minList);
			
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

		TimerObj statsAdd = new TimerObj();
		TimerObj statsSearch = new TimerObj();
		
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
		time[3] = (long) (((float)time[0]/(float)time[2])*10000);
		time[4] = (long) (((float)time[1]/(float)time[2])*10000);

		
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
		float[] pos;
		
		for (TreeBenchObject currentObject : objectList) 
		{
			pos = new float[2];
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
	
	
	public static class TimerObj
	{
		private long stepStartTime;
		private long stepEndTime;
		private long stepTotalTime;

		public TimerObj()
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
			float[] pos = new float[2];
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
