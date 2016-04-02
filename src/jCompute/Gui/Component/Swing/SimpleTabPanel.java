package jCompute.Gui.Component.Swing;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class SimpleTabPanel extends JPanel
{
	private static final long serialVersionUID = -2568428138969029838L;
	
	private JTabbedPane tabs;
	
	// Link the SimpleTab Alignment to JTabbedPane Alignments
	public static final int CENTER = JTabbedPane.CENTER;
	public static final int TOP = JTabbedPane.TOP;
	public static final int LEFT = JTabbedPane.LEFT;
	public static final int BOTTOM = JTabbedPane.BOTTOM;
	public static final int RIGHT = JTabbedPane.RIGHT;
	
	public SimpleTabPanel(int tabPlacement)
	{
		// Layout
		setLayout(new BorderLayout());
		
		tabs = new JTabbedPane(tabPlacement);
		
		add(tabs, BorderLayout.CENTER);
	}
	
	public SimpleTabPanel()
	{
		// Default is Tabs on top
		this(TOP);
	}
	
	public void addTab(Component component, SimpleTabTabTitle tabTitle)
	{
		tabs.addTab("", null, component);
		tabs.setTabComponentAt(tabs.getTabCount() - 1, tabTitle);
	}
	
	/**
	 * Add Tab with an Icon
	 *
	 * @param component
	 * @param icon
	 * @param name
	 */
	public void addTab(Component component, Icon icon, String name)
	{
		tabs.addTab(name, icon, component);
	}
	
	/**
	 * Add Tab with no Icon
	 *
	 * @param component
	 * @param name
	 */
	public void addTab(Component component, String name)
	{
		tabs.addTab(name, null, component);
	}
	
	/**
	 * Check the tab list for a Component with a class matching clazz.
	 * 
	 * @param clazz
	 * @return
	 * True if there is one or more matching classes.
	 * False if there is none.
	 */
	public boolean hasTabComponentWithClass(Class<?> clazz)
	{
		for(Component tabComponent : tabs.getComponents())
		{
			if(tabComponent.getClass().equals(clazz))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Removes a tab
	 *
	 * @param component
	 */
	public void removeTab(Component component)
	{
		tabs.remove(component);
	}
	
	/**
	 * Selects a tab
	 *
	 * @param component
	 */
	public void setSelectedTab(Component component)
	{
		tabs.setSelectedComponent(component);
	}
	
	/**
	 * 
	 * @param class1
	 */
	public void setSelectedTab(Class<?> clazz)
	{
		for(Component tabComponent : tabs.getComponents())
		{
			if(tabComponent.getClass().equals(clazz))
			{
				tabs.setSelectedComponent(tabComponent);
			}
		}		
	}
	
	/***
	 * Selects a tab index
	 *
	 * @param i
	 */
	public void setSelectedTab(int index)
	{
		if(index < 0)
		{
			return;
		}
		
		if(index > tabs.getTabCount())
		{
			return;
		}
		
		tabs.setSelectedIndex(0);
	}
}
