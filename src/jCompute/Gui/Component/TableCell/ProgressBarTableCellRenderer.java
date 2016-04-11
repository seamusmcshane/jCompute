package jCompute.Gui.Component.TableCell;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import jCompute.Gui.Component.swing.jcomponent.JComputeProgressBar;

public class ProgressBarTableCellRenderer implements TableCellRenderer
{
	private JComputeProgressBar pb;
	
	public ProgressBarTableCellRenderer(JTable table)
	{
		pb = new JComputeProgressBar(table.getFont().deriveFont(table.getFont().getSize() * .75f), table.getSelectionBackground());
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		int width = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
		int height = table.getRowHeight();
		
		pb.setSize(width, height);
		
		pb.prepare((int) value, isSelected);
		
		return pb;
	}
}
