package jCompute.Gui.View.Graphics;

public class A2DVector2f
{
	private final int X_POS = 0;
	private final int Y_POS = 1;
	
	private float pos[];
	
	public A2DVector2f(float x, float y)
	{
		this.pos = new float[2];
		
		this.pos[X_POS] = x;
		this.pos[Y_POS] = y;
	}
	
	public A2DVector2f(float[] pos)
	{
		this.pos = new float[2];
		
		this.pos[X_POS] = pos[X_POS];
		this.pos[Y_POS] = pos[Y_POS];
	}
	
	public A2DVector2f()
	{
		pos = new float[]
		{
			0, 0
		};
	}
	
	public float getX()
	{
		return pos[X_POS];
	}
	
	public void setX(float x)
	{
		pos[X_POS] = x;
	}
	
	public float getY()
	{
		return pos[Y_POS];
	}
	
	public void setY(float y)
	{
		pos[Y_POS] = y;
	}
	
	public void set(A2DVector2f vector)
	{
		pos[X_POS] = vector.getX();
		pos[Y_POS] = vector.getY();
	}
	
	public A2DVector2f sub(A2DVector2f vector)
	{
		pos[X_POS] -= vector.getX();
		pos[Y_POS] -= vector.getY();
		
		return this;
	}
	
	public A2DVector2f add(A2DVector2f vector)
	{
		pos[X_POS] += vector.getX();
		pos[Y_POS] += vector.getY();
		
		return this;
	}
	
	public A2DVector2f add(float x, float y)
	{
		pos[X_POS] += x;
		pos[Y_POS] += y;
		
		return this;
	}
	
	public void set(float x, float y)
	{
		pos[X_POS] = x;
		pos[Y_POS] = y;
	}
	
	public float[] getArray()
	{
		return pos;
	}
	
}
