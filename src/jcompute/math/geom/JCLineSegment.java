package jcompute.math.geom;

public class JCLineSegment
{
	public JCVector2f start;
	public JCVector2f end;
	
	public JCLineSegment(JCVector2f start, JCVector2f end)
	{
		this.start = start;
		this.end = end;
	}
	
	public JCLineSegment(float x1, float y1, float x2, float y2)
	{
		start = new JCVector2f(x1, y1);
		end = new JCVector2f(x2, y2);
	}
	
	@Override
	public String toString()
	{
		return start.x + " x " + start.y + " " + end.x + " x " + end.y;
	}
}
