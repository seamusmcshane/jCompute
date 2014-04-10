package alifeSim.Gui.Standard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileFilter;

import alifeSim.Gui.Charts.GlobalStatChartPanel;
import alifeSim.Simulation.SimulationManager.SimulationsManagerInf;

public class GraphsTabPanel extends JPanel
{	
	private ImageIcon simulationStatChartIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/kchart.png"));

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
			chartTabs.setIconAt(chartTabs.getTabCount() - 1, simulationStatChartIcon);
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
