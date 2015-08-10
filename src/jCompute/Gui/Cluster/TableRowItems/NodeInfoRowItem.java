package jCompute.Gui.Cluster.TableRowItems;

import jCompute.Cluster.Node.NodeDetails.NodeInfo;
import jCompute.Gui.Component.RowItem;

public class NodeInfoRowItem implements RowItem, Comparable
{
	private int uid;
	private long weighting;
	private String address;
	private int maxSims;
	private int hwThreads;
	private String os;
	private String arch;
	private int totalOSMem;
	private int maxJVMMemory;
	private String desc;
	private int nodeState;
	
	public NodeInfoRowItem()
	{
		this.uid = -1;
		this.weighting = Long.MAX_VALUE;
		this.address = "Invalid";
		this.maxSims = 0;
		this.desc = "";
		this.nodeState = -1;
	}
	
	public NodeInfoRowItem(NodeInfo node, int nodeState)
	{
		this.uid = node.getUid();
		this.weighting = node.getWeighting();
		this.address = node.getAddress();
		this.maxSims = node.getMaxSims();
		this.hwThreads = node.getHWThreads();
		this.os = node.getOperatingSystem();
		this.arch = node.getSystemArch();
		this.totalOSMem = node.getTotalOSMemory();
		this.maxJVMMemory = node.getMaxJVMMemory();
		this.desc = node.getDescription();
		this.nodeState = nodeState;
	}
	
	public String[] getFieldList()
	{
		return new String[]
		{
			"uid", "weighting", "address", "maxSims", "hwThreads", "os", "arch", "totalOSMem", "maxJVMMemory", "desc", "nodeState"
		};
	}
	
	public String[] getFieldNames()
	{
		return new String[]
		{
			"Uid", "Weighting", "Address", "Max Sims", "HThreads", "OS", "Arch", "OS Mem", "JVM Memory", "Description", "Node State"
		};
	}
	
	@Override
	public boolean[] getEditableCells()
	{
		return new boolean[]
		{
			false, false, false, false, false, false, false, false, false, false, true
		};
	}
	
	@Override
	public Object getFieldValue(int field)
	{
		switch(field)
		{
			case 0:
				return uid;
			case 1:
				return weighting;
			case 2:
				return address;
			case 3:
				return maxSims;
			case 4:
				return hwThreads;
			case 5:
				return os;
			case 6:
				return arch;
			case 7:
				return totalOSMem;
			case 8:
				return maxJVMMemory;
			case 9:
				return desc;
			case 10:
				return nodeState;
		}
		
		return null;
	}
	
	@Override
	public void setFieldValue(int field, Object value)
	{
		switch(field)
		{
			case 0:
				uid = (int) value;
			break;
			case 1:
				weighting = (Long) value;
			break;
			case 2:
				address = (String) value;
			break;
			case 3:
				maxSims = (int) value;
			break;
			case 4:
				hwThreads = (int) value;
			break;
			case 5:
				os = (String) value;
			break;
			case 6:
				arch = (String) value;
			break;
			case 7:
				totalOSMem = (int) value;
			break;
			case 8:
				maxJVMMemory = (int) value;
			break;
			case 9:
				desc = (String) value;
			break;
			case 10:
				nodeState = (int) value;
			break;
		}
	}
	
	public int getUid()
	{
		return uid;
	}
	
	public Long getWeighting()
	{
		return weighting;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
	
	public int getHwThreads()
	{
		return hwThreads;
	}
	
	public String getOs()
	{
		return os;
	}
	
	public String getArch()
	{
		return arch;
	}
	
	public int getTotalOSMem()
	{
		return totalOSMem;
	}
	
	public int getMaxJVMMemory()
	{
		return maxJVMMemory;
	}
	
	public String getDesc()
	{
		return desc;
	}
	
	public void setNodeState(int nodeState)
	{
		this.nodeState = nodeState;
	}
	
	public int getNodeState()
	{
		return nodeState;
	}
	
	@Override
	public int compareTo(Object rowObject)
	{
		NodeInfoRowItem otherRow = (NodeInfoRowItem) rowObject;
		
		int value = 0;
		
		// Otherwise we sort by the position in the queue
		if(this.weighting < otherRow.getWeighting())
		{
			value = -1;
		}
		else if(this.weighting > otherRow.getWeighting())
		{
			value = 1;
		}
		
		return value;
	}
	
}
