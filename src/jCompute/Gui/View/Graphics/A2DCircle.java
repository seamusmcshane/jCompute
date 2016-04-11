package jCompute.gui.view.graphics;

public class A2DCircle
{
	private float x;
	private float y;
	private float radius;
	
	public A2DCircle(float x, float y, float radius)
	{
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	public float getX()
	{
		return x;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public float getY()
	{
		return y;
	}

	public void setY(float y)
	{
		this.y = y;
	}

	public float getRadius()
	{
		return radius;
	}

	public void setRadius(float radius)
	{
		this.radius = radius;
	}

	public void setLocation(float x, float y)
	{
		setX(x);	
		setY(y);
	}

}
