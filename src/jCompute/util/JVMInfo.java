package jCompute.util;

public final class JVMInfo
{
	private static JVMInfo instance;
	
	private final Runtime runtime;
	
	// Non runtime changing JVM info
	private final String JVM_NAME;
	private final String JVM_VER;
	
	// The max memory the JVM will ever try to use
	private final long MAX_MEMORY;
	
	private JVMInfo()
	{
		runtime = Runtime.getRuntime();
		
		JVM_NAME = System.getProperty("java.vm.name");
		JVM_VER = System.getProperty("java.version");
		
		MAX_MEMORY = Runtime.getRuntime().maxMemory();
	}
	
	/*
	 * ***************************************************************************************************
	 * Instance
	 *****************************************************************************************************/
	
	public synchronized static JVMInfo getInstance()
	{
		if(instance == null)
		{
			instance = new JVMInfo();
		}
		
		return instance;
	}
	
	/*
	 * ***************************************************************************************************
	 * Static Information
	 *****************************************************************************************************/
	
	public String getJVMName()
	{
		return JVM_NAME;
	}
	
	public String getJVMVersion()
	{
		return JVM_VER;
	}
	
	/*
	 * ***************************************************************************************************
	 * Real-Time Statistics
	 *****************************************************************************************************/
	
	public int getMaxMemory()
	{
		return (int) (Runtime.getRuntime().maxMemory() / NumericConstants.JDEC.MEGABYTE.byteValue);
	}
	
	public int getTotalJVMMemory()
	{
		return (int) (runtime.totalMemory() / NumericConstants.JDEC.MEGABYTE.byteValue);
	}
	
	public int getUsedJVMMemory()
	{
		return (int) ((runtime.totalMemory() - runtime.freeMemory()) / NumericConstants.JDEC.MEGABYTE.byteValue);
	}
	
	public int getUsedJVMMemoryPercentage()
	{
		long used = runtime.totalMemory() - runtime.freeMemory();
		
		return Math.round((used / MAX_MEMORY) * 100L);
	}
	
	public long getFreeJVMMemory()
	{
		long used = runtime.totalMemory() - runtime.freeMemory();
		
		return (int) ((runtime.maxMemory() - used) / NumericConstants.JDEC.MEGABYTE.byteValue);
	}
	
	public int getFreeJVMMemoryPercentage()
	{
		long used = runtime.totalMemory() - runtime.freeMemory();
		
		long jvmused = runtime.maxMemory() - used;
		
		return Math.round((jvmused / MAX_MEMORY) * 100);
	}
	
	/*
	 * ***************************************************************************************************
	 * Helper Method
	 *****************************************************************************************************/
	
	public String getJVMInfoString()
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
