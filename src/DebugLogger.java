
public class DebugLogger
{
	public static final boolean debug=false;
	
	public static void output(String text)
	{
		if(debug)
		{
			System.out.println(text);	
		}
	}
	
}
