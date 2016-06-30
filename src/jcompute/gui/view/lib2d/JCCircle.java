package jcompute.gui.view.lib2d;

public class JCCircle
{
	public JCVector2f center;
	public float radius;
	
	public JCCircle(JCVector2f center, float radius)
	{
		this.center = center;
		this.radius = radius;
	}
	
	public JCCircle(float cx, float cy, float radius)
	{
		this.center = new JCVector2f(cx, cy);
		this.radius = radius;
	}
	
	@Override
	public String toString()
	{
		return center.x + " x " + center.y + " x " + radius;
	}
}
