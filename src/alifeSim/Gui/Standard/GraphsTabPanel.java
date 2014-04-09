package alifeSim.Gui.Standard;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import alifeSim.Gui.Charts.GlobalStatChartPanel;
import alifeSim.Simulation.SimulationManager.SimulationsManagerInf;

public class GraphsTabPanel extends JPanel implements MouseListener, ActionListener
{	
	private ImageIcon simulationStatChartIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/kchart.png"));

	private JTabbedPane chartTabs;
	
	private LinkedList<GlobalStatChartPanel> charts;

	private JPopupMenu tabPopUpMenu;
	private JMenuItem menuExportStat;
	private JMenuItem menuExportAllStats;	
	
	public GraphsTabPanel()
	{
		// Layout
		setLayout(new BorderLayout(0, 0));
				
		chartTabs = new JTabbedPane();
		
		chartTabs.addMouseListener(this);
		
		add(chartTabs,BorderLayout.CENTER);
		
		// Export Pop up
		tabPopUpMenu  = new JPopupMenu();
		
		menuExportStat = new JMenuItem("Export Stat");
		menuExportAllStats = new JMenuItem("Export All Stats");
		
		// Add a new menu item
		menuExportStat.addActionListener(this);
		menuExportAllStats.addActionListener(this);
		
	    tabPopUpMenu.add(menuExportStat);
	    tabPopUpMenu.add(menuExportAllStats);
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

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if((e.getButton() == 3))
		{
		    tabPopUpMenu.show(e.getComponent(), e.getX(), e.getY());
		}	
		
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == menuExportStat)
		{
			System.out.println("Export Stat");
		}	
		else if(e.getSource() == menuExportAllStats)
		{
			System.out.println("Export All Stats");
		}		
	}
	
}
