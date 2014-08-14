package jCompute.Gui.Standard.Tab;

import jCompute.IconManager;
import jCompute.Gui.Component.GlobalStatChartPanel;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;

import java.awt.BorderLayout;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class GraphsTabPanel extends JPanel
{
	private static final long serialVersionUID = 6906961582077427931L;

	private ImageIcon chartIcon = IconManager.getIcon("chartIcon");

	private JTabbedPane chartTabs;
	
	private LinkedList<GlobalStatChartPanel> charts;
	
	public GraphsTabPanel()
	{
		// Layout
		setLayout(new BorderLayout(0, 0));
		
		chartTabs = new JTabbedPane();
		
		add(chartTabs,BorderLayout.CENTER);

	}
	
	public void addCharts(LinkedList<GlobalStatChartPanel> charts)
	{
		for (GlobalStatChartPanel chartPanel : charts)
		{
			System.out.println("Adding " + chartPanel.getName() + " Chart Panel");
			chartTabs.addTab(chartPanel.getName(), null, chartPanel);
			chartTabs.setIconAt(chartTabs.getTabCount() - 1, chartIcon);
		}
		
		this.charts = charts;
		
	}
	
	public void clearCharts(SimulationsManagerInf simsManager, int simId)
	{
		if(charts!=null)
		{
			System.out.println("Clearing Charts : " + charts.size());
			
			// Remove ChartPanels and Unset listeners
			for (GlobalStatChartPanel chartPanel : charts)
			{
				simsManager.removeStatGroupListener(simId, chartPanel.getName(), chartPanel);
								
				System.out.println("Removing " + chartPanel.getName() + " Chart Panel");
				
				chartTabs.remove(chartPanel);
				
				chartPanel.destroy();
			}

			// Clear the Chart List
			charts = null;			
		}
	}
	
}
