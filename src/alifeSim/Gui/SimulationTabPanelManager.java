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

public class SimulationTabPanelManager extends JTabbedPane implements MouseListener, ActionListener
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
	
	/** Simulation Tabs */
	private SimulationTabPanel simulationTabs[];
	
	/** Remove Popup Menu */
	private JPopupMenu tabRemovePopUpMenu;

	/** Current Selected Tab Index ID */
	private int selectedTabIndex;

	/** A Reference to the Simulations Manager */
	private SimulationsManager simsManager;
	
	public SimulationTabPanelManager(final SimulationsManager simsManager)
	{
		/* Tabs on the Left */
		super(LEFT);
		
		this.simsManager = simsManager;
				
		/* Match the Simulations Manager Limits */
		maxTabs = simsManager.getMaxSims();
		simulationTabs = new SimulationTabPanel[maxTabs];
	
		simulationListTab = new SimulationListTabPanel();	
		this.add(simulationListTab);
		this.setTitleAt(this.getTabCount()-1, simulationListTab.getTabName());
		this.setIconAt(this.getTabCount()-1, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/dialog-information.png")));
	
		this.add(addPanel);
		this.setIconAt(this.getTabCount()-1, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/tab-new-background.png")));
		
		// This makes the last tab act as a button, that adds tabs.
		this.addChangeListener(new ChangeListener(){
			
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if( getSelectedComponent() == addPanel)
				{
					// Unselect us asap or we will get called again.
					setSelectedIndex(0);
					
					// call the add tab functionality and we are done.
					addTab();
				}
				else
				{
					if(simulationListTab == getSelectedComponent())
					{						
						simsManager.clearActiveSim();							
					}
					
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
		});

		tabRemovePopUpMenu  = new JPopupMenu();
		this.addMouseListener(this);

	}
			
	public boolean addTab()
	{
		boolean added = false;
		
		if(tabCount<maxTabs)
		{
			for(int i=0;i<maxTabs;i++)
			{
				if(simulationTabs[i] == null)
				{
					simulationTabs[i] = new SimulationTabPanel(simsManager);
					
					this.add(simulationTabs[i],this.getTabCount()-1);

					this.setIconAt(this.getTabCount()-2, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-stop.png")));
					this.setTitleAt(this.getTabCount()-2, "New");
					this.setSelectedComponent(simulationTabs[i]);
					
					added =true;

					tabCount++;
					break;
				}
			}
		}
		else
		{
			System.out.println("Cannot Add more Simulations - Max Concurrent Simulation Limit Reached");
		}

		return added;
	}
	
	public void removeTab()
	{
		String message;
		String title;
		title = "Confirm Simulation Remove";
		message = "Remove \"" + this.getTitleAt(this.getSelectedIndex()) + "\" ?";

		JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		// Center Dialog on the GUI
		JDialog dialog = pane.createDialog(this, title);

		dialog.pack();
		dialog.setVisible(true);

		int value = ((Integer) pane.getValue()).intValue();

		if (value == JOptionPane.YES_OPTION)
		{
			simulationListTab.clearTrace(this.getTitleAt(this.getSelectedIndex()));
			simulationTabs[selectedTabIndex].removeSim();

			this.setSelectedIndex(0);
			this.remove(simulationTabs[selectedTabIndex]);
			simulationTabs[selectedTabIndex] = null;	
			tabCount--;
		}
		
	}
	
	private void showPopUP(MouseEvent e)
	{
		tabRemovePopUpMenu.removeAll();
		JMenuItem menuItem = new JMenuItem("Remove " + this.getTitleAt(this.getSelectedIndex()));
		
	    menuItem.addActionListener(this);
	    tabRemovePopUpMenu.add(menuItem);
	    
	    tabRemovePopUpMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{				
		if((e.getButton() == 3))
		{
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
	
}
