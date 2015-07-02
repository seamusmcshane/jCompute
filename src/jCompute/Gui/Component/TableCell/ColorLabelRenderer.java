package jCompute.Gui.Component.TableCell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;

public class ColorLabelRenderer  extends DefaultTableCellRenderer 
{
	private JPanel panel;
	private JPanel colorPanel;
	private JLabel label;

	public ColorLabelRenderer()
	{
		panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new BorderLayout(0, 0));
		
		colorPanel = new JPanel();
		panel.add(colorPanel,BorderLayout.WEST);
		
		label = new JLabel("");
		panel.add(label,BorderLayout.CENTER);
		label.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		int nid = (int)value;
		Color color = new Color(Color.HSBtoRGB(((0.13f * nid) - 0.13f), 1f, 1f));

		colorPanel.setBackground(color);
		label.setText(String.valueOf(nid));
		
		return panel;
	}
	
}
