package jCompute.util;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

public class OSInfo
{
	private static OperatingSystemMXBean mx = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	private static final int MEGABYTE = 1048576;
	
	public static String getOSName()
	{
		return mx.getName();
	}
	
	public static String getSystemArch()
	{
		return mx.getArch();
	}
	
	public static int getHWThreads()
	{
		return mx.getAvailableProcessors();
	}
	
	public static int getSystemCpuUsage()
	{
		// Converted to 100% and . removed
		return (int) (mx.getSystemCpuLoad() * 100);
	}
	
	public static int getSystemTotalMemory()
	{
		return (int) (mx.getTotalPhysicalMemorySize() / MEGABYTE);
	}
}
