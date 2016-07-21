package jcompute.math;

import jcompute.math.geom.JCVector2f;
import jcompute.math.trig.JCTrig;

public class MathVector2f
{
	private MathVector2f()
	{
		
	}
	
	/**
	 * Returns a the vector resulting from b added to a
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static JCVector2f Added(JCVector2f a, JCVector2f b)
	{
		return new JCVector2f(a.x + b.x, a.y + b.y);
	}
	
	/**
	 * Returns a the vector resulting from b substracted from a
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static JCVector2f Subtracted(JCVector2f a, JCVector2f b)
	{
		return new JCVector2f(a.x - b.x, a.y - b.y);
	}
	
	/**
	 * Returns a copy of the vector multiplied by the scalar
	 * 
	 * @param scalar
	 * @return
	 */
	public static JCVector2f Multiplied(JCVector2f v, float scalar)
	{
		return new JCVector2f(v.x * scalar, v.y * scalar);
	}
	
	/**
	 * Returns a copy of the vector divided by the divisor
	 * 
	 * @param divisor
	 * @return
	 */
	public static JCVector2f Divided(JCVector2f v, float divisor)
	{
		return new JCVector2f(v.x / divisor, v.y / divisor);
	}
	
	/**
	 * Returns a copy of the vector rotated in degrees
	 * 
	 * @param degrees
	 */
	public static JCVector2f Rotated(JCVector2f v, float degrees)
	{
		// float rads = (float) JCTrig.toRadians(degrees);
		// float sine = (float) JCTrig.sinLutInt(rads);
		// float cosine = (float) JCTrig.cosLutInt(rads);
		
		float rads = JCTrig.toRadiansFloat(degrees);
		float sine = (float) JCTrig.sinLutInt(rads);
		float cosine = (float) JCTrig.cosLutInt(rads);
		
		return new JCVector2f((v.x * cosine) - (v.y * sine), (v.x * sine) + (v.y * cosine));
	}
	
	/**
	 * Returns a copy of the vector rotated Clockwise 90 Degrees.
	 * 
	 * @return
	 */
	public static JCVector2f RotatedC90(JCVector2f v)
	{
		return new JCVector2f(-v.y, v.x);
	}
	
	/**
	 * Returns a copy of the vector rotated Counter Clockwise 90 Degrees.
	 * 
	 * @return
	 */
	public static JCVector2f RotatedCC90(JCVector2f v)
	{
		return new JCVector2f(v.y, -v.x);
	}
	
	/**
	 * Returns a copy of the this vectors unit vector;
	 * 
	 * @param vector
	 * @return
	 */
	public static JCVector2f Unit(JCVector2f v)
	{
		float length = v.length();
		
		// Copy
		JCVector2f unitVec = new JCVector2f(v);
		
		if(length > 0)
		{
			unitVec.divide(length);
		}
		
		return unitVec;
	}
	
	/**
	 * Returns the perpendicular vector for this vector;
	 * 
	 * @param vector
	 * @return
	 */
	public static JCVector2f Perpendicular(JCVector2f v)
	{
		return new JCVector2f(-v.y, v.x);
	}
	
	/**
	 * Returns a negated copy of the vector;
	 * 
	 * @param vector
	 * @return
	 */
	public static JCVector2f Negated(JCVector2f v)
	{
		return new JCVector2f(-v.x, -v.y);
	}
	
	/**
	 * Returns the resulting vector from a projected onto b vector
	 * 
	 * @param to
	 * @return
	 */
	public static JCVector2f Projected(JCVector2f a, JCVector2f b)
	{
		JCVector2f projected = b.copy();
		
		// cdp same as len here
		float cpd = projected.dotProduct(projected);
		
		if(0 < cpd)
		{
			float pdp = a.dotProduct(b);
			
			projected.multiply(pdp / cpd);
		}
		
		return projected;
	}
	
	public static boolean VectorsAreEqual(JCVector2f a, JCVector2f b)
	{
		return FloatingPoint.AlmostEqual(a.x - b.x, 0) && FloatingPoint.AlmostEqual(a.y - b.y, 0);
	}
	
	public static boolean VectorsAreParallel(JCVector2f a, JCVector2f b)
	{
		JCVector2f na = MathVector2f.RotatedC90(a);
		
		return FloatingPoint.AlmostEqual(0, na.dotProduct(b));
	}
	
	public static float DistanceSquared(JCVector2f from, JCVector2f to)
	{
		// Do not modify the original vectors
		JCVector2f temp = Subtracted(to, from);
		
		return temp.lengthSquared();
	}
	
	public static float Distance(JCVector2f from, JCVector2f to)
	{
		// Do not modify the original vectors
		JCVector2f temp = Subtracted(from, to);
		
		return temp.length();
	}
}
