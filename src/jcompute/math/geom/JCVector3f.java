package jcompute.math.geom;

public class JCVector3f
{
	public float x;
	public float y;
	public float z;
	
	/**
	 * Creates a vector from three floats
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public JCVector3f(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Creates a vector from another vector
	 * 
	 * @param vector
	 */
	public JCVector3f(JCVector3f vector)
	{
		this.x = vector.x;
		this.y = vector.y;
		this.z = vector.z;
	}
	
	/**
	 * Adds another vector to this vector
	 * 
	 * @param vector
	 */
	public void add(JCVector3f vector)
	{
		this.x += vector.x;
		this.y += vector.y;
		this.z += vector.z;
	}
	
	public void add(float x, float y, float z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
	}
	
	public void add(JCVector2f v)
	{
		this.x += v.x;
		this.y += v.y;
	}
	
	/**
	 * Subtracts another vector from this vector
	 * 
	 * @param vector
	 */
	public void sub(JCVector3f vector)
	{
		this.x -= vector.x;
		this.y -= vector.y;
		this.z -= vector.z;
	}
	
	public void sub(float x, float y, float z)
	{
		this.x -= x;
		this.y -= y;
		this.z -= z;
	}
	
	/**
	 * Multiplies this vector by the scalar
	 * 
	 * @param scalar
	 */
	public void multiply(float scalar)
	{
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
	}
	
	/**
	 * Divides this vector by the divisor
	 * 
	 * @param divisor
	 */
	public void divide(float divisor)
	{
		this.x /= divisor;
		this.y /= divisor;
		this.z /= divisor;
	}
	
	public float length()
	{
		return (float) Math.sqrt((this.x * this.x) + (this.y * this.y) + (this.z * this.z));
	}
	
	public float lengthSquared()
	{
		return (this.x * this.x) + (this.y * this.y) + (this.z * this.z);
	}
	
	@Override
	public String toString()
	{
		return x + " x " + y + " x " + z;
	}
}
