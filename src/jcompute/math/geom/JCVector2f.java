package jcompute.math.geom;

import jcompute.math.FloatingPoint;
import jcompute.math.trig.JCTrig;

public class JCVector2f
{
	public float x;
	public float y;
	
	/**
	 * Creates a vector from two floats
	 * 
	 * @param x
	 * @param y
	 */
	public JCVector2f(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Creates a vector from another vector
	 * 
	 * @param vector
	 */
	public JCVector2f(JCVector2f vector)
	{
		this.x = vector.x;
		this.y = vector.y;
	}
	
	/**
	 * Adds another vector to this vector
	 * 
	 * @param vector
	 */
	public void add(JCVector2f vector)
	{
		this.x += vector.x;
		this.y += vector.y;
	}
	
	public void add(float x, float y)
	{
		this.x += x;
		this.y += y;
	}
	
	/**
	 * Subtracts another vector from this vector
	 * 
	 * @param vector
	 */
	public void sub(JCVector2f vector)
	{
		this.x -= vector.x;
		this.y -= vector.y;
	}
	
	public void sub(float x, float y)
	{
		this.x -= x;
		this.y -= y;
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
	}
	
	/**
	 * Rotates this vector in degrees
	 * 
	 * @param degrees
	 */
	public void rotate(float degrees)
	{
		// float rads = (float) JCTrig.toRadians(degrees);
		// float sine = (float) JCTrig.sinLutInt(rads);
		// float cosine = (float) JCTrig.cosLutInt(rads);
		
		float rads = JCTrig.toRadiansFloat(degrees);
		float sine = (float) JCTrig.sinLutInt(rads);
		float cosine = (float) JCTrig.cosLutInt(rads);
		
		float tx = (this.x * cosine) - (this.y * sine);
		y = (this.x * sine) + (this.y * cosine);
		x = tx;
	}
	
	/**
	 * Returns the angle between this and another vector
	 * 
	 * @param b
	 * @return
	 */
	public float enclosedAngle(JCVector2f b)
	{
		// Dot product of two unit vectors
		float dp = MathVector2f.Unit(this).dotProduct(MathVector2f.Unit(b));
		
		// Ensure DP is in the acos valid range.
		// Cases such as when locations one or both vectors are close to 0
		
		if(dp > 1)
		{
			dp = 1;
		}
		
		if(dp < -1)
		{
			dp = -1;
		}
		
		return JCTrig.toDegreesFloat((float) JCTrig.acosLutInt(dp));
	}
	
	public float angleTo(JCVector2f b)
	{
		float dp = (x * b.x) + (y * b.y);
		float pdp = (x * b.y) - (y * b.x);
		
		return JCTrig.atan2Float(pdp, dp);
	}
	
	public float angleToAltSigned(JCVector2f b)
	{
		JCVector2f v1u = MathVector2f.Unit(this);
		JCVector2f v2u = MathVector2f.Unit(b);
		
		float dp = v1u.dotProduct(v2u);
		float angle = (float) JCTrig.acosLutInt(dp);
		
		JCVector2f v1Perp = MathVector2f.RotatedCC90(v1u);
		
		float pdp = v1Perp.dotProduct(v2u);
		
		if(pdp > 0)
		{
			angle = -1f * angle;
		}
		
		return JCTrig.toDegreesFloat(angle);
	}
	
	/**
	 * Returns the heading to another vector.
	 * 
	 * @param b
	 * @return
	 */
	public float headingTo(JCVector2f b)
	{
		// This trick avoids the need for checking negative degrees and wrapping 360 using modulus
		float bearing = JCTrig.toDegreesFloat(JCTrig.atan2Float(-y - b.y, -x - b.x));
		
		return bearing + 180;
	}
	
	/**
	 * Returns the heading to another vector.
	 * (Reference for headingTo)
	 * 
	 * @param b
	 * @return
	 */
	public float headingToRef(JCVector2f b)
	{
		float bearing = JCTrig.toDegreesFloat(JCTrig.atan2Float(y - b.y, x - b.x));
		
		if(bearing < 0)
		{
			bearing += 360;
		}
		
		return bearing % 360;
	}
	
	/**
	 * Fast Clockwise 90 Degree rotation
	 * 
	 * @return
	 */
	public void rotateC90()
	{
		x = -y;
		y = x;
	}
	
	/**
	 * Fast Counter Clockwise 90 Degree rotation
	 * 
	 * @return
	 */
	public void rotateCC90()
	{
		x = y;
		y = -x;
	}
	
	public float length()
	{
		return (float) Math.sqrt((this.x * this.x) + (this.y * this.y));
	}
	
	public float lengthSquared()
	{
		return (this.x * this.x) + (this.y * this.y);
	}
	
	public float dotProduct(JCVector2f b)
	{
		return (this.x * b.x) + (this.y * b.y);
	}
	
	// public float perpendicularDotProduct(JCVector2f b)
	// {
	// return perpendicular().dotProduct(b);
	// }
	
	public float perpendicularDotProduct(JCVector2f b)
	{
		return -y * b.x + x * b.y;
	}
	
	public float determinant(JCVector2f b)
	{
		return (this.x * b.y) - (this.y * b.x);
	}
	
	/**
	 * Negates this vector;
	 * 
	 * @param vector
	 * @return
	 */
	public void negate()
	{
		x = -x;
		y = -y;
	}
	
	public boolean equals(JCVector2f b)
	{
		return(FloatingPoint.AlmostEqual(x, b.x) && FloatingPoint.AlmostEqual(y, b.y));
	}
	
	public boolean equals(float x, float y)
	{
		return(FloatingPoint.AlmostEqual(this.x, x) && FloatingPoint.AlmostEqual(this.y, y));
	}
	
	public JCVector2f copy()
	{
		return new JCVector2f(this);
	}
	
	@Override
	public String toString()
	{
		return x + " x " + y;
	}
	
}