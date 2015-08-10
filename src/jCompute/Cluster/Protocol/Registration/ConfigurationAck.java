package jCompute.Cluster.Protocol.Registration;

import jCompute.Cluster.Node.NodeDetails.NodeInfo;
import jCompute.Cluster.Protocol.NCP;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ConfigurationAck
{
	private int maxSims;
	private long weighting;
	private String os;
	private String arch;
	private int hwThreads;
	private int totalOSMemory;
	private int maxJVMMemory;
	private String desc;
	
	public ConfigurationAck(NodeInfo conf)
	{
		this.maxSims = conf.getMaxSims();
		this.weighting = conf.getWeighting();
		
		this.os = conf.getOperatingSystem();
		this.arch = conf.getSystemArch();
		this.hwThreads = conf.getHWThreads();
		
		this.totalOSMemory = conf.getTotalOSMemory();
		this.maxJVMMemory = conf.getMaxJVMMemory();
		
		this.desc = conf.getDescription();
	}
	
	// Construct from an input stream
	public ConfigurationAck(ByteBuffer source) throws IOException
	{
		byte[] tBytes;
		int tLen;
		maxSims = source.getInt();
		weighting = source.getLong();
		
		// OS Len
		tLen = source.getInt();
		tBytes = new byte[tLen];
		// OS
		source.get(tBytes, 0, tLen);
		os = new String(tBytes);
		
		// Arch Len
		tLen = source.getInt();
		tBytes = new byte[tLen];
		// Arch
		source.get(tBytes, 0, tLen);
		arch = new String(tBytes);
		
		hwThreads = source.getInt();
		totalOSMemory = source.getInt();
		maxJVMMemory = source.getInt();
		
		// Desc Len
		tLen = source.getInt();
		tBytes = new byte[tLen];
		// Desc
		source.get(tBytes, 0, tLen);
		desc = new String(tBytes);
		
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
	
	public long getWeighting()
	{
		return weighting;
	}
	
	public String getOs()
	{
		return os;
	}
	
	public String getArch()
	{
		return arch;
	}
	
	public int getHwThreads()
	{
		return hwThreads;
	}
	
	public int getTotalOSMemory()
	{
		return totalOSMemory;
	}
	
	public int getMaxJVMMemory()
	{
		return maxJVMMemory;
	}
	
	public String getDescription()
	{
		return desc;
	}
	
	public byte[] toBytes()
	{
		int osLen = os.getBytes().length;
		int archLen = arch.getBytes().length;
		
		int descLen = desc.getBytes().length;
		
		int dataLen = 36 + osLen + archLen + descLen;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCP.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCP.ConfAck);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(maxSims);
		tbuffer.putLong(weighting);
		
		tbuffer.putInt(osLen);
		tbuffer.put(os.getBytes());
		
		tbuffer.putInt(archLen);
		tbuffer.put(arch.getBytes());
		
		tbuffer.putInt(hwThreads);
		tbuffer.putInt(totalOSMemory);
		tbuffer.putInt(maxJVMMemory);
		
		tbuffer.putInt(descLen);
		
		tbuffer.put(desc.getBytes());
		
		return tbuffer.array();
	}
}
