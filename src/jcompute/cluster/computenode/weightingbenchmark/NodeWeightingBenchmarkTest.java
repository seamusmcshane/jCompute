package jcompute.cluster.computenode.weightingbenchmark;

public class NodeWeightingBenchmarkTest
{
	public static void main(String args[])
	{
		final int NUM_OBJECTS = 1024;
		final int ITERATIONS = 10000;
		final int WARM_UP_ITERATIONS = 10000;
		final int NUM_RUNS = 6;

		int bucketSize = 1;
		
		// Warm up
		if(true)
		{
			NodeWeightingBenchmark bench = new NodeWeightingBenchmark(NUM_OBJECTS, ITERATIONS,24);
			bench.warmUp(WARM_UP_ITERATIONS);
			long weighting = bench.weightingBenchmark(NUM_RUNS);
			System.out.println("Test\t" + weighting);
		}
		
		System.out.println("BS\tW" );

		//for(int i=0;i<=64;i++)
		{
			System.out.print(bucketSize);
			NodeWeightingBenchmark bench = new NodeWeightingBenchmark(NUM_OBJECTS, ITERATIONS,32);
			bench.warmUp(0);
			long weighting = bench.weightingBenchmark(NUM_RUNS);
			System.out.print("\t"+weighting);
			System.out.print('\n');
			bucketSize +=2;
		}
	}
	
}
