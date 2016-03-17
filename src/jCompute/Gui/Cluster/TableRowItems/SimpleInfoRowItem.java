package jCompute.Gui.Cluster.TableRowItems;

import jCompute.Gui.Component.RowItem;

public class SimpleInfoRowItem extends RowItem<SimpleInfoRowItem>
{
	private String parameter;
	private String value;

	public SimpleInfoRowItem()
	{
		parameter = "NULL";
		value = "NULL";
	}

	public SimpleInfoRowItem(String parameter, String value)
	{
		super();
		this.parameter = parameter;
		this.value = value;
	}

	@Override
	public String[] getFieldList()
	{
		return new String[]
		{
			"parameter", "value"
		};
	}

	@Override
	public String[] getFieldNames()
	{
		return new String[]
		{
			"Parameter", "Value"
		};
	}

	@Override
	public boolean[] getEditableCells()
	{
		return new boolean[]
		{
			false, false
		};
	}

	@Override
	public Object getFieldValue(int field)
	{
		switch(field)
		{
			case 0:
				return parameter;
			case 1:
				return value;
		}

		return null;
	}

	@Override
	public void setFieldValue(int field, Object value)
	{
		switch(field)
		{
			case 0:
				parameter = (String) value;
			break;
			case 1:
				parameter = (String) value;
			break;
		}
	}

	public String getParameter()
	{
		return parameter;
	}

	public void setParameter(String parameter)
	{
		this.parameter = parameter;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public int compareTo(SimpleInfoRowItem o)
	{
		return 0;
	}
}
