package jCompute.Gui.Component.Swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SimpleTabTabTitle extends JPanel
{
	private static final long serialVersionUID = 6971454561853926885L;

	public SimpleTabTabTitle(int tabWidth,Icon icon, String text)
	{
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setVgap(10);
		borderLayout.setHgap(10);
		setLayout(borderLayout);
		
		this.setOpaque(false);
		
		setPreferredSize(new Dimension(tabWidth,icon.getIconHeight()));
		
		add(new JLabel(icon), BorderLayout.WEST);
		
		// html used for text wrapping
		add(new JLabel("<html>"+ text +"</html>"), BorderLayout.CENTER);
	}
}
