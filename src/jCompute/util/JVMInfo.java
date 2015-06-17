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
	
	public static String getJVMVersion()
	{
		return System.getProperty("java.version");
	}
	
	public static String getJVMName()
	{
		return System.getProperty("java.vm.name");
	}
	
	public static String getJVMInfoString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(getJVMName());
		builder.append(' ');
		builder.append(getJVMVersion());
		builder.append(" (");
		builder.append("Max Mem:");
		builder.append(getMaxMemory());
		builder.append('|');
		builder.append("Total Mem:");
		builder.append(getTotalJVMMemory());
		builder.append('|');
		builder.append("Used Mem:");
		builder.append(getUsedJVMMemory());
		builder.append(')');
		
		return builder.toString();
	}
	
}
