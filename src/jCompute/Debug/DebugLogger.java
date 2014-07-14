package jCompute.Debug;

public class DebugLogger
{
	private static boolean debug;
	
	
	public static void setDebug(boolean debug)
	{
		DebugLogger.debug = debug;
	}
		
	
	public static void output(String text)
	{
		if(debug)
		{
			System.out.println(text);	
		}
	}
	
}
