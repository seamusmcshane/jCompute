package alifeSim.datastruct.knn;

import java.util.Random;

import alifeSim.datastruct.knn.TreeBenchmarks.timerObj;
import alifeSimGeom.A2DVector2f;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.OpenCLDevice;
import com.amd.aparapi.OpenCLPlatform;
import com.amd.aparapi.Range;

public class AparapiComputeTest2
{
	final float[]points;
	private int numPoints;
	private int pointSize = 2;

	final int nearestObjectIDs[];	
	final float nearestObjectDis[];
	final int resultsSize;
	
	
	private timerObj statsSearch = new timerObj();

	private ComputeKernel kernel;
	private int numObjects;
	
	private Random r = new Random();
	
	private int steps;
	

	
	private OpenCLDevice dev;
	
	public AparapiComputeTest2(OpenCLDevice dev, int numObjects,int steps,int area)
	{
		this.dev = dev;
		this.numObjects = numObjects;
		this.numPoints = numObjects*pointSize;
		
		this.resultsSize = numObjects*numObjects;
		
		/* Points */
		points = new float[numPoints];
        for(int p = 0 ;p<numPoints;p++)
        {
        	points[p] =  r.nextFloat()*area;
        }
        
		/* 2D array in 1D for id of each objects to each point */
		nearestObjectIDs = new int[resultsSize];
		
		/* 2D array in 1D for dis of each objects to each point */
		nearestObjectDis = new float[resultsSize];
        for (int i=0; i<resultsSize; i++)
        {
        	nearestObjectIDs[i] = -1;	
        	nearestObjectDis[i] = Float.MAX_VALUE;
        }

        this.steps = steps;
	}
	
	public void compute()
	{

		Range range;
		   
		if(dev == null)
		{
			range = Range.create(numObjects);
		}
		else
		{
			range = dev.createRange(numObjects);
		}
			
		//System.out.println(NvidiaDev.getPlatform());
		//System.out.println(NvidiaDev.getMaxComputeUnits());
		//Range range = IntelDev.createRange(num);
		
		kernel = new ComputeKernel(points,numPoints,pointSize,nearestObjectIDs,nearestObjectDis,numObjects,resultsSize);
		//kernel.setExplicit(true);
		//kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
			
		statsSearch.startTimer();
	
		//for(int i=0;i<steps;i++)
		kernel.execute(range,100000);
			
		statsSearch.stopTimer();
		
		kernel.dispose();
	}
	
	public class ComputeKernel extends Kernel
	{
		
		final float[] points;
		final private int numPoints;
		final private int pointSize;
		
		final int nearestObjectIDs[];
		final float nearestObjectDis[];
		final private int objectCount;
		final private int resultsSize;

		public ComputeKernel(float[] points, int numPoints,int pointSize, int[] nearestObjectIDs,float nearestObjectDis[],int objectCount,int resultsSize)
		{
			this.points = points;
			this.numPoints = numPoints;
			this.pointSize = pointSize;
			
			this.nearestObjectIDs = nearestObjectIDs;
			this.nearestObjectDis = nearestObjectDis;
			this.objectCount = objectCount;			
			this.resultsSize = resultsSize;
		}

		@Override
		public void run()
		{
			int pointId = getGlobalId(0)*2;
			int resStart = getGlobalId(0)*objectCount;	// 2d array in 1d (start=0 of array
			
			float minDis = Float.MAX_VALUE;
			int minId = -1;
			float tDis = Float.MAX_VALUE;
			
			float idx = points[pointId+0];
			float idy = points[pointId+1];

			//boolean nn = false;
			
			int resIndex = resStart;
			
			for (int i = 0; i < numPoints; i+=2)
			{
				float ix = points[i+0];
				float iy = points[i+1];
				
				float dx = idx - ix;
				float dy = idy - iy;
				
				nearestObjectIDs[resIndex] = i/2;
				nearestObjectDis[resIndex] = sqrt( (dx*dx)+(dy*dy) );
				resIndex++;
			}
						
			
			/*for (int i = 0; i < numPoints; i+=2)
			{
				float ix = points[i+0];
				float iy = points[i+1];
				
				float dx = idx - ix;
				float dy = idy - iy;
				
				tDis =  ( (dx*dx)+(dy*dy) );
	
				if(tDis < minDis)
				{
					if(i!=pointId)
					{
						minId = i;
						minDis = tDis;
						nn = true;
					}

				}
			}
			
			if(nn)
			{
				nearestObjectIDs[resId] = minId/2;
				nearestObjectDis[resId] = minDis;			
			}
			*/
		}
		
		
	}

	public void output(int i)
	{
		System.out.println(i + "\t" + numObjects + "\t" + statsSearch.getTimeTaken());
		
		for(int o = 0; o<numObjects; o++)
		{			
			for(int res = numObjects*o; res<resultsSize; res++)
			{
				System.out.println( "ID : " + o + " NN :" + nearestObjectIDs[res] + " Dis : " + nearestObjectDis[res]);
			}	
		}
			
		/*for (int o = 0; o < nearestObjectIDs.length; o++)
		{
			System.out.println( "ID : " + o + " 1NN :" + nearestObjectIDs[o] + " Dis : " + nearestObjectDis[o]);
		}*/
		
	}

	
	
}

