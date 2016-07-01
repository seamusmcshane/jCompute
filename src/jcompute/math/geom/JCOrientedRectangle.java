package jcompute.math.geom;

public class JCOrientedRectangle
{
	public JCVector2f center;
	public JCVector2f halfExtend;
	public float rotation;
	
	public JCOrientedRectangle(JCVector2f center, JCVector2f halfExtend, float rotation)
	{
		this.center = center;
		this.halfExtend = halfExtend;
		this.rotation = rotation;
	}
}
