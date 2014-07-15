package jCompute.Scenario.Math.Mandelbrot.Lib;

public interface MandelbrotKernelInterface
{	
	public void setDest(int[] dest,int[] pallete);
	public void ComputeAndCreateImage(double targetX, double targetY, double zoom, final int iterations);
	public void computeMandle(double targetX, double targetY, double zoom, int iterations);

	public String getComputeMethodString();
	
	
	public long getCount();
	public void destroy();
	public void updateBuffers();
}
