package jcompute.math.trig;

import org.apache.commons.math3.util.FastMath;

public class JCTrig
{
	// Must be base 2 for fast modulus
	private static final int VALUES = (int) Math.pow(2, 10);
	private static final int VALUESM1 = VALUES - 1;
	private static final int HALF_VALUES = VALUES / 2;
	
	public static final double PI = Math.PI;
	public static final float PI_F = (float) Math.PI;
	
	public static final double HALF_PI = (Math.PI / 2);
	public static final double QUATER_PI = (Math.PI / 4);
	public static final double PI_PLUS_HALF_PI = (3 * (Math.PI / 4));
	public static final double TWO_PI = (2 * Math.PI);
	
	public static final float RAD_TO_DEG = (float) (360 / TWO_PI);
	public static final float DEG_TO_RAD = (float) (TWO_PI / 360);
	
	private static final double[] sineTable;
	private static final double SINE_TABLE_STEP_SIZE = (TWO_PI / VALUES);
	private static final double REV_SINE_TABLE_STEP_SIZE = (VALUES / TWO_PI);
	
	private static final double[] asineTable;
	private static final double A_TABLE_STEP_SIZE = (2.0 / VALUES);
	private static final double REV_A_TABLE_STEP_SIZE = (VALUES / 2.0);
	
	static
	{
		// These lookup tables only get generated if the class is accessed/loaded.
		// Java.Math used for generation
		sineTable = new double[VALUES];
		
		// Min
		sineTable[0] = Math.sin(0);
		
		for(int s = 1; s < VALUESM1; s++)
		{
			sineTable[s] = (Math.sin(SINE_TABLE_STEP_SIZE * s));
		}
		
		// Max
		sineTable[VALUESM1] = Math.sin(TWO_PI);
		
		asineTable = new double[VALUES];
		
		// Min
		asineTable[0] = Math.asin(-1);
		
		for(int s = 1; s < VALUESM1; s++)
		{
			asineTable[s] = Math.asin((A_TABLE_STEP_SIZE * s) - 1.0);
		}
		
		// Max
		asineTable[VALUESM1] = Math.asin(1);
		
		System.out.println("JCTrig Lookup Table Size + " + VALUES);
	}
	
	/**
	 * Returns an approximate sin value using a look up table.
	 * From the direct index look up and the next index linear interpolation is performed.
	 * 
	 * @param x
	 * @return
	 */
	public static double sinLutInt(double x)
	{
		double offset = x * REV_SINE_TABLE_STEP_SIZE;
		int intOffset = (int) Math.floor(offset);
		offset -= intOffset;
		
		int index1 = intOffset & VALUESM1;
		int index2 = (intOffset + 1) & VALUESM1;
		
		double sin1 = sineTable[index1];
		double sin2 = sineTable[index2];
		
		return(((1.0 - offset) * sin1) + (sin2 * offset));
	}
	
	/**
	 * Returns an approximate cos value using a look up table.
	 * From the direct index look up and the next index linear interpolation is performed.
	 * 
	 * @param x
	 * @return
	 */
	public static double cosLutInt(double x)
	{
		return sinLutInt(x + HALF_PI);
	}
	
	/**
	 * Returns an approximate tan value using a look up table.
	 * From the direct index look up and the next index linear interpolation is performed.
	 * 
	 * @param x
	 * @return
	 */
	public static double tanLutInt(double x)
	{
		double cos = (sinLutInt(x + HALF_PI));
		
		// sin/cos
		return (cos == 0) ? cos : sinLutInt(x) / cos;
	}
	
	/**
	 * Returns an approximate asin value using a look up table.
	 * From the direct index look up and the next index linear interpolation is performed.
	 * 
	 * @param x
	 * @return
	 */
	public static double asinLutInt(double x)
	{
		// ASIN does not wrap so we must check the bounds
		// This should return nans for impossible values
		
		// Invalid values given - this also prevents our array index under/overflowing.
		if(x < -1 || x > 1)
		{
			return Double.NaN;
		}
		
		// Use absolute output values for absolute input values
		if(x == 1)
		{
			return HALF_PI;
		}
		
		if(x == -1)
		{
			return -HALF_PI;
		}
		
		// Signed zero is preserved per IEEE754
		if(x == 0)
		{
			return x;
		}
		
		double offset = (x * REV_A_TABLE_STEP_SIZE) + HALF_VALUES;
		int intOffset = (int) Math.floor(offset);
		offset -= intOffset;
		
		int index1 = intOffset;
		int index2 = (intOffset + 1);
		
		// Cannot interpolate +1 - index1 is at the array length
		if(index1 == VALUESM1)
		{
			return asineTable[index1];
		}
		
		double asin1 = asineTable[index1];
		double asin2 = asineTable[index2];
		
		// System.out.println("index 1 + " + index1 + " asin1 " + asin1 + " index2 " + index2 + " asin2 " + asin2);
		
		return(((1.0 - offset) * asin1) + (asin2 * offset));
	}
	
	/**
	 * Returns an approximate acos value using a look up table.
	 * From the direct index look up and the next index linear interpolation is performed.
	 * 
	 * @param x
	 * @return
	 */
	public static double acosLutInt(double x)
	{
		return HALF_PI - asinLutInt(x);
	}
	
	/**
	 * Delegates to Apache FastMath.atan2
	 * 
	 * @param y
	 * @param x
	 * @return
	 */
	public static float atan2Float(float y, float x)
	{
		return (float) FastMath.atan2(y, x);
	}
	
	/**
	 * Delegates to Apache FastMath.atan
	 * 
	 * @param y
	 * @param x
	 * @return
	 */
	public static float atanFloat(float x)
	{
		return (float) FastMath.atan(x);
	}
	
	/**
	 * @param angleDegree
	 * @return
	 */
	public static float toRadiansFloat(float angleDegree)
	{
		return angleDegree * DEG_TO_RAD;
	}
	
	/**
	 * @param angleRadians
	 * @return
	 */
	public static float toDegreesFloat(float angleRadians)
	{
		return angleRadians * RAD_TO_DEG;
	}
}
