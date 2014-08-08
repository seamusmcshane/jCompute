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
	
	public static void outputString(String text)
	{
		if(debug)
		{
			System.out.print(text);	
		}
	}
	
	public static void outputChar(char c)
	{
		if(debug)
		{
			System.out.print(c);	
		}
	}
	
}
