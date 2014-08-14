package jCompute.Gui.Batch.TableRowItems;

import jCompute.Gui.Component.RowItem;

public class BatchCompletedRowItem implements RowItem, Comparable
{
	private int batch;
	private String name;
	private String runTime;
	private String finished;
	
	public BatchCompletedRowItem()
	{
		super();
		this.batch = -1;
		this.name = "NULL";
		this.runTime = "NEVER";
		this.finished = "NEVER";
	}
	
	public BatchCompletedRowItem(int batch, String name, String runTime, String finished)
	{
		super();
		this.batch = batch;
		this.name = name;
		this.runTime = runTime;
		this.finished = finished;
	}

	public String[] getFieldList()
	{
		return new String[]{"batch", "name", "runTime", "finished"};
	}
	
	public String[] getFieldNames()
	{
		return new String[]{"Batch", "Name", "RunTime", "Finished"};
	}
	
	@Override
	public Object getFieldValue(int field)
	{
		switch(field)
		{
			case 0:
				return batch;
			case 1:
				return name;
			case 2:
				return runTime;
			case 3:
				return finished;
		}
		
		return null;
	}
	
	@Override
	public void setFieldValue(int field, Object value)
	{
		switch(field)
		{
			case 0:
				batch = (int) value;
			break;
			case 1:
				name = (String) value;
			break;
			case 2:
				runTime = (String) value;
			break;
			case 3:
				finished = (String)value;
			break;
		}
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

	public String getRunTime()
	{
		return runTime;
	}

	public void setRunTime(String runTime)
	{
		this.runTime = runTime;
	}

	public String getFinished()
	{
		return finished;
	}

	public void setFinished(String finished)
	{
		this.finished = finished;
	}

	@Override
	public int compareTo(Object rowObject)
	{
		BatchCompletedRowItem otherRow = (BatchCompletedRowItem)rowObject;
		int value = 0;
		
		if(this.batch > otherRow.getBatch())
		{
			value = 1;
		}
		else if(this.batch < otherRow.getBatch())
		{
			value = -1;
		}
		
		return value;
	}

}
