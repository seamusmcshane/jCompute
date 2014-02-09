package alifeSim.Gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;

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
	
	private final int maxTabs = 8;
	private int tabCount = 0;
	private int launchCount = 0;
	
	private SimulationInfoTabPanel simulationInfoTab;	
	private JPanel addPanel = new JPanel();
	private SimulationTabPanel simulationTabs[];
	private JPopupMenu tabPopUpMenu;
	private JMenuItem menuItem;

	private int selectedTabIndex = 0;

	private SimulationsManager simsManager;
	
	private Timer tabStatusPoll = new Timer();
	
	public SimulationTabPanelManager(final SimulationsManager simsManager)
	{
		super(LEFT);
		
		this.simsManager = simsManager;
				
		simulationTabs = new SimulationTabPanel[maxTabs];
		
		simulationInfoTab = new SimulationInfoTabPanel();
		
		this.add(simulationInfoTab);
		this.setTitleAt(this.getTabCount()-1, "Status Info");
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
					if(simulationInfoTab == getSelectedComponent())
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

		tabPopUpMenu  = new JPopupMenu();
		this.addMouseListener(this);

		// A slow/low overhead timer to update the tab icons based on the status of the running simulation in that tab.
		tabStatusPoll.schedule(new TimerTask() 
		{
			  @Override
			  public void run() 
			  {
				  refresh();
			  }
			  
		},0,100);
	}
		
	private void refresh()
	{
		  simulationInfoTab.clearTable();
		  
		  for(int i = 0;i<getTabCount();i++)
		  {
			  if(getComponentAt (i)!=null)
			  {
				  if(getComponentAt(i).getClass().equals(SimulationTabPanel.class))
				  {
					  	SimulationTabPanel temp = (SimulationTabPanel) getComponentAt (i);

						if(temp.getState().equals("Running"))
						{
							setIconAt(i, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-start.png")));
							
							simulationInfoTab.addRow(getTitleAt(i), new String[]{temp.getState(), temp.getStepNo(),temp.getASPS(), temp.getTime()});
						}
						else if(temp.getState().equals("Paused"))
						{
							setIconAt(i, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-pause.png")));
							
							simulationInfoTab.addRow(getTitleAt(i), new String[]{temp.getState(), temp.getStepNo(),"0", temp.getTime()});
						}
						else if(temp.getState().equals("New"))
						{
							setIconAt(i, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-stop.png")));
							
							simulationInfoTab.addRow(getTitleAt(i),  new String[]{temp.getState(), temp.getStepNo(),"0", temp.getTime()});

						}
						else
						{
							setIconAt(i, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/task-complete.png")));
							
							simulationInfoTab.addRow(getTitleAt(i), new String[]{temp.getState(), temp.getStepNo(),"0", temp.getTime()});

						}	
				  }	
			  }  
		  }
		  
		  simulationInfoTab.update();
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
					this.setTitleAt(this.getTabCount()-2, "Simulation " + (launchCount+1));
					this.setIconAt(this.getTabCount()-2, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-stop.png")));
					
					this.setSelectedComponent(simulationTabs[i]);
					
					added =true;
					launchCount++;
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
			simulationInfoTab.clearTrace(this.getTitleAt(this.getSelectedIndex()));
			simulationTabs[selectedTabIndex].destroy();

			this.setSelectedIndex(0);
			this.remove(simulationTabs[selectedTabIndex]);
			simulationTabs[selectedTabIndex] = null;	
			tabCount--;
			refresh();
		}
		
	}
	
	private void showPopUP(MouseEvent e)
	{

		tabPopUpMenu.removeAll();
		JMenuItem menuItem = new JMenuItem("Remove " + this.getTitleAt(this.getSelectedIndex()));
		
	    menuItem.addActionListener(this);
	    tabPopUpMenu.add(menuItem);
	    
		tabPopUpMenu.show(e.getComponent(), e.getX(), e.getY());

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
		removeTab();		
	}

}
