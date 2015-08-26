package jCompute.Gui.Cluster.TableRowItems;

import jCompute.Gui.Component.RowItem;

public class NodeConnectionLogRowItem implements RowItem, Comparable
{
	private int eid;
	private int uid;
	private String address;
	private String event;
	private String time;
	
	public NodeConnectionLogRowItem()
	{
		this.eid = -1;
		this.uid = -1;
		this.address = "";
		this.event = "";
		this.time = "";
	}
	
	public NodeConnectionLogRowItem(int eid, int uid, String address, String event, String time)
	{
		this.eid = eid;
		this.uid = uid;
		this.address = address;
		this.event = event;
		this.time = time;
	}
	
	public String[] getFieldList()
	{
		return new String[]
		{
			"eid", "uid", "address", "event", "time"
		};
	}
	
	public String[] getFieldNames()
	{
		return new String[]
		{
			"Eid", "Uid", "Address", "Event", "Time"
		};
	}
	
	@Override
	public boolean[] getEditableCells()
	{
		return new boolean[]
		{
			false, false, false, false, false
		};
	}
	
	@Override
	public Object getFieldValue(int field)
	{
		switch(field)
		{
			case 0:
				return eid;
			case 1:
				return uid;
			case 2:
				return address;
			case 3:
				return event;
			case 4:
				return time;
		}
		
		return null;
	}
	
	@Override
	public void setFieldValue(int field, Object value)
	{
		switch(field)
		{
			case 0:
				eid = (int) value;
			break;
			case 1:
				uid = (int) value;
			break;
			case 2:
				address = (String) value;
			break;
			case 3:
				event = (String) value;
			break;
			case 4:
				time = (String) value;
			break;
		}
	}
	
	public int getEid()
	{
		return eid;
	}
	
	public int getUid()
	{
		return uid;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public String getEvent()
	{
		return event;
	}
	
	public String getTime()
	{
		return time;
	}
	
	public void setEid(int eid)
	{
		this.eid = eid;
	}
	
	public void setUid(int uid)
	{
		this.uid = uid;
	}
	
	public void setAddress(String address)
	{
		this.address = address;
	}
	
	public void setEvent(String event)
	{
		this.event = event;
	}
	
	public void setTime(String time)
	{
		this.time = time;
	}
	
	@Override
	public int compareTo(Object rowObject)
	{
		NodeConnectionLogRowItem otherRow = (NodeConnectionLogRowItem) rowObject;
		
		int value = 0;
		
		if(this.eid < otherRow.getEid())
		{
			value = -1;
		}
		else if(this.eid > otherRow.getEid())
		{
			value = 1;
		}
		
		return value;
	}
	
}
