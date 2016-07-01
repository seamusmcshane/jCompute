package jcompute.math.geom;

public class JCLineInfinite
{
	public JCVector2f base;
	public JCVector2f direction;
	
	public JCLineInfinite(JCVector2f base, JCVector2f direction)
	{
		this.base = base;
		this.direction = direction;
	}
	
	public JCLineInfinite(float bx, float by, float dx, float dy)
	{
		base = new JCVector2f(bx, by);
		direction = new JCVector2f(dx, dy);
	}
	
	@Override
	public String toString()
	{
		return base.x + " x " + base.y + " " + direction.x + " x " + direction.y;
	}
}
