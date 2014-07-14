package jCompute.Gui.View.Graphics;

public class A2DPoint2d
{
	float x;
	float y;
	A2RGBA color;
	
	public A2DPoint2d(float x, float y, A2RGBA color)
	{
		super();
		this.x = x;
		this.y = y;
		this.color = color;
	}
	
	public A2DPoint2d(float x, float y)
	{
		super();
		this.x = x;
		this.y = y;
	}
	
	public A2DPoint2d(double x, double y, A2RGBA color)
	{
		super();
		this.x = (float)x;
		this.y = (float)y;
		this.color = color;
	}

	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	public A2RGBA getColor()
	{
		return color;
	}
	
	public void setColor(A2RGBA color)
	{
		this.color =  color;
	}
	
	
}
