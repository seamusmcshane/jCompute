package alifeSim.Gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import alifeSim.Simulation.SimulationsManager;
import alifeSim.Simulation.SimulationState.SimStatus;

public class SimulationTabPanelManager extends JTabbedPane implements MouseListener, ActionListener,TabStatusChangedListenerInf
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
	private SimulationTabPanel simulationTabs[];
	
	/** Remove Popup Menu */
	private JPopupMenu tabRemovePopUpMenu;

	/** Current Selected Tab Index ID */
	private int selectedTabIndex;

	/** A Reference to the Simulations Manager */
	private SimulationsManager simsManager;
	
	private ImageIcon simRunningIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-start.png"));
	private ImageIcon simPausedIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-pause.png"));
	private ImageIcon newTabIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/dialog-warning.png"));
	private ImageIcon simNewIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-stop.png"));
	private ImageIcon simFinishedIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/task-complete.png"));
	
	public SimulationTabPanelManager(final SimulationsManager simsManager)
	{
		/* Tabs on the Left */
		super(LEFT);
		
		this.simsManager = simsManager;
				
		/* Match the Simulations Manager Limits */
		maxTabs = simsManager.getMaxSims();
		simulationTabs = new SimulationTabPanel[maxTabs];
	
		/* The tab with the list of simulations */
		simulationListTab = new SimulationListTabPanel();	
		this.add(simulationListTab);
		this.setTitleAt(this.getTabCount()-1, simulationListTab.getTabName());
		this.setIconAt(this.getTabCount()-1, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/dialog-information.png")));
	
		/* The Special AddTab button */
		this.add(addPanel);
		this.setIconAt(this.getTabCount()-1, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/tab-new-background.png")));
		
		this.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				// This makes the last tab (simulationListTab) act as a button, that adds new tabs.
				if( getSelectedComponent() == addPanel)
				{
					// call the add tab functionality and we are done.
					addTab();
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
								simsManager.setActiveSim(simulationTabs[i].getSimulationId());
								
								break;
							}
						}					
					}					
				}
			}
		});

		// The remove tab popup menu
		tabRemovePopUpMenu  = new JPopupMenu();
		this.addMouseListener(this);

	}
			
	/**
	 * Adds a new Simulation Tab
	 */
	private void addTab()
	{		
		// Prevent duplicate tab additions
		if(!addingTab)
		{
			addingTab=true;		
		
			// Only allow adding up to the tab limit
			if(tabCount < maxTabs)
			{
				// Loop though all the simulation tabs
				for(int i=0;i<maxTabs;i++)
				{
					// If This slot is a free.
					if(simulationTabs[i] == null)
					{
						// Add a new tab the list
						simulationTabs[i] = new SimulationTabPanel(simsManager);
						
						// Listen to Status change events
						simulationTabs[i].addTabStatusListener(this);
						
						// Add the tab before the new tab button
						this.add(simulationTabs[i],this.getTabCount()-1);
	
						// Default our states.
						this.setIconAt(this.getTabCount()-2, newTabIcon);
						this.setTitleAt(this.getTabCount()-2, simulationTabs[i].getTitle());
						this.setSelectedComponent(simulationTabs[i]);
						
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
	 * Removes a Simulation Tab if the user confirms
	 */
	private void removeTab()
	{
		String title = "Confirm Simulation Remove";
		String message = "Remove \"" + this.getTitleAt(this.getSelectedIndex()) + "\" ?";

		JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		// Center Dialog on the GUI and Display
		JDialog dialog = pane.createDialog(this, title);
		dialog.pack();
		dialog.setVisible(true);

		int value = ((Integer) pane.getValue()).intValue();

		// If the user has confirmed
		if (value == JOptionPane.YES_OPTION)
		{
			// Remove the Simulation Matching the Current Tabs SimId
			simsManager.removeSimulation(simulationTabs[selectedTabIndex].getSimulationId());			
			
			// Select the Simualtion List
			this.setSelectedIndex(0);
			
			// Remove this tab from the TabPane
			this.remove(simulationTabs[selectedTabIndex]);
			
			// Clear the slot in the tab list
			simulationTabs[selectedTabIndex] = null;
			tabCount--;
		}
		
	}
	
	/**
	 * The Popup menu.
	 * The text contents are dynamically set based on the selected tab name.
	 * @param e
	 */
	private void showPopUP(MouseEvent e)
	{
		// Clear the contents of the popup menu
		tabRemovePopUpMenu.removeAll();
		JMenuItem menuItem = new JMenuItem("Remove " + this.getTitleAt(this.getSelectedIndex()));
		
		// Add a new menu item
	    menuItem.addActionListener(this);
	    tabRemovePopUpMenu.add(menuItem);
	    
	    // Show the popup
	    tabRemovePopUpMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		// If the right mouse is click on a tab
		if((e.getButton() == 3))
		{
			// Find the 
			for(int i=0;i<maxTabs;i++)
			{
				if(simulationTabs[i] == this.getSelectedComponent())
				{
					selectedTabIndex=i;
					showPopUP(e);
				}
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
		removeTab();
	}

	@Override
	public void tabStatusChanged(SimulationTabPanel tab,SimStatus status)
	{
		int index = this.indexOfComponent(tab);
				
	  	if(status == SimStatus.RUNNING)
	  	{
	  		this.setIconAt(index, simRunningIcon);							
	  	}
	  	else if(status == SimStatus.PAUSED)
	  	{
	  		this.setIconAt(index, simPausedIcon);							
					  		
	  	}
	  	else if(status == SimStatus.NEW)
	  	{
	  		this.setIconAt(index, simNewIcon);							

	  	}
	  	else // Finished
	  	{
	  		this.setIconAt(index, simFinishedIcon);							
	  	}
	  	
	  	this.setTitleAt(index, tab.getTitle());
	}
	
}
