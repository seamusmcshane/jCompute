package jCompute.Gui.Component.TableCell;

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
	
	
	public ProgressBarTableCellRenderer(Color bg, Color fg,Color bar)
	{
		pb = new ProgressBar(bg,fg,bar);
	}
	

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if(isSelected)
		{
			pb.setBG(table.getSelectionBackground());
		}
		else
		{
			pb.setBG(table.getBackground());
		}
		
		pb.setProgress((int)value);
		return pb;
	}
}
