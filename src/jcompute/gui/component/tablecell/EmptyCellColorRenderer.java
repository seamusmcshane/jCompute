package jcompute.gui.component.tablecell;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

public class EmptyCellColorRenderer extends JTextArea implements TableCellRenderer
{
	private static final long serialVersionUID = 507858106811105911L;
	
	private Color backgroundColor;
	
	private final int charWrap;
	
	public EmptyCellColorRenderer(Color color, int charWrap)
	{
		this.backgroundColor = color;
		
		this.charWrap = charWrap;
		
		if(charWrap > 0)
		{
			setLineWrap(true);
			setWrapStyleWord(false);
		}
		
		setBorder(null);
		setOpaque(true);
	}
	
	public EmptyCellColorRenderer()
	{
		this(new Color(240, 240, 240), 0);
	}
	
	public EmptyCellColorRenderer(int charWrap)
	{
		this(new Color(240, 240, 240), charWrap);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		// Component component = getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		if(value.getClass() == String.class)
		{
			String text = (String) value;
			
			if(text.equals(""))
			{
				this.setBackground(backgroundColor);
			}
			else
			{
				if(charWrap > 0)
				{
					int len = text.length();
					
					if(len > charWrap)
					{
						int multi = len % charWrap;
						
						int rowHeight = table.getRowHeight();
						
						// Limit to three lines
						int newHeight = Math.min(rowHeight * multi, 60);
						
						table.setRowHeight(row, newHeight);
					}
					else
					{
						table.setRowHeight(row, table.getRowHeight());
					}
				}
				
				this.setBackground(table.getBackground());
			}
			
			setText(text);
		}
		else
		{
			this.setBackground(table.getBackground());
		}
		
		return this;
	}
}
