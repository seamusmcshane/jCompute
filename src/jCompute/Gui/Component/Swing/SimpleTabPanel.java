package jCompute.Gui.Component.Swing;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class SimpleTabPanel extends JPanel
{	
	private static final long serialVersionUID = -2568428138969029838L;
	
	private JTabbedPane tabs;
	
	public SimpleTabPanel()
	{
		// Layout
		setLayout(new BorderLayout());
		
		tabs = new JTabbedPane();
		
		add(tabs,BorderLayout.CENTER);

	}
	
	public void addTab(Component component, String name)
	{
		tabs.addTab(name, null,component);
	}
	
}

