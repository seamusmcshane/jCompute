package jCompute.Gui.Batch.TableRowItems;

import jCompute.Gui.Component.RowItem;

public class BatchInfoQueueRowItem implements RowItem, Comparable
{
	private int item;
	private int batch;
	private String name;
	
	public BatchInfoQueueRowItem()
	{
		super();
		this.item = -1;
		this.batch = -1;
		this.name = "NULL";
	}
	
	public BatchInfoQueueRowItem(int item, int batch, String name)
	{
		super();
		this.item = item;
		this.batch = batch;
		this.name = name;
	}
	
	public String[] getFieldList()
	{
		return new String[]{"item", "batch", "name"};
	}
	
	public String[] getFieldNames()
	{
		return new String[]{"Item", "Batch", "Name"};
	}
	
	@Override
	public Object getFieldValue(int field)
	{
		switch(field)
		{
			case 0:
				return item;
			case 1:
				return batch;
			case 2:
				return name;
		}
		
		return null;
	}
	
	@Override
	public void setFieldValue(int field, Object value)
	{
		switch(field)
		{
			case 0:
				item = (int) value;
			break;
			case 1:
				batch = (int) value;
			break;
			case 2:
				name = (String) value;
			break;
		}
	}

	public int getItem()
	{
		return item;
	}

	public void setItem(int item)
	{
		this.item = item;
	}

	public int getBatch()
	{
		return batch;
	}

	public void setBatch(int batch)
	{
		this.batch = batch;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public int compareTo(Object rowObject)
	{
		BatchInfoQueueRowItem otherRow = (BatchInfoQueueRowItem)rowObject;
		int value = 0;
		
		if(this.item > otherRow.getItem())
		{
			value = 1;
		}
		else if(this.item < otherRow.getItem())
		{
			value = -1;
		}
		
		return value;
	}
}
