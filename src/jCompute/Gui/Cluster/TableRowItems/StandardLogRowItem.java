package jCompute.gui.cluster.tablerowitems;

import jCompute.gui.component.RowItem;

public class StandardLogRowItem extends RowItem<StandardLogRowItem, Integer>
{
	private int index;
	private long msec;
	private String level;
	private String thread;
	private String message;

	public StandardLogRowItem()
	{
		index = -1;
		level = "";
		thread = "";
		message = "";
	}

	public StandardLogRowItem(int index, long msec, String level, String thread, String message)
	{
		super();
		this.index = index;
		this.msec = msec;
		this.level = level.replaceAll("\\s", "");
		this.thread = thread;
		this.message = message;
	}

	@Override
	public String[] getFieldList()
	{
		return new String[]
		{
			"index", "Milli", "level", "thread", "message"
		};
	}

	@Override
	public String[] getFieldNames()
	{
		return new String[]
		{
			"Index", "Milli", "Level", "Thread", "Message"
		};
	}

	@Override
	public boolean[] getEditableCells()
	{
		return new boolean[]
		{
			false, false, false, false, false, false
		};
	}

	@Override
	public Object getFieldValue(int field)
	{
		switch(field)
		{
			case 0:
				return index;
			case 1:
				return msec;
			case 2:
				return level;
			case 3:
				return thread;
			case 4:
				return message;
		}

		return null;
	}

	@Override
	public void setFieldValue(int field, Object value)
	{
		switch(field)
		{
			case 0:
				index = (int) value;
			break;
			case 1:
				msec = (long) value;
			break;
			case 2:
				level = (String) value;
			break;
			case 3:
				thread = (String) value;
			break;
			case 4:
				message = (String) value;
			break;
		}
	}

	public int getIndex()
	{
		return index;
	}

	public long getMilli()
	{
		return msec;
	}

	public String getThread()
	{
		return thread;
	}

	public String getLevel()
	{
		return level;
	}

	public String getMessage()
	{
		return message;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public void setMilli(long msec)
	{
		this.msec = msec;
	}

	public void setThread(String thread)
	{
		this.thread = thread;
	}

	public void setLevel(String level)
	{
		this.level = level;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	@Override
	public int compareTo(StandardLogRowItem otherRow)
	{
		int value = 0;

		if(index < otherRow.getIndex())
		{
			value = -1;
		}
		else if(index > otherRow.getIndex())
		{
			value = 1;
		}

		return value;
	}

	@Override
	public boolean keyEquals(Integer value)
	{
		return(index == value);
	}
}
