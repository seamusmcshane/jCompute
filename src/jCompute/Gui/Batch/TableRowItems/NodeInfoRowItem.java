package jCompute.Gui.Batch.TableRowItems;

import jCompute.Cluster.Node.NodeConfiguration;
import jCompute.Gui.Component.RowItem;

public class NodeInfoRowItem implements RowItem, Comparable
{
	private int uid;
	private long weighting;
	private String address;
	private int maxSims;
	private long simulationsProcessed;

	public NodeInfoRowItem()
	{
		this.uid = -1;
		this.weighting = Long.MAX_VALUE;
		this.address = "Invalid";
		this.maxSims = 0;
		this.simulationsProcessed = 0;
	}

	public NodeInfoRowItem(NodeConfiguration node)
	{
		this.uid = node.getUid();
		this.weighting = node.getWeighting();
		this.address = node.getAddress();
		this.maxSims = node.getMaxSims();
		this.simulationsProcessed = node.getSimulationsProcessed();
	}

	public String[] getFieldList()
	{
		return new String[]
		{
				"uid", "weighting", "address", "maxSims", "simulationsProcessed"
		};
	}

	public String[] getFieldNames()
	{
		return new String[]
		{
				"Uid", "Weighting", "Address", "Max Sims", "Completed"
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
			case 5:
				return simulationsProcessed;
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
			case 4:
				simulationsProcessed = (long) value;
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

	public void setUid(int uid)
	{
		this.uid = uid;
	}

	public void setWeighting(Long weighting)
	{
		this.weighting = weighting;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public void setMaxSims(int maxSims)
	{
		this.maxSims = maxSims;
	}

	public long getSimulationsProcessed()
	{
		return simulationsProcessed;
	}

	public void setSimulationsProcessed(long simulationsProcessed)
	{
		this.simulationsProcessed = simulationsProcessed;
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
