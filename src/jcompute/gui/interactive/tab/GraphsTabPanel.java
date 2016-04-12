package jcompute.gui.interactive.tab;

import java.awt.BorderLayout;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.IconManager;
import jcompute.IconManager.IconIndex;
import jcompute.gui.component.swing.jpanel.GlobalStatChartPanel;
import jcompute.simulationmanager.SimulationsManager;

public class GraphsTabPanel extends JPanel
{
	private static final long serialVersionUID = 6906961582077427931L;

	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(GraphsTabPanel.class);

	private JTabbedPane chartTabs;

	private LinkedList<GlobalStatChartPanel> charts;

	public GraphsTabPanel()
	{
		// Layout
		setLayout(new BorderLayout(0, 0));

		chartTabs = new JTabbedPane();

		add(chartTabs, BorderLayout.CENTER);
	}

	public void addCharts(LinkedList<GlobalStatChartPanel> charts)
	{
		for(GlobalStatChartPanel chartPanel : charts)
		{
			log.debug("Adding " + chartPanel.getName() + " Chart Panel");
			chartTabs.addTab(chartPanel.getName(), null, chartPanel);
			chartTabs.setIconAt(chartTabs.getTabCount() - 1, IconManager.retrieveIcon(IconIndex.charts16));
		}

		this.charts = charts;
	}

	public void clearCharts(SimulationsManager simsManager, int simId)
	{
		if(charts != null)
		{
			log.debug("Clearing Charts : " + charts.size());

			// Remove ChartPanels and Unset listeners
			for(GlobalStatChartPanel chartPanel : charts)
			{
				simsManager.removeStatGroupListener(simId, chartPanel.getName(), chartPanel);

				log.debug("Removing " + chartPanel.getName() + " Chart Panel");

				chartTabs.remove(chartPanel);

				chartPanel.destroy();
			}

			// Clear the Chart List
			charts = null;
		}
	}
}
