package jCompute.util;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

public final class OSInfo
{
	private static OSInfo instance;
	
	private final OperatingSystemMXBean mx;
	
	private final int PHYSICAL_MEMORY_SIZE;
	
	private final String OS_NAME;
	private final String SYSTEM_ARCH;
	private final int HW_THREADS;
	
	private OSInfo()
	{
		mx = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		
		OS_NAME = mx.getName();
		SYSTEM_ARCH = mx.getArch();
		HW_THREADS = mx.getAvailableProcessors();
		
		PHYSICAL_MEMORY_SIZE = (int) (mx.getTotalPhysicalMemorySize() / NumericConstants.JDEC.MEGABYTE.byteValue);
	}
	
	/*
	 * ***************************************************************************************************
	 * Instance
	 *****************************************************************************************************/
	
	public synchronized static OSInfo getInstance()
	{
		if(instance == null)
		{
			instance = new OSInfo();
		}
		
		return instance;
	}
	
	/*
	 * ***************************************************************************************************
	 * Static Information
	 *****************************************************************************************************/
	
	public String getOSName()
	{
		return OS_NAME;
	}
	
	public String getSystemArch()
	{
		return SYSTEM_ARCH;
	}
	
	public int getHWThreads()
	{
		return HW_THREADS;
	}
	
	public int getSystemPhysicalMemorySize()
	{
		return PHYSICAL_MEMORY_SIZE;
	}
	
	/*
	 * ***************************************************************************************************
	 * Real-Time Statistics
	 *****************************************************************************************************/
	
	public int getSystemCpuUsage()
	{
		// Converted to 100% and . removed
		return (int) (mx.getSystemCpuLoad() * 100);
	}
	
}
