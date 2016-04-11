package jCompute.gui.component.tablecell;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class BooleanIconRenderer extends DefaultTableCellRenderer 
{
	private static final long serialVersionUID = 2460204844367610001L;
	private ImageIcon tIcon = null;    
    private ImageIcon fIcon = null;    
    
    public BooleanIconRenderer(ImageIcon trueIcon,ImageIcon falseIcon) 
    {
    	tIcon = trueIcon;
    	fIcon = falseIcon;
    }
    
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		boolean boolValue = (boolean)value;
		
		this.setHorizontalAlignment(JLabel.CENTER);
		
		if(boolValue)
		{
			this.setIcon(tIcon);
		}
		else
		{
			this.setIcon(fIcon);
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