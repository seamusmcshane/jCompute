package alifeSim.datastruct.knn.benchmark;

import java.util.Random;

import alifeSim.Scenario.Math.Mandelbrot.Lib.AparapiUtil;

import com.amd.aparapi.OpenCLDevice;
import com.amd.aparapi.OpenCLPlatform;

public class AparapiTest
{
	static Random r = new Random();
	static int iterations = 100000;
	static int startObjects = 1;
	
	static int areaSize = 1024;
	
	static int runs = 10;
	
	public static void main(String args[])
	{		
		int numObjects = startObjects;
		
		OpenCLDevice dev = AparapiUtil.chooseOpenCLDevice();
		
		System.out.println("Run\tObjects\tTime");
		
		for(int i=0;i<runs;i++)
		{
			AparapiTestUniverse universe = new AparapiTestUniverse(dev,numObjects,iterations,areaSize);
	
			universe.compute();
			
			universe.output(i);
			
			numObjects = numObjects<<1;		
	
		}
	
		System.exit(0);
	
	}

}
