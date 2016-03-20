package jCompute.Gui.Component.TableCell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class ColorLabelRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = -605977013068495073L;
	
	private JPanel panel;
	private JPanel colorPanel;
	private JLabel label;

	public ColorLabelRenderer()
	{
		panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new BorderLayout(0, 0));

		colorPanel = new JPanel();
		panel.add(colorPanel, BorderLayout.WEST);

		label = new JLabel("");
		panel.add(label, BorderLayout.CENTER);
		label.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		int nid = (int) value;
		Color color = new Color(Color.HSBtoRGB(((0.13f * nid) - 0.13f), 1f, 1f));

		colorPanel.setBackground(color);
		label.setText(String.valueOf(nid));

		return panel;
	}

}
