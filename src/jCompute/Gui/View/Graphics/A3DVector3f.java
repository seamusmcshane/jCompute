package jCompute.Gui.View.Graphics;

public class A3DVector3f
{
	private float x;
	private float y;
	private float z;
	
	private A2RGBA color;
	
	public A3DVector3f(float x, float y, float z,A2RGBA color)
	{
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.setColor(color);
	}
	
	public A3DVector3f(double x, double y, double z,A2RGBA color)
	{
		super();
		this.x = (float)x;
		this.y = (float)y;
		this.z = (float)z;
		this.setColor(color);
	}
	
	public float getX()
	{
		return x;
	}
	public float getY()
	{
		return y;
	}
	public float getZ()
	{
		return z;
	}

	public A2RGBA getColor()
	{
		return color;
	}

	public void setColor(A2RGBA color)
	{
		this.color = color;
	}
	
}
