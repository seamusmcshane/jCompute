package jCompute.gui.view.graphics;

public class A3DVector3f
{
	private final int X_POS = 0;
	private final int Y_POS = 1;
	private final int Z_POS = 2;
	
	private float[] pos;
	
	public A3DVector3f()
	{
		this.pos = new float[3];
		
		this.pos[X_POS] = 0;
		this.pos[Y_POS] = 0;
		this.pos[Z_POS] = 0;
	}
	
	public A3DVector3f(float x, float y, float z)
	{
		this.pos = new float[3];
		
		this.pos[X_POS] = x;
		this.pos[Y_POS] = y;
		this.pos[Z_POS] = z;
	}
	
	public A3DVector3f(double x, double y, double z)
	{
		this((float) x, (float) y, (float) z);
	}
	
	public A3DVector3f(float[] pos)
	{
		this.pos = new float[3];
		
		this.pos[X_POS] = pos[X_POS];
		this.pos[Y_POS] = pos[Y_POS];
		this.pos[Z_POS] = pos[Z_POS];
	}
	
	public A3DVector3f(double[] pos)
	{
		this.pos = new float[3];

		this.pos[X_POS] = (float) pos[X_POS];
		this.pos[Y_POS] = (float) pos[Y_POS];
		this.pos[Z_POS] = (float) pos[Z_POS];
	}
	
	public float getX()
	{
		return pos[X_POS];
	}
	
	public float getY()
	{
		return pos[Y_POS];
	}
	
	public float getZ()
	{
		return pos[Z_POS];
	}
	
	public float distanceSquared(A3DVector3f other)
	{
		float dx = other.getX() - getX();
		float dy = other.getY() - getY();
		float dz = other.getZ() - getZ();
		
		return (dx * dx) + (dy * dy) + (dz * dz);
	}
	
	public float distanceSquared(float[] other)
	{
		float dx = other[X_POS] - pos[X_POS];
		float dy = other[Y_POS] - pos[Y_POS];
		float dz = other[Z_POS] - pos[Z_POS];
		
		return (dx * dx) + (dy * dy) + (dz * dz);
	}
	
}
