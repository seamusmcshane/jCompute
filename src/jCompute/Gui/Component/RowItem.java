package jCompute.Gui.Component;

public abstract class RowItem<T> implements Comparable<T>
{
	public String[] getFieldList()
	{
		return null;
	}

	public String[] getFieldNames()
	{
		return null;
	}

	public boolean[] getEditableCells()
	{
		return null;
	}

	public Object getFieldValue(int field)
	{
		return null;
	}

	public void setFieldValue(int field, Object value)
	{

	}
}
