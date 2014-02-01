package alifeSimGeom;

import com.badlogic.gdx.math.Rectangle;

public class A2DRectangle
{
	private Rectangle rectangle;
	
	public A2DRectangle(float x, float y, float width, float height)
	{
		super();
		
		rectangle = new Rectangle(x,y,width,height);
	}
	
	public boolean contains(float x, float y)
	{
		return rectangle.contains(x, y);		
	}
	
	public float getX()
	{
		return rectangle.getX();
	}
	
	public float getY()
	{
		return rectangle.getY();
	}
	
	public float getWidth()
	{
		return rectangle.getWidth();
	}
	
	public float getHeight()
	{
		return rectangle.getWidth();
	}
	
	public void setLocation(float x,float y)
	{
		rectangle.setPosition(x, y);		
	}
	
}
