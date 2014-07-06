package alifeSim.Scenario.Math.Mandelbrot;

public class MandelbrotCoordinate
{
	// Current
	double coordinateX;
	double coordinateY;
	double currentZoom;
	
	// Motion 
	boolean motionIn;
	private double zoomEnd;
	private double zoomStart;
	
	public MandelbrotCoordinate(boolean zoomDirectionIn)
	{
		super();
		this.motionIn = zoomDirectionIn;
	}
	
	public boolean isMotionIn()
	{
		return motionIn;
	}

	public void setMotionIn(boolean motionIn)
	{
		this.motionIn = motionIn;
	}

	public double getCoordinateX()
	{
		return coordinateX;
	}
	
	public void setCoordinateX(double coordinateX)
	{
		this.coordinateX = coordinateX;
	}
	
	public double getCoordinateY()
	{
		return coordinateY;
	}
	
	public void setCoordinateY(double coordinateY)
	{
		this.coordinateY = coordinateY;
	}
	
	public double getCurrentZoom()
	{
		return currentZoom;
	}
	
	public void setCurrentZoom(double currentZoom)
	{
		this.currentZoom = currentZoom;
	}

	public double getZoomEnd()
	{
		return zoomEnd;
	}

	public void setZoomEnd(double zoomEnd)
	{
		this.zoomEnd = zoomEnd;
	}

	public double getZoomStart()
	{
		return zoomStart;
	}

	public void setZoomStart(double zoomStart)
	{
		this.zoomStart = zoomStart;
	}
	
}
