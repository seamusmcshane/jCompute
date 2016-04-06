package jCompute.Gui.Cluster.TableRowItems;

import jCompute.Gui.Component.RowItem;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.util.TimeString;
import jCompute.util.TimeString.TimeStringFormat;

public class SimulationListRowItem extends RowItem<SimulationListRowItem, Integer>
{
	private int simId;
	private SimState state;
	private int stepNo;
	private int progress;
	private int asps;
	private long runTime;

	public SimulationListRowItem()
	{
		super();
		simId = -1;
		state = SimState.NEW;
		stepNo = -1;
		progress = -1;
		asps = -1;
		runTime = -1;
	}

	public SimulationListRowItem(int simId)
	{
		super();
		this.simId = simId;
		state = SimState.NEW;
		stepNo = 0;
		progress = 0;
		asps = 0;
		runTime = 0;
	}

	public SimulationListRowItem(int simId, SimState state, int stepNo, int progress, int asps, long runTime)
	{
		super();
		this.simId = simId;
		this.state = state;
		this.stepNo = stepNo;
		this.progress = progress;
		this.asps = asps;
		this.runTime = runTime;
	}

	@Override
	public String[] getFieldList()
	{
		return new String[]
		{
			"simId", "state", "stepNo", "progress", "asps", "runTime"
		};
	}

	@Override
	public String[] getFieldNames()
	{
		return new String[]
		{
			"SimId", "State", "Step", "Progress", "Asps", "Run Time"
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
				return simId;
			case 1:
				return state;
			case 2:
				return stepNo;
			case 3:
				return progress;
			case 4:
				return asps;
			case 5:
				return runTime;
		}

		return null;
	}

	@Override
	public void setFieldValue(int field, Object value)
	{
		switch(field)
		{
			case 0:
				simId = (int) value;
			break;
			case 1:
				state = (SimState) value;
			break;
			case 2:
				stepNo = (int) value;
			break;
			case 3:
				progress = (int) value;
			break;
			case 4:
				asps = (int) value;
			break;
			case 5:
				runTime = (long) value;
			break;
		}
	}

	public int getSimId()
	{
		return simId;
	}

	public void setSimId(int simId)
	{
		this.simId = simId;
	}

	public SimState getState()
	{
		return state;
	}

	public void setState(SimState state)
	{
		this.state = state;
	}

	public int getStepNo()
	{
		return stepNo;
	}

	public void setStepNo(int stepNo)
	{
		this.stepNo = stepNo;
	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public int getAsps()
	{
		return asps;
	}

	public void setAsps(int asps)
	{
		this.asps = asps;
	}
	
	public String getRunTime()
	{
		return TimeString.timeInMillisAsFormattedString(runTime, TimeStringFormat.HMS);
	}
	
	public void setRunTime(long runTime)
	{
		this.runTime = runTime;
	}

	@Override
	public int compareTo(SimulationListRowItem otherRow)
	{
		int value = 0;

		if(simId > otherRow.getSimId())
		{
			value = 1;
		}
		else if(getSimId() < otherRow.getSimId())
		{
			value = -1;
		}

		return value;
	}

	@Override
	public boolean keyEquals(Integer value)
	{
		return(simId == value);
	}
}
