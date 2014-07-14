package jCompute.Scenario.Math.Mandelbrot.Lib;

import jCompute.Scenario.Math.Mandelbrot.MandelbrotCoordinate;

public class MandelbrotAnimate
{
	private final static Thread thread = Thread.currentThread();
	
	public static int doZoomIn(MandelbrotKernelInterface kernel,double targetX, double targetY, int iterations, double iterInc, double zoomStart, double zoomEnd)
	{
		double zInc = 1.075;
		double iInc = zInc * iterInc;
		double zoom = zoomStart;

		while (zoom < zoomEnd)
		{
			kernel.ComputeAndCreateImage(targetX, targetY, zoom, iterations);

			zoom = zoom * zInc;

			iterations += iInc;
		}

		kernel.ComputeAndCreateImage(targetX, targetY, zoomEnd, iterations);
		
		return iterations;
	}
	
	public static void doZoomInSingleStep(MandelbrotKernelInterface kernel,double targetX, double targetY, int iterations, MandelbrotCoordinate coordinates, double zoomEnd)
	{
		double zInc = 1.075;
		double zoom = coordinates.getCurrentZoom();

		if (zoom < zoomEnd)
		{
			zoom = zoom * zInc;
			
			coordinates.setCurrentZoom(zoom);
		}
		else
		{
			// Finished Zoom
			coordinates.setCurrentZoom(zoomEnd);
			coordinates.setMotionIn(false);
		}		
	}
	
	public static void doZoomOut(MandelbrotKernelInterface kernel,double targetX, double targetY, int iterations, double iterInc, double zoomStart)
	{
		double zInc = 0.75;
		double iInc = zInc * iterInc;
		double zoom = zoomStart;

		while (zoom > MandelbrotConstants.DEFAULT_ZOOM)
		{
			kernel.ComputeAndCreateImage(targetX, targetY, zoom, iterations);

			zoom = zoom * zInc;

			iterations -= iInc;
		}
		
		kernel.ComputeAndCreateImage(targetX, targetY, MandelbrotConstants.DEFAULT_ZOOM, iterations);

	}
	
	public static boolean doZoomOutSingle(MandelbrotKernelInterface kernel,double targetX, double targetY, int iterations, MandelbrotCoordinate coordinates, double zoomEnd)
	{
		double zInc = 0.75;
		double zoom = coordinates.getCurrentZoom();

		if (zoom > zoomEnd)
		{
			zoom = zoom * zInc;
			
			coordinates.setCurrentZoom(zoom);
			
			return false;
		}
		else
		{
			// Finished Zoom
			coordinates.setCurrentZoom(zoomEnd);
		}
		return true;
	}
	
	private static void recenterSingle(MandelbrotKernelInterface kernel,double startX, double startY, MandelbrotCoordinate coordinates, double targetX, double targetY, int iterations, int steps)
	{
		double xStepInc = 0;
		double yStepInc = 0;
		
		if(targetX > coordinates.getCoordinateX())
		{
			xStepInc = Math.abs((startX - targetX) / steps);			
			coordinates.setCoordinateX(coordinates.getCoordinateX() + xStepInc);
			
			if(targetX < coordinates.getCoordinateX())
			{
				coordinates.setCoordinateX(targetX);
			}
		}

		if(targetY > coordinates.getCoordinateY())
		{
			yStepInc = Math.abs((startY - targetY) / steps);
			coordinates.setCoordinateY(coordinates.getCoordinateY() + yStepInc);
			
			if(targetY < coordinates.getCoordinateY())
			{
				coordinates.setCoordinateY(targetY);
			}
			
		}
		
		if(targetX < coordinates.getCoordinateX())
		{
			xStepInc = Math.abs((startX - targetX) / steps);
			coordinates.setCoordinateX(coordinates.getCoordinateX() - xStepInc);
			
			if(targetX > coordinates.getCoordinateX())
			{
				coordinates.setCoordinateX(targetX);
			}
			
		}

		if(targetY < coordinates.getCoordinateY())
		{
			yStepInc = Math.abs((startY - targetY) / steps);
			coordinates.setCoordinateY(coordinates.getCoordinateY() - yStepInc);
			
			if(targetY > coordinates.getCoordinateY())
			{
				coordinates.setCoordinateY(targetY);
			}
		}

	}
	
	private static double[] recenter(MandelbrotKernelInterface kernel,double currentX, double currentY, double targetX, double targetY, double zoom, int iterations, int steps)
	{
		double x[] = new double[steps];
		double y[] = new double[steps];

		double xStepInc = 0;
		double yStepInc = 0;

		if(targetX > currentX)
		{
			xStepInc = Math.abs((currentX - targetX) / steps);
			
			x[0] = currentX + xStepInc;
			for (int i = 1; i < steps; i++)
			{
				x[i] = x[i - 1] + xStepInc;
			}
		}

		if(targetY > currentY)
		{
			yStepInc = Math.abs((currentY - targetY) / steps);

			y[0] = currentY + yStepInc;
			for (int i = 1; i < steps; i++)
			{
				y[i] = y[i - 1] + yStepInc;
			}
		}
		
		if(targetX < currentX)
		{
			xStepInc = Math.abs((currentX - targetX) / steps);
			
			x[0] = currentX - xStepInc;
			for (int i = 1; i < steps; i++)
			{
				x[i] = x[i - 1] - xStepInc;
			}
		}

		if(targetY < currentY)
		{
			yStepInc = Math.abs((currentY - targetY) / steps);

			y[0] = currentY - yStepInc;
			for (int i = 1; i < steps; i++)
			{
				y[i] = y[i - 1] - yStepInc;
			}
		}
		
		for (int i = 0; i < steps; i++)
		{
			kernel.ComputeAndCreateImage(x[i], y[i], zoom, iterations);
		}

		return new double[]
		{
				x[steps - 1], y[steps - 1]
		};
	}
	
	public static void CycleIterations(MandelbrotKernelInterface kernel,int sleep)
	{
		int x = 0;
		int y = 0;
		int iters = 0;
		
		while(iters < 32)
		{
			try
			{
				thread.sleep(sleep);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			iters = iters+1;
			
			kernel.ComputeAndCreateImage(0,0,150,iters);		

		}		
	}
	
	private static void animatePalete(MandelbrotKernelInterface kernel,int[] pallete,int sleep, int numCycles)
	{
		// Palete
		int previous;
		int current;
		
		previous = pallete[MandelbrotConstants.PALETTE_SIZE-1];
		
		for(int a=0;a<(MandelbrotConstants.PALETTE_SIZE*numCycles);a++)
		{
			// Shift Palete
			for(int i=0;i<MandelbrotConstants.PALETTE_SIZE;i++)
			{
				current = pallete[i];
				
				pallete[i] = previous;

				previous = current;
			}
			
			kernel.ComputeAndCreateImage(0,0,150,MandelbrotConstants.PALETTE_SIZE);			

			try
			{
				thread.sleep(sleep);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
	
	public static void animate(MandelbrotKernelInterface kernel,double curX, double curY, double tX, double tY, int iterations)
	{
		recenter(kernel,curX, curY, tX, tY, MandelbrotConstants.DEFAULT_ZOOM, iterations, 30);
		doZoomIn(kernel,tX, tY, iterations, 0, MandelbrotConstants.DEFAULT_ZOOM, MandelbrotConstants.MAX_ZOOM);
		doZoomOut(kernel,tX, tY, iterations, 0, MandelbrotConstants.MAX_ZOOM);
	}
	
	public static boolean animateSingleStep(MandelbrotKernelInterface kernel,double startX, double startY, MandelbrotCoordinate coordinates, double tX, double tY, int iterations)
	{
		if(coordinates.getCoordinateX()!=tX && coordinates.getCoordinateY()!=tY)
		{
			recenterSingle(kernel,startX, startY,coordinates, tX, tY, iterations, 30);
		}
		else
		{
			if(coordinates.isMotionIn())
			{
				doZoomInSingleStep(kernel,tX, tY, iterations, coordinates, coordinates.getZoomEnd());
			}
			else
			{
				if(coordinates.getCurrentZoom()!=coordinates.getZoomStart())
				{
					if(!coordinates.isMotionIn())
					{
						doZoomOutSingle(kernel,tX, tY, iterations, coordinates,coordinates.getZoomStart());
					}
				}
				else
				{
					// Finished
					return true;
				}
			}
		}
		
		return false;
	}
}
