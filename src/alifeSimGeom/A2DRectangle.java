package alifeSimGeom;

public class A2DRectangle
{
	private float x;
	private float y;
	private float x2;
	private float y2;
	private float width;
	private float height;
	
	public A2DRectangle(float x, float y, float width, float height)
	{
		super();

		this.width = width;
		this.height = height;
		
		setLocation(x,y);
	}

	public boolean contains(float x, float y)
	{
		// outside left wall?
		if(x<this.x)
		{
			return false;
		}
		
		// outside bottom wall
		if(y<this.y)
		{
			return false;
		}
		
		// outside right wall
		if(x>x2)
		{
			return false;
		}
		
		if(y>y2)
		{
			return false;
		}
		
		return true;	
	}
	
	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
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
		this.x = x;
		this.y = y;
		
		this.x2 = x+width;
		this.y2 = y+height;		
	}
	
}
