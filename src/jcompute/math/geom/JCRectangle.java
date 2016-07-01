package jcompute.math.geom;

public class JCRectangle
{
	public JCVector2f origin;
	public JCVector2f size;
	
	public JCRectangle(JCVector2f origin, JCVector2f size)
	{
		this.origin = origin;
		this.size = size;
	}
	
	public JCRectangle(float ox, float oy, float sx, float sy)
	{
		this.origin = new JCVector2f(ox, oy);
		this.size = new JCVector2f(sx, sy);
	}
	
	@Override
	public String toString()
	{
		return origin.x + " x " + origin.y + " " + size.x + " x " + size.y;
	}
}
