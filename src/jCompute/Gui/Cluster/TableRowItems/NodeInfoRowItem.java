package jCompute.Gui.Cluster.TableRowItems;

import jCompute.Cluster.Node.NodeInfo;
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
	private int totalMem;

	public NodeInfoRowItem()
	{
		this.uid = -1;
		this.weighting = Long.MAX_VALUE;
		this.address = "Invalid";
		this.maxSims = 0;
	}

	public NodeInfoRowItem(NodeInfo node)
	{
		this.uid = node.getUid();
		this.weighting = node.getWeighting();
		this.address = node.getAddress();
		this.maxSims = node.getMaxSims();
		this.hwThreads = node.getHWThreads();
		this.os = node.getOperatingSystem();
		this.arch = node.getSystemArch();
		this.totalMem = node.getTotalMemory();
	}

	public String[] getFieldList()
	{
		return new String[]
		{
				"uid", "weighting", "address", "maxSims", "hwThreads", "os", "arch", "totalMem"
		};
	}

	public String[] getFieldNames()
	{
		return new String[]
		{
				"Uid", "Weighting", "Address", "Max Sims", "HThreads", "OS", "Arch", "Memory"
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
				return totalMem;
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
				totalMem = (int) value;
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

	public int getTotalMem()
	{
		return totalMem;
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
