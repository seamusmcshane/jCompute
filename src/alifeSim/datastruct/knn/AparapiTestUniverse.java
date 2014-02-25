package alifeSim.datastruct.knn;

import java.util.Random;

import alifeSim.datastruct.knn.TreeBenchmarks.timerObj;
import alifeSimGeom.A2DVector2f;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.OpenCLDevice;
import com.amd.aparapi.OpenCLPlatform;
import com.amd.aparapi.Range;

public class AparapiTestUniverse
{
	final float[]points;
	final int nearestObjectIDs[];
	final float nearestObjectDis[];
	private timerObj statsSearch = new timerObj();

	private ComputeKernel kernel;
	private int numObjects;
	
	private Random r = new Random();
	
	private int steps;
	private int numPoints;
	private int pointSize = 2;
	
	private OpenCLDevice dev;
	
	public AparapiTestUniverse(OpenCLDevice dev, int numObjects,int steps,int area)
	{		
		this.dev = dev;
		this.numObjects = numObjects;
		this.numPoints = numObjects*pointSize;
		
		points = new float[numPoints];

		nearestObjectIDs = new int[numObjects];
		nearestObjectDis = new float[numObjects];
        for (int i=0; i<numObjects; i++)
        {        	
        	nearestObjectIDs[i] = -1;	
        	nearestObjectDis[i] = Float.MAX_VALUE;
        }
        
        for(int p = 0 ;p<numPoints;p++)
        {
        	points[p] =  r.nextFloat()*area;
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
		
		kernel = new ComputeKernel(points,numPoints,pointSize,nearestObjectIDs,nearestObjectDis,numObjects);
		//kernel.setExplicit(true);
		//kernel.setExecutionMode(Kernel.EXECUTION_MODE.GPU);
			
		statsSearch.startTimer();
	
		//for(int i=0;i<steps;i++)
		kernel.execute(range,steps);
			
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

		public ComputeKernel(float[] points, int numPoints,int pointSize, int[] nearestObjectIDs,float nearestObjectDis[],int objectCount)
		{
			this.points = points;
			this.numPoints = numPoints;
			this.pointSize = pointSize;
			
			this.nearestObjectIDs = nearestObjectIDs;
			this.nearestObjectDis = nearestObjectDis;
			this.objectCount = objectCount;
			
		}

		@Override
		public void run()
		{
			int pointId = getGlobalId()*2;
			int resId = getGlobalId();
			
			float minDis = Float.MAX_VALUE;
			int minId = -1;
			float tDis = Float.MAX_VALUE;
			
			float idx = points[pointId+0];
			float idy = points[pointId+1];

			boolean nn = false;
			
			for (int i = 0; i < numPoints; i+=2)
			{
				float ix = points[i+0];
				float iy = points[i+1];
				
				float dx = idx - ix;
				float dy = idy - iy;
				
				tDis =  ( (dx*dx)+(dy*dy) );

				
				/*
				 * 	public float distanceSquared(A2DVector2f other) 
					{
						float dx = other.getX() - getX();
						float dy = other.getY() - getY();
						
						return (float) (dx*dx)+(dy*dy);
					}
				 */
				
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

		}
		
		
	}

	public void output(int i)
	{
		System.out.println(i + "\t" + numObjects + "\t" + statsSearch.getTimeTaken());
		
		/*for (int o = 0; o < nearestObjectIDs.length; o++)
		{
			System.out.println( "ID : " + o + " 1NN :" + nearestObjectIDs[o] + " Dis : " + nearestObjectDis[o]);
		}*/
		
	}

	
	
}

