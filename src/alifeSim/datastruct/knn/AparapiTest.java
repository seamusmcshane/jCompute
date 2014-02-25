package alifeSim.datastruct.knn;

import java.util.Random;

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
		
		OpenCLDevice dev = chooseOpenCLDevice();
		
		System.out.println("Run\tObjects\tTime");
		
		for(int i=0;i<runs;i++)
		{
			AparapiTestUniverse universe = new AparapiTestUniverse(dev,numObjects,iterations,areaSize);

			universe.compute();
			
			universe.output(i);
			
			numObjects = numObjects<<1;		

		}
		
		/*System.out.println("Run\tObjects\tTime");
		
		for(int i=0;i<runs;i++)
		{
			AparapiComputeTest2 universe = new AparapiComputeTest2(dev,numObjects,iterations,areaSize);

			universe.compute();
			
			universe.output(i);
			
			numObjects = numObjects<<1;
		}		*/

		System.exit(0);
		
	}
	
	public static OpenCLDevice chooseOpenCLDevice()
	{
		OpenCLDevice dev = OpenCLDevice.select(new OpenCLDevice.DeviceSelector() 
		{
	        public OpenCLDevice select(OpenCLDevice d) 
	        {
	        	//System.out.println("Devices " + d.getPlatform().getDevices());
	        	
	        	OpenCLDevice intel = null;
	        	OpenCLDevice amd = null;
	        	OpenCLDevice nvidia = null;
	        		        	
	        	for(OpenCLPlatform platform : d.getPlatform().getPlatforms())
	        	{
	        		
	        		if(platform.getName().contains("AMD"))
	        		{
	        			for(OpenCLDevice dev : platform.getDevices())
	        			{
	        				if(dev.getType().toString().equals("GPU"))
	        				{
	    	        			System.out.println("AMD GPU Detected");
	    	        			
	    	        			amd = dev;
	        				}
	        			}
	        		}
	        		
	        		if(platform.getName().contains("NVIDIA"))
	        		{
	        			for(OpenCLDevice dev : platform.getDevices())
	        			{
	        				if(dev.getType().toString().equals("GPU"))
	        				{
	    	        			System.out.println("NVIDIA GPU Detected");
	    	        			
	    	        			nvidia = dev;
	        				}
	        			}
	        		}
	        		
	        		if(platform.getName().contains("Intel"))
	        		{
	        			for(OpenCLDevice dev : platform.getDevices())
	        			{
	        				if(dev.getType().toString().equals("GPU"))
	        				{
	    	        			System.out.println("Intel GPU Detected");
	    	        			
	    	        			intel = dev;
	        				}
	        			}
	        		}
	        	}
	        	
	        	if(amd != null)
	        	{
	        		System.out.println("Selected : " + amd.getPlatform().getName() + " " + amd.getType());

	        		return amd;
	        	}
	        	
	        	if(nvidia != null)
	        	{
	        		System.out.println("Selected : " + nvidia.getPlatform().getName() + " " + nvidia.getType());

	        		return nvidia;
	        	}
	        	
	        	if(intel != null)
	        	{
	        		System.out.println("Selected : " + intel.getPlatform().getName() + " " + intel.getType());

	        		return intel;
	        	}	        	
	        	
	        	return null;
		    }
		});
		
		return dev;
	}

}
