package jCompute.Scenario.Math.Mandelbrot;

import java.util.ArrayList;

public class MandelbrotSettings
{
	private ArrayList<Double> coordinates;
	private ArrayList<Double> zoomStartEnds;

	private int textureSize = 256;
	private int iterations = 64;
	private String computeMethod = "Java";
	
	public MandelbrotSettings()
	{
		coordinates = new ArrayList<Double>();
		zoomStartEnds = new ArrayList<Double>();
	}
	
	public void addCoordinates(double coordX,double coordY)
	{
		coordinates.add(coordX);
		coordinates.add(coordY);
		
		System.out.println("Coord("+ coordinates.size()/2 +")" + " : " + coordX + "x" + coordY);
	}
	
	public void addZoom(double zoomStart,double zoomEnd)
	{
		zoomStartEnds.add(zoomStart);
		zoomStartEnds.add(zoomEnd);
		
		System.out.println("Zoom ("+ zoomStartEnds.size()/2 + ") from " + zoomStart +  " to " + zoomEnd);
	}
	
	public double[] getZooms()
	{
		Double [] temp1 = zoomStartEnds.toArray(new Double[zoomStartEnds.size()]);
		
		// Unbox
		double[] temp2 = new double[temp1.length];
		
		for(int i=0;i<temp1.length;i++)
		{
			temp2[i] = temp1[i];
		}
		
		return temp2;
	}
	
	public double[] getCoordiantes()
	{
		Double [] temp1 = coordinates.toArray(new Double[coordinates.size()]);
		
		// Unbox
		double[] temp2 = new double[temp1.length];
		
		for(int i=0;i<temp1.length;i++)
		{
			temp2[i] = temp1[i];
		}
		
		return temp2;
	}

	public String getComputeMethod()
	{
		return computeMethod;
	}

	public void setComputeMethod(String computeMethod)
	{
		this.computeMethod = computeMethod;
	}

	public int getIterations()
	{
		return iterations;
	}

	public void setIterations(int iterations)
	{
		this.iterations = iterations;
	}

	public int getTextureSize()
	{
		return textureSize;
	}

	public void setTextureSize(int textureSize)
	{
		this.textureSize = textureSize;
	}
	
}
