package jcompute.gui.component.tablecell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class TextHighLighterRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = -605977013068495073L;
	
	private JPanel panel;
	private JLabel label;
	private String[] highlight;
	private Color[] color;
	
	public TextHighLighterRenderer(Color[] color, String[] highlight)
	{
		panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new BorderLayout(0, 0));
		
		label = new JLabel("");
		panel.add(label, BorderLayout.CENTER);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.color = color;
		this.highlight = highlight;
		
		if(color.length != highlight.length)
		{
			throw new IllegalArgumentException("Color and highlight array lengths must be equal");
		}
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		String text = (String) value;
		
		for(int h = 0; h < highlight.length; h++)
		{
			if(text.equals(highlight[h]))
			{
				label.setForeground(color[h]);
				label.setText(text);
				
				break;
			}
		}
		
		if(isSelected)
		{
			panel.setBackground(table.getSelectionBackground());
		}
		else
		{
			panel.setBackground(table.getBackground());
		}
		
		return panel;
	}
}
