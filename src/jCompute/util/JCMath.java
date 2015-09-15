package jCompute.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
	
	public static double round(double value, int places)
	{
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	public static float round(float value, int places)
	{
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.floatValue();
	}
	
	public static int getNumberOfDecimalPlaces(float val)
	{
		// String constructor required for correct conversion
		BigDecimal bigDecimal = new BigDecimal(String.valueOf(val));
		String string = bigDecimal.stripTrailingZeros().toPlainString();
		int index = string.indexOf(".");
		return index < 0 ? 0 : string.length() - index - 1;
	}
	
	public static int getNumberOfDecimalPlaces(double val)
	{
		// String constructor required for correct conversion
		BigDecimal bigDecimal = new BigDecimal(String.valueOf(val));
		String string = bigDecimal.stripTrailingZeros().toPlainString();
		int index = string.indexOf(".");
		return index < 0 ? 0 : string.length() - index - 1;
	}
}
