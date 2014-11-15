package jCompute.util;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

public class OSInfo
{
	// CPU Usage
	private static OperatingSystemMXBean mx = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	
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
		return (int)(mx.getSystemCpuLoad()*100);
	}
	
	public static int getSystemTotalMemory()
	{
		// Converted to megabytes
		return (int)(mx.getTotalPhysicalMemorySize()/1024/1024);
	}
	
	public static int getSystemFreeMemory()
	{
		// Converted to megabytes
		return (int)(mx.getFreePhysicalMemorySize()/1024/1024);
	}
}
