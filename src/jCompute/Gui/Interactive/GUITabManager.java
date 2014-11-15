package jCompute.Gui.Interactive;

import jCompute.IconManager;
import jCompute.Gui.Interactive.Tab.GUISimulationTab;
import jCompute.Gui.Interactive.Tab.SimulationListTabPanel;
import jCompute.SimulationManager.SimulationsManagerInf;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class GUITabManager extends JTabbedPane implements MouseListener, ActionListener
{
	private static final long serialVersionUID = 1L;
	
	/** Max Tabs == Max Simulations */
	private final int maxTabs;
	
	/** Current Tab Count */
	private int tabCount = 0;

	/** The Simulation List Tab */
	private SimulationListTabPanel simulationListTab;
	
	/** Special Panel used for the add tab feature */
	private JPanel addPanel = new JPanel();
	
	/** Used to prevent duplicate tab additions */
	private boolean addingTab = false;
	
	/** Simulation Tabs */
	private GUISimulationTab simulationTabs[];
	
	/** Tab Popup Menu */
	private JPopupMenu tabPopUpMenu;
	private JMenuItem menuCloseItem;
	private JMenuItem menuRemoveItem;
	private JMenuItem menuExportAllStats;	

	/** Current Selected Tab Index ID */
	private int selectedTabIndex;

	/** A Reference to the Simulations Manager */
	private SimulationsManagerInf simsManager;

	/* Icons */ 
	ImageIcon simulationListTabIcon = IconManager.getIcon("simulationListTabIcon");
	ImageIcon AddTabIcon = IconManager.getIcon("AddTabIcon");
	
	public GUITabManager(final SimulationsManagerInf simsManager)
	{
		/* Tabs on the Left */
		super(LEFT);
		
		this.simsManager = simsManager;
				
		/* Match the Simulations Manager Limits */
		maxTabs = simsManager.getMaxSims();
		simulationTabs = new GUISimulationTab[maxTabs];
	
		/* The tab with the list of simulations */
		simulationListTab = new SimulationListTabPanel(this);
		this.add(simulationListTab);
		
		this.setTitleAt(this.getTabCount()-1, simulationListTab.getTabName());
		this.setIconAt(this.getTabCount()-1,simulationListTabIcon);
	
		/* The Special AddTab button */
		this.add(addPanel);
		this.setIconAt(this.getTabCount()-1, AddTabIcon);
		
		this.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				// This makes the last tab (simulationListTab) act as a button, that adds new tabs.
				if( getSelectedComponent() == addPanel)
				{
					// call the add tab functionality and we are done.
					addTab(-1);
				}
				else
				{
					/* 
					 * Clear the Simulation Shown in the view.
					 */
					if(simulationListTab == getSelectedComponent())
					{						
						simsManager.clearActiveSim();							
					}
					else
					{
						/*
						 *  We find the SimulationTab that matches the selected tab.
						 *  Then set the active simulation in the view to the matching simulation id of the tab.
						 */
						for(int i=0;i<maxTabs;i++)
						{
							if(simulationTabs[i] == getSelectedComponent())
							{
								System.out.println("Simulation Tab Selected - Sim Id : " + simulationTabs[i].getSimulationId());
								
								simsManager.setActiveSim(simulationTabs[i].getSimulationId());
								
								break;
							}
						}					
					}					
				}
			}
		});

		// The remove tab popup menu
		tabPopUpMenu  = new JPopupMenu();
		
		menuCloseItem = new JMenuItem("Close Tab ");
		menuRemoveItem = new JMenuItem("Remove Tab");
		menuExportAllStats = new JMenuItem("Export All Stats");

		// Add a new menu item
		menuCloseItem.addActionListener(this);
		menuRemoveItem.addActionListener(this);
		menuExportAllStats.addActionListener(this);

	    tabPopUpMenu.add(menuCloseItem);
	    tabPopUpMenu.add(menuRemoveItem);
	    tabPopUpMenu.add(menuExportAllStats);
	    
		this.addMouseListener(this);

	}
			
	
	public void displayTab(int simId)
	{
		int tabSimId = -1;
			
		System.out.println(simId);
		
		// Check incase the tab is already added
		for(int i=0;i<maxTabs;i++)
		{
			// Incase no tabs
			if(simulationTabs[i] != null)
			{
				// Find out if we have this tab displayed (added)
				if(simulationTabs[i].getSimulationId() == simId)
				{
					this.setSelectedComponent(simulationTabs[i]);
					
					tabSimId = simulationTabs[i].getSimulationId();
					
					return;
				}
			}
		}
		
		// No Tab Added
		if(tabSimId == -1)
		{
			addTab(simId);
		} 
		else
		{
			this.setSelectedIndex(0);
			JOptionPane.showMessageDialog(this, "The Limit of " + simsManager.getMaxSims() + " Sims has been reached.");
		}
			
	}
	
	/**
	 * Adds a new Simulation Tab
	 */
	public void addTab(int simId)
	{
		// Prevent duplicate tab additions
		if(!addingTab)
		{
			addingTab=true;		
		
			// Only allow adding up to the tab limit
			if( tabCount < maxTabs)
			{
				// Loop though all the simulation tabs
				for(int i=0;i<maxTabs;i++)
				{
					// If This slot is a free.
					if(simulationTabs[i] == null)
					{
						// Add a new tab the list ( -1 = no Sim )
						simulationTabs[i] = new GUISimulationTab(this,simsManager, simId);
						
						// Listen to Status change events
						//simulationTabs[i].addTabStatusListener(this);
						
						// Add the tab before the new tab button
						this.add(simulationTabs[i],this.getTabCount()-1);
						//this.add(simulationTabs[i],this.getTabCount());
	
						// Default our states.
						this.setTabComponentAt(this.indexOfComponent(simulationTabs[i]), simulationTabs[i].getTabTitle());
						
						//this.setIconAt(this.getTabCount()-2, newTabIcon);
						//this.setTitleAt(this.getTabCount()-2, simulationTabs[i].getTitle());
						
						this.setSelectedComponent(simulationTabs[i]);
						
						/*this.setIconAt(this.getTabCount()-1, newTabIcon);
						this.setTitleAt(this.getTabCount()-1, simulationTabs[i].getTitle());
						this.setSelectedComponent(simulationTabs[i]);*/
						
						tabCount++;
						
						break;
					}
				}
			}
			else
			{
				this.setSelectedIndex(0);
				JOptionPane.showMessageDialog(this, "The Limit of " + maxTabs + " Tabs has been reached.");
			}
			
			addingTab=false;
		}
	}
	
	
	/** 
	 * Close a Tab when can later be re-opened
	 */
	public void closeTab()
	{				
		// Find the selected component
		for(int i=0;i<maxTabs;i++)
		{
			if(simulationTabs[i] == this.getSelectedComponent())
			{
				selectedTabIndex=i;
			}
		}
		
		// Select the Simulation List
		this.setSelectedIndex(0);
		
		// Tell the tab to detach up
		simulationTabs[selectedTabIndex].detachTabFromSim();
		
		// Remove this tab from the TabPane
		this.remove(simulationTabs[selectedTabIndex]);
		
		// Clear the slot in the tab list
		simulationTabs[selectedTabIndex] = null;
		tabCount--;
		
	}
	
	/** 
	 * Removes a Tab permanently
	 */
	public void removeTab()
	{				
		// Find the selected component
		for(int i=0;i<maxTabs;i++)
		{
			if(simulationTabs[i] == this.getSelectedComponent())
			{
				selectedTabIndex=i;
			}
		}
		
		// Select the Simulation List
		this.setSelectedIndex(0);
		
		// Tell the tab to clean up
		simulationTabs[selectedTabIndex].destroy();
		
		// Remove this tab from the TabPane
		this.remove(simulationTabs[selectedTabIndex]);
		
		// Clear the slot in the tab list
		simulationTabs[selectedTabIndex] = null;
		tabCount--;
		
	}
	
	
	
	/**
	 * The Popup menu.
	 * The text contents are dynamically set based on the selected tab name.
	 * @param e
	 */
	private void showPopUP(MouseEvent e)
	{	    
	    // Show the popup
	    tabPopUpMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{	
		if(this.getSelectedComponent() instanceof GUISimulationTab)
		{
			// If the right mouse is click on a tab
			if((e.getButton() == 3))
			{
				showPopUP(e);
			}	
		}
	
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{	
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{		
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{		

		if(e.getSource() == menuCloseItem)
		{
			closeTab();
		}	
		else if(e.getSource() == menuRemoveItem)
		{
			removeTab();
		}
		else if(e.getSource() == menuExportAllStats)
		{			
			chooseExport();
		}
		
	}

	public void closeAllSimTabs()
	{
		for(int i=0;i<maxTabs;i++)
		{
			if(simulationTabs[i]!=null)
			{
				this.setSelectedComponent(simulationTabs[i]);
				closeTab();
			}
		}
	}
	
	
	private void chooseExport()
	{
		System.out.println("Choose Export Directory");

		String exportDirectory = "";
		ExportFormat exportFormat = null;
		
		JFileChooser filechooser = new JFileChooser(new File("./stats"));

		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		filechooser.setDialogTitle("Choose Export Directory");
		
		filechooser.setSelectedFile(new File("./"));
		
		// Allowable file formats
		filechooser.setAcceptAllFileFilterUsed(false);

		// Add the formats
		for (ExportFormat format : ExportFormat.values()) 
		{
			filechooser.addChoosableFileFilter(new ExportFileFilter(format) );
		}
		
		int val = filechooser.showSaveDialog(filechooser);

		if (val == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				exportDirectory = filechooser.getSelectedFile().getCanonicalPath();
				exportFormat = ((ExportFileFilter) filechooser.getFileFilter()).getFormat();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			System.out.println("Export Cancelled");
			
			exportDirectory = "CANCELLED";
		}

		// Export File format
		if(!exportDirectory.equals("CANCELLED"))
		{
			int simId = ((GUISimulationTab)getSelectedComponent()).getSimulationId();
			
			System.out.println("Directory Choosen : " + exportDirectory);

			// Get the Stat Exporter containing the stats for simId
			StatExporter exporter = simsManager.getStatExporter(simId,"",exportFormat);
			
			if(exporter!=null)
			{
				// Export the stats
				exporter.exportAllStatsToDir(exportDirectory);
			}
			else
			{
				System.out.println("Error Stat Export was null");
			}

		}

	}
	
	private class ExportFileFilter extends FileFilter
	{
		private ExportFormat format;
		
		public ExportFileFilter(ExportFormat format)
		{
			this.format = format;
		}
		
		@Override
		public boolean accept(File file)
		{
			return file.getName().toLowerCase().endsWith(format.getExtension()) || file.isDirectory();
		}

		@Override
		public String getDescription()
		{
			return format.getDescription();
		}
		
		public ExportFormat getFormat()
		{
			return format;
		}
		
	}
	
}
