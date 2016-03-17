package jCompute.Gui.Component;

public abstract class RowItem<RowType, IndexType> implements Comparable<RowType>, RowKeyInf<IndexType>
{
	public abstract String[] getFieldList();

	public abstract String[] getFieldNames();

	public abstract boolean[] getEditableCells();

	public abstract Object getFieldValue(int field);

	public abstract void setFieldValue(int field, Object value);
}
