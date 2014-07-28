package jCompute.Scenario.Math.Mandelbrot.Lib;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.OpenCLDevice;
import com.amd.aparapi.Range;

public class MandelbrotAparapiKernel implements MandelbrotKernelInterface
{
	private ComputeKernel kernel;

	private Range range;
	
	private int[] dest = null;
	
	private long count = 0;
	
	private OpenCLDevice dev;
	
	public MandelbrotAparapiKernel(OpenCLDevice dev, int width,int height)
	{
		System.out.println("OpenCL Kernel in use");
		
		this.dev = dev;
		
		if(dev == null)
		{
			range = Range.create2D(width, height);
		}
		else
		{
			range = dev.createRange2D(width, height);
		}
		
	}
	
	public String getComputeMethodString()
	{
		if(dev!=null)
		{
			return "Aparapi " + dev.getPlatform().getVendor() +  " " + dev.getType();
		}
		
		return "No Dev";
	}
	
	public void setDest(int[] dest,int[] pallete)
	{
		this.dest = dest;
		
		kernel = new ComputeKernel(pallete,dest);
		
		kernel.setExplicit(true);	
	}
	
	public long getCount()
	{
		return count;
	}
	
	public void ComputeAndCreateImage(double targetX, double targetY, double zoom, final int iterations)
	{
		compute(targetX, targetY, zoom, iterations);

		createImage(dest);		
	}
	
	public void computeMandle(double targetX, double targetY, double zoom, int iterations)
	{
		compute(targetX, targetY, zoom, iterations);		
	}
	
	private void compute(double targetX, double targetY,double zoom,int iterations)
	{
		kernel.setValues(targetX,targetY,zoom,iterations);
		//kernel.setExecutionMode(Kernel.EXECUTION_MODE.JTP);
		
		kernel.execute(range);
		
		count++;
		
		//kernel.dispose();
	}
	
	private void createImage(int[] destRGB)
	{
		kernel.get(dest);

		//System.arraycopy(imageData, 0, destRGB, 0, imageData.length);		
	}
	
	private class ComputeKernel extends Kernel
	{		
		private final int dataDest[];
		private final int[] pallete;
		private final int pSize;
		
		private int maxIterations;

		private double zoom;		
		private double iCX = 0;
		private double iCY = 0;
		
		public ComputeKernel(int[] pallete,int dataDest[])
		{
			this.dataDest = dataDest;
			this.pallete = pallete;
			pSize = this.pallete.length;
		}

		public void setValues(double targetX, double targetY, double zoom, int iterations)
		{
			this.maxIterations = iterations;			
			this.zoom = zoom;			
			iCX = targetX*zoom;
			iCY = targetY*zoom;
		}

		@Override
		public void run()
		{
			int gid = getGlobalId(1) * getGlobalSize(0) + getGlobalId(0);
			
			double cX = ( getGlobalId(0)-(getGlobalSize(0)/2-iCX)+0.5)/zoom;
			double cY = ( getGlobalId(1)-(getGlobalSize(1)/2-iCY)+0.5)/zoom;
			
			double zx = 0;
			double zy = 0;

			double xTemp =0;
			
			int iter =  maxIterations;
			
			double cYSq = cY*cY;
			double cX25 = cX-0.25;
			
			// Skip Main Cardoid infinity
			double q = ( (cX25)*(cX25) ) + cYSq;
			
			if(q*(q+(cX25)) < ((0.25)*(cYSq)) )
			{
				iter = 0;
			}
			
			// Skip P2 infinity
			if( ((cX+1)*(cX+1)) + (cYSq) < (0.0625) )
			{
				iter = 0;
			}
			
			while (zx * zx + zy * zy < 256 && iter > 0 )
			{
				// Cubic
				//xTemp = zx * (zx * zx - zy * zy)  - 2.0 * (zx * zy * zy) + cX;
				//zy = 2.0 * zx*zx*zy +zy * (zx*zx-zy*zy) + cY;

				xTemp = zx * zx - zy * zy + cX;
				zy = 2.0f * zx * zy + cY;
				
				zx = xTemp;

			    iter--;
			}
			
			if(iter!=0)
			{
				dataDest[gid] = pallete[(pSize-1)-(iter % pSize)];
			}
			else
			{
				dataDest[gid] = 0;
			}

		}	
		
	}

	@Override
	public void destroy()
	{
		kernel.dispose();
	}

	@Override
	public void updateBuffers()
	{
		createImage(dest);
	}
}

