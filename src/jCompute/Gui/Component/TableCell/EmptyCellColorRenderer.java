package jCompute.Gui.Component.TableCell;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class EmptyCellColorRenderer extends DefaultTableCellRenderer 
{
	private static final long serialVersionUID = 507858106811105911L;

	private Color backgroundColor;
	
	public EmptyCellColorRenderer(Color color)
	{
		this.backgroundColor = color;
	}
	
	public EmptyCellColorRenderer()
	{
		this.backgroundColor = new Color(240,240,240);
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if(value.getClass() == String.class)
        {
        	String text = (String)value;
        	
        	if(text.equals(""))
        	{
                component.setBackground(backgroundColor);
        	}
        	else
        	{
                component.setBackground(table.getBackground());
        	}
        }
    	else
    	{
            component.setBackground(table.getBackground());
    	}        
        
		return component;
	}

}
