package jCompute.Gui.Cluster.TableRowItems;

import jCompute.Gui.Component.RowItem;

public class BatchCompletedRowItem extends RowItem<BatchCompletedRowItem, Integer>
{
	private int batchId;
	private String name;
	private String runTime;
	private String finished;

	public BatchCompletedRowItem()
	{
		super();
		batchId = -1;
		name = "NULL";
		runTime = "NEVER";
		finished = "NEVER";
	}

	public BatchCompletedRowItem(int batchId, String name, String runTime, String finished)
	{
		super();
		this.batchId = batchId;
		this.name = name;
		this.runTime = runTime;
		this.finished = finished;
	}

	@Override
	public String[] getFieldList()
	{
		return new String[]
		{
			"batchId", "name", "runTime", "finished"
		};
	}

	@Override
	public String[] getFieldNames()
	{
		return new String[]
		{
			"Batch Id", "Name", "RunTime", "Finished"
		};
	}

	@Override
	public boolean[] getEditableCells()
	{
		return new boolean[]
		{
			false, false, false, false
		};
	}

	@Override
	public Object getFieldValue(int field)
	{
		switch(field)
		{
			case 0:
				return batchId;
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
				batchId = (int) value;
			break;
			case 1:
				name = (String) value;
			break;
			case 2:
				runTime = (String) value;
			break;
			case 3:
				finished = (String) value;
			break;
		}
	}

	public int getBatchId()
	{
		return batchId;
	}

	public void setBatchId(int batchId)
	{
		this.batchId = batchId;
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
	public int compareTo(BatchCompletedRowItem rowObject)
	{
		BatchCompletedRowItem otherRow = rowObject;

		if(batchId > otherRow.getBatchId())
		{
			return 1;
		}
		else if(batchId < otherRow.getBatchId())
		{
			return -1;
		}
		else
		{
			// Equal
			return 0;
		}
	}

	@Override
	public boolean keyEquals(Integer value)
	{
		return(batchId == value);
	}
}
