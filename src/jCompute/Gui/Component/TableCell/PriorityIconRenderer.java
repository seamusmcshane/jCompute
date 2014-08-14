package jCompute.Gui.Component.TableCell;

import jCompute.Batch.Batch.BatchPriority;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PriorityIconRenderer  extends DefaultTableCellRenderer 
{
	private static final long serialVersionUID = -8601876379125785406L;
	private ImageIcon hIcon = null;    
    private ImageIcon sIcon = null;    
    
    public PriorityIconRenderer(ImageIcon high,ImageIcon standard) 
    {
    	hIcon = high;
    	sIcon = standard;
    }
    
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		String priority = (String)value.toString();
		
		this.setHorizontalAlignment(JLabel.CENTER);		
		
		
		if(priority.equals("HIGH"))
		{
			this.setIcon(hIcon);
		}
		else
		{
			this.setIcon(sIcon);
		}
		
		if(isSelected)
		{
			this.setBackground(table.getSelectionBackground());
		}
		else
		{
			this.setBackground(table.getBackground());
		}

		return this;		
	}
}
