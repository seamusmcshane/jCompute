package jCompute.Scenario.Math.Mandelbrot.Lib;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class MandelbrotJavaKernel implements MandelbrotKernelInterface
{	
	private int[] dest;
	
	private BufferedImage image;
	private	int[] imageData;
	
	private int[] pallete;
	private int pSize;
	
	private long count = 0;
	
	public MandelbrotJavaKernel(int width,int height)
	{		
		System.out.println("Java CPU Kernel in use");
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	}

	public void setDest(int[] dest,int[] pallete)
	{
		this.dest = dest;
		this.pallete = pallete;
		pSize = this.pallete.length;
	}
	
	public String getComputeMethodString()
	{
		return "Java Threads";
	}
	
	public long getCount()
	{
		return count;
	}
	
	@Override
	public void ComputeAndCreateImage(double targetX, double targetY, double zoom, int iterations)
	{
		compute(targetX, targetY, zoom, iterations);

		createImage(dest);
	}
	
	@Override
	public void computeMandle(double targetX, double targetY, double zoom, int iterations)
	{
		compute(targetX, targetY, zoom, iterations);	
	}
	
	public int putImage(int[] dest)
	{
		System.arraycopy(imageData, 0, dest, 0, imageData.length);
		return pSize;
	}
	
	public void compute(final double targetX,final double targetY,final double zoom,final int iterations)
	{
		/*int cellSize = 100;
		int cellArea = cellSize*cellSize;
		int cells = (realWidth*realHeight)/cellArea;
		*/
		//System.out.println(cellSize);
		//System.out.println(cells);
		
		int rows = 4;//realWidth/cellSize;
		int columns = 4;//realHeight/cellSize;
		
		//System.out.println("rows : " + rows);
		//System.out.println("columns : " + columns);
		
		int threads = rows*columns;
		
		// System.out.println("threads : " + threads);
		
		int xSplit = (image.getWidth()/columns);
		int ySplit = (image.getHeight()/rows);
		
		//System.out.println("xSplit : " + xSplit);
		//System.out.println("ySplit : " + ySplit);
		
		Thread thread[] = new Thread[threads];
		
		int t=0;
		for(int r=0;r<rows;r++)
		{
			for(int c=0;c<columns;c++)
			{
				thread[t] = new MandleTaskXY(targetX,targetY,zoom,iterations,xSplit*c,xSplit*(c+1),ySplit*r,ySplit*(r+1));				
				t++;
			}			
		}

		for(int i=0;i<threads;i++)
		{
			thread[i].start();
		}
		
		for(int i=0;i<threads;i++)
		{
			try
			{
				thread[i].join();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		count++;

	}

	private void createImage(int[] dest)
	{
		System.arraycopy(imageData, 0, dest, 0, imageData.length);
	}
	
	public void drawMandleXY(double tX, double tY, double zoom, int iterations, int pXmin, int pXmax, int pYmin, int pYmax)
	{		
		double iCX = tX*zoom;
		double iCY = tY*zoom;
		
		for (int pY = pYmin; pY < pYmax; pY++)
		{
			for (int pX = pXmin; pX < pXmax; pX++)
			{
				int iter = iterations;
				
				double cX = (pX-(image.getWidth()/2-iCX)+0.5)/zoom;
				double cY = (pY-(image.getHeight()/2-iCY)+0.5)/zoom;
				
				double zx = 0;
				double zy = 0;
				
				double xTemp =0;
				double yTemp =0;
				
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
				
				while (zx * zx + zy * zy < 256 && iter > 0)
				{
					
					// Cubic
					//xTemp = zx * (zx * zx - zy * zy)  - 2.0 * (zx * zy * zy) + cX;									
					//yTemp = 2.0 * zx*zx*zy +zy * (zx*zx-zy*zy) + cY;
					
					xTemp = zx * zx - zy * zy + cX;
					yTemp = 2.0 * zx * zy + cY;
					
					//Periodicity checking
					if (zx == xTemp  &&  zy == yTemp)
					{
						iter = 0;
						break;
					}
					
					zx = xTemp;
					zy = yTemp;

				    iter--;
				}
				
				if(iter!=0)
				{
					image.setRGB(pX,pY,pallete[(pSize-1)-(iter % pSize)]);
				}
				else
				{
					image.setRGB(pX,pY,0);       
				}
				
			}
		}
		
	}
		
	private class MandleTaskXY extends Thread
	{
		double tX;
		double tY;
		double zoom;
		int iterations;
		int pXmin;
		int pXmax;
		int pYmin;
		int pYmax;
		
		public MandleTaskXY(double tX, double tY, double zoom, int iterations, int pXmin, int pXmax, int pYmin, int pYmax)
		{
			super();
			this.tX = tX;
			this.tY = tY;
			this.zoom = zoom;
			this.iterations = iterations;
			this.pXmin = pXmin;
			this.pXmax = pXmax;
			this.pYmin = pYmin;
			this.pYmax = pYmax;
		}

		public void run()
		{
			drawMandleXY(tX,tY,zoom,iterations,pXmin,pXmax, pYmin,pYmax);			
		}
		
	}

	@Override
	public void destroy()
	{
		// NA	
	}

	@Override
	public void updateBuffers()
	{
		createImage(dest);		
	}
}
