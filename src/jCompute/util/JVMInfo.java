package jCompute.util;

public class JVMInfo
{
	private static Runtime runtime = Runtime.getRuntime();
	
	private static final int MEGABYTE = 1048576;
	
	public static int getMaxMemory()
	{
		return (int) (Runtime.getRuntime().maxMemory() / MEGABYTE);
	}
	
	public static int getTotalJVMMemory()
	{
		return (int) (runtime.totalMemory() / MEGABYTE);
	}
	
	public static int getUsedJVMMemory()
	{
		return (int) ((runtime.totalMemory() - runtime.freeMemory()) / MEGABYTE);
	}
	
	public static int getUsedJVMMemoryPercentage()
	{
		long used = runtime.totalMemory() - runtime.freeMemory();
		
		return Math.round((((float) used / (float) Runtime.getRuntime().maxMemory()) * 100));
	}
	
	public static long getFreeJVMMemory()
	{
		long used = runtime.totalMemory() - runtime.freeMemory();
		
		return (int) ((runtime.maxMemory() - used) / MEGABYTE);
	}
	
	public static int getFreeJVMMemoryPercentage()
	{
		long used = runtime.totalMemory() - runtime.freeMemory();
		
		long jvmused = runtime.maxMemory() - used;
		
		return Math.round((((float) jvmused / (float) Runtime.getRuntime().maxMemory()) * 100));
	}
	
}
