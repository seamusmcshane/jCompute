package alifeSimGeom;

public class A3DVector3f
{
	private float x;
	private float y;
	private float z;
	
	public A3DVector3f(float x, float y, float z)
	{
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public A3DVector3f(double x, double y, double z)
	{
		super();
		this.x = (float)x;
		this.y = (float)y;
		this.z = (float)z;
	}
	
	public float getX()
	{
		return x;
	}
	public float getY()
	{
		return y;
	}
	public float getZ()
	{
		return z;
	}
	
}
