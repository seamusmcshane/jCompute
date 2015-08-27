package jCompute.Gui.View.Graphics;

import java.awt.Color;

public class A2DRectangle
{
	private float[] coords;
	private float width;
	private float height;
	private A2RGBA color;
		
	public A2DRectangle(float x, float y, float width, float height)
	{
		super();

		this.width = width;
		this.height = height;
		
		setColor(new A2RGBA(Color.white));
		
		setLocation(x,y);
	}
	
	public A2DRectangle(float x, float y, float width, float height,A2RGBA color)
	{
		super();

		this.width = width;
		this.height = height;
		
		this.setColor(color);
		
		setLocation(x,y);
	}

	public boolean contains(float[] pos)
	{
		// outside left wall?
		if(pos[0]<coords[0])
		{
			return false;
		}
		
		// outside bottom wall
		if(pos[1]<coords[1])
		{
			return false;
		}
		
		// outside right wall
		if(pos[0]>coords[2])
		{
			return false;
		}
		
		if(pos[1]>coords[3])
		{
			return false;
		}
		
		return true;	
	}
	
	public boolean contains(double[] pos)
	{
		// outside left wall?
		if(pos[0]<coords[0])
		{
			return false;
		}
		
		// outside bottom wall
		if(pos[1]<coords[1])
		{
			return false;
		}
		
		// outside right wall
		if(pos[0]>coords[2])
		{
			return false;
		}
		
		if(pos[1]>coords[3])
		{
			return false;
		}
		
		return true;	
	}
	
	public float getX()
	{
		return coords[0];
	}
	
	public float getY()
	{
		return coords[1];
	}
	
	public float getWidth()
	{
		return width;
	}
	
	public float getHeight()
	{
		return height;
	}
	
	public void setLocation(float x,float y)
	{
		coords = new float[4];
		coords[0] = x;
		coords[1] = y;
		coords[2] = x+width;
		coords[3] = y+height;		
	}

	public A2RGBA getColor()
	{
		return color;
	}

	public void setColor(A2RGBA color)
	{
		this.color = color;
	}
	
	public float getAxisMin(int axis)
	{
		if(axis == 0)
		{
			return getXAxisMin();
		}
		else
		{
			return getYAxisMin();
		}
	}
	
	public float getAxisMax(int axis)
	{
		if(axis == 0)
		{
			return getXAxisMax();
		}
		else
		{
			return getYAxisMax();
		}
	}
	
	public float getXAxisMin()
	{
		if(coords[0]<coords[2])
		{
			return coords[0];
		}
		else
		{
			return coords[2];

		}		
	}
	
	public float getYAxisMin()
	{
		if(coords[1]<coords[3])
		{
			return coords[1];
		}
		else
		{
			return coords[3];
		}		
	}

	public float getXAxisMax()
	{
		if(coords[0]>coords[2])
		{
			return coords[0];
		}
		else
		{
			return coords[2];

		}		
	}
	
	public float getYAxisMax()
	{
		if(coords[1]>coords[3])
		{
			return coords[1];
		}
		else
		{
			return coords[3];
		}		
	}
}
