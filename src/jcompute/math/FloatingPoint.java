package jcompute.math;

public class FloatingPoint
{
	public final static float FLOAT_EPSILON = 1.0f / 8192f;
	
	public static float Abs(float val)
	{
		return (val <= 0) ? 0 - val : val;
	}
	
	public static boolean AlmostEqual(float a, float b)
	{
		return(Math.abs(a - b) < FLOAT_EPSILON);
	}
	
	public static boolean AlmostEqualEpsilon(float a, float b, float e)
	{
		return(Math.abs(a - b) < e);
	}
}
