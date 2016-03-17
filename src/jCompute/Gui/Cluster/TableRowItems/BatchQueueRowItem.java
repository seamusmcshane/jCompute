package jCompute.Gui.Cluster.TableRowItems;

import jCompute.Batch.Batch;
import jCompute.Gui.Component.RowItem;

public class BatchQueueRowItem extends RowItem<BatchQueueRowItem>
{
	private int position;
	private int batch;
	private String name;
	private boolean status;
	private int progress;
	private String estimatedFinish;

	public BatchQueueRowItem()
	{
		position = -1;
		batch = -1;
		name = "NULL";
		status = false;
		progress = -1;
		estimatedFinish = "Never";
	}

	public BatchQueueRowItem(Batch batch)
	{
		position = batch.getPosition();
		this.batch = batch.getBatchId();
		name = batch.getFileName();
		status = batch.getStatus();
		progress = batch.getProgress();

		if(batch.getCompleted() > 0)
		{
			estimatedFinish = jCompute.util.Text.timeNowPlus(batch.getETT());
		}
		else
		{
			estimatedFinish = jCompute.util.Text.timeNowPlus(-1);
		}

	}

	@Override
	public String[] getFieldList()
	{
		return new String[]
		{
			"position", "batch", "name", "status", "progress", "estimatedFinish"
		};
	}

	@Override
	public String[] getFieldNames()
	{
		return new String[]
		{
			"Position", "Batch", "Name", "Status", "Progress", "Est Finish"
		};
	}

	@Override
	public boolean[] getEditableCells()
	{
		return new boolean[]
		{
			false, false, false, false, false, false, false
		};
	}

	@Override
	public Object getFieldValue(int field)
	{
		switch(field)
		{
			case 0:
				return position;
			case 1:
				return batch;
			case 2:
				return name;
			case 3:
				return status;
			case 4:
				return progress;
			case 5:
				return estimatedFinish;
		}

		return null;
	}

	@Override
	public void setFieldValue(int field, Object value)
	{
		switch(field)
		{
			case 0:
				position = (int) value;
			break;
			case 1:
				batch = (int) value;
			break;
			case 2:
				name = (String) value;
			break;
			case 3:
				status = (boolean) value;
			break;
			case 4:
				progress = (int) value;
			break;
			case 5:
				estimatedFinish = (String) value;
			break;
		}
	}

	public long getPosition()
	{
		return position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public long getBatch()
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

	public boolean getStatus()
	{
		return status;
	}

	public void setStatus(boolean status)
	{
		this.status = status;
	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public String getEstimatedFinish()
	{
		return estimatedFinish;
	}

	public void setEstimatedFinish(String estimatedFinish)
	{
		this.estimatedFinish = estimatedFinish;
	}

	@Override
	public int compareTo(BatchQueueRowItem otherRow)
	{
		int value = 0;

		// Otherwise we sort by the position in the queue
		if(position < otherRow.getPosition())
		{
			value = -1;
		}
		else if(position > otherRow.getPosition())
		{
			value = 1;
		}

		return value;
	}

}
