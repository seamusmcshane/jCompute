package alifeSim.Gui.Component;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressBarTableCellRenderer implements TableCellRenderer
{
	ProgressBar pb;

	public ProgressBarTableCellRenderer()
	{
		pb = new ProgressBar();
	}
	
	
	public ProgressBarTableCellRenderer(Color bg, Color fg)
	{
		pb = new ProgressBar(bg,fg);
	}
	

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		pb.setProgress(Integer.parseInt((String) value));
		return pb;
	}
}
