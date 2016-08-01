package jcompute.cluster.computenode.weightingbenchmark;

public class NodeWeightingBenchmarkTest
{
	final static int NUM_OBJECTS = 1024;
	final static int ITERATIONS = 10000;
	final static int WARM_UP_ITERATIONS = 10000;
	final static int NUM_RUNS = 6;
	final static int BUCKET_TESTS = 8;
	final static int BUCKET_INC = 8;
	
	public static void main(String args[])
	{
		Ori();
		New();
	}
	
	private static void New()
	{
		int bucketSize = BUCKET_INC;
		
		// Warm up
		if(true)
		{
			NodeWeightingBenchmark2 bench = new NodeWeightingBenchmark2(NUM_OBJECTS, ITERATIONS,24);
			bench.warmUp(WARM_UP_ITERATIONS);
			long weighting = bench.weightingBenchmark(NUM_RUNS);
			System.out.println("Test\t" + weighting);
		}
		
		System.out.println("BS\tW" );

		for(int i=0;i<=BUCKET_TESTS;i++)
		{
			System.out.print(bucketSize);
			NodeWeightingBenchmark2 bench = new NodeWeightingBenchmark2(NUM_OBJECTS, ITERATIONS,32);
			bench.warmUp(0);
			long weighting = bench.weightingBenchmark(NUM_RUNS);
			System.out.print("\t"+weighting);
			System.out.print('\n');
			bucketSize +=BUCKET_INC;
		}
	}
	
	private static void Ori()
	{
		int bucketSize = BUCKET_INC;
		
		// Warm up
		if(true)
		{
			NodeWeightingBenchmark bench = new NodeWeightingBenchmark(NUM_OBJECTS, ITERATIONS,24);
			bench.warmUp(WARM_UP_ITERATIONS);
			long weighting = bench.weightingBenchmark(NUM_RUNS);
			System.out.println("Test\t" + weighting);
		}
		
		System.out.println("BS\tW" );

		for(int i=0;i<=BUCKET_TESTS;i++)
		{
			System.out.print(bucketSize);
			NodeWeightingBenchmark bench = new NodeWeightingBenchmark(NUM_OBJECTS, ITERATIONS,32);
			bench.warmUp(0);
			long weighting = bench.weightingBenchmark(NUM_RUNS);
			System.out.print("\t"+weighting);
			System.out.print('\n');
			bucketSize +=BUCKET_INC;
		}
	}
	
}
