package jCompute.Gui.Batch.TableRowItems;

import jCompute.Batch.Batch;
import jCompute.Batch.Batch.BatchPriority;
import jCompute.Gui.Component.RowItem;

public class BatchQueueRowItem implements RowItem, Comparable
{
	private int position;
	private int batch;
	private String name;
	private BatchPriority priority;
	private boolean enabled;
	private int progress;
	private String estimatedFinish;

	public BatchQueueRowItem()
	{
		this.position = -1;
		this.batch = -1;
		this.name = "NULL";
		this.priority = BatchPriority.STANDARD;
		this.enabled = false;
		this.progress = -1;
		this.estimatedFinish = "Never";
	}

	public BatchQueueRowItem(Batch batch)
	{
		this.position = batch.getPosition();
		this.batch = batch.getBatchId();
		this.name = batch.getFileName();
		this.priority = batch.getPriority();
		this.enabled = batch.getEnabled();
		this.progress = batch.getProgress();

		if(batch.getCompleted() > 0)
		{
			this.estimatedFinish = jCompute.util.Text.timeNowPlus(batch.getETT());
		}
		else
		{
			this.estimatedFinish = jCompute.util.Text.timeNowPlus(-1);
		}

	}

	public String[] getFieldList()
	{
		return new String[]
		{
				"position", "batch", "name", "priority", "enabled", "progress", "estimatedFinish"
		};
	}

	public String[] getFieldNames()
	{
		return new String[]
		{
				"Position", "Batch", "Name", "Priority", "Enabled", "Progress", "Est Finish"
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
				return priority;
			case 4:
				return enabled;
			case 5:
				return progress;
			case 6:
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
				priority = (BatchPriority) value;
			break;
			case 4:
				enabled = (boolean) value;
			break;
			case 5:
				progress = (int) value;
			break;
			case 6:
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

	public BatchPriority getPriority()
	{
		return priority;
	}

	public void setPriority(BatchPriority priority)
	{
		this.priority = priority;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
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
	public int compareTo(Object rowObject)
	{
		BatchQueueRowItem otherRow = (BatchQueueRowItem) rowObject;
		int value = 0;

		// Evaluate the priorities
		int eval = priority.compareTo(otherRow.getPriority());

		// if the priorities are not equal we can use this as the value.
		if(eval < 0 || eval > 0)
		{
			value = eval;
		}
		else
		{
			// Otherwise we sort by the position in the queue
			if(this.position < otherRow.getPosition())
			{
				value = -1;
			}
			else if(this.position > otherRow.getPosition())
			{
				value = 1;
			}
		}

		return value;
	}

}
