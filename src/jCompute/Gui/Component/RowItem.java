package jCompute.Gui.Component;

public interface RowItem
{
	public String[] getFieldList();
	public String[] getFieldNames();
	public boolean[] getEditableCells();
	public Object getFieldValue(int field);
	public void setFieldValue(int field,Object value);
}
