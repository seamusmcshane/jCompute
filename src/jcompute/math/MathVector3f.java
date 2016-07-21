package jcompute.math;

import jcompute.math.geom.JCVector3f;

public class MathVector3f
{
	/**
	 * Returns a copy of the this vectors unit vector;
	 * 
	 * @param vector
	 * @return
	 */
	public static JCVector3f Unit(JCVector3f v)
	{
		float length = v.length();
		
		// Copy
		JCVector3f unitVec = new JCVector3f(v);
		
		if(length > 0)
		{
			unitVec.divide(length);
		}
		
		return unitVec;
	}
}
