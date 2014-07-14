package jCompute.Gui.View.Graphics;

import java.awt.Color;

public class A2DLine
{
	private float x1;

	private float y1;

	private float x2;

	private float y2;

	private A2RGBA color;
	
	public A2DLine(float x1, float y1, float x2, float y2)
	{
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		color = new A2RGBA(Color.white);
	}

	public A2DLine(float x1, float y1, float x2, float y2, A2RGBA color)
	{
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.setColor(color);
	}
	
	public A2DLine(A2DVector2f vec1, A2DVector2f vec2)
	{
		this.x1 = vec1.getX();
		this.y1 = vec1.getY();
		this.x2 = vec2.getX();
		this.y2 = vec2.getY();
	}

	public float getX1()
	{
		return x1;
	}

	public void setX1(float x1)
	{
		this.x1 = x1;
	}

	public float getY1()
	{
		return y1;
	}

	public void setY1(float y1)
	{
		this.y1 = y1;
	}

	public float getX2()
	{
		return x2;
	}

	public void setX2(float x2)
	{
		this.x2 = x2;
	}

	public float getY2()
	{
		return y2;
	}

	public void setY2(float y2)
	{
		this.y2 = y2;
	}
	
	public float length()
	{
		return (float)Math.sqrt(Math.abs((((x1)-x2)*(x1-x2)) + ((y1-y2)*(y1-y2))));
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
