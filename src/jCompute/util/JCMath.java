package jCompute.util;

public class JCMath
{
	public static float distance(float[] from, float[] to)
	{
		return (float) Math.sqrt(distanceSquared(from, to));
	}
	
	public static float distanceSquared(float[] from, float[] to)
	{
		return ((to[0] - from[0]) * (to[0] - from[0])) + ((to[1] - from[1]) * (to[1] - from[1]));
	}
	
	public static double distance(double[] from, double[] to)
	{
		return Math.sqrt(distanceSquared(from, to));
	}
	
	public static double distanceSquared(double[] from, double[] to)
	{
		return ((to[0] - from[0]) * (to[0] - from[0])) + ((to[1] - from[1]) * (to[1] - from[1]));
	}
	
	public static double distance(double fromX, double fromY, double toX, double toY)
	{
		return Math.sqrt(distanceSquared(fromX, fromY, toX, toY));
	}
	
	public static double distanceSquared(double fromX, double fromY, double toX, double toY)
	{
		return ((toX - fromX) * (toX - fromX)) + ((toY - fromY) * (toY - fromY));
	}
	
	public static float distance(float fromX, float fromY, float toX, float toY)
	{
		return (float) Math.sqrt(distanceSquared(fromX, fromY, toX, toY));
	}
	
	public static float distanceSquared(float fromX, float fromY, float toX, float toY)
	{
		return ((toX - fromX) * (toX - fromX)) + ((toY - fromY) * (toY - fromY));
	}
}
