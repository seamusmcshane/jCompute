package alifeSim.Gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SimulationTabPanelManager extends JTabbedPane implements MouseListener, ActionListener
{
	private static final long serialVersionUID = 1L;
	
	private final int maxTabs = 5;
	private int tabCount = 0;
	private int launchCount = 0;
	
	private SimulationInfoTabPanel simulationInfoTab;	
	private JPanel addPanel = new JPanel();
	private SimulationTabPanel simulationTabs[];
	private JPopupMenu tabPopUpMenu;
	private JMenuItem menuItem;

	private int selectedTabIndex = 0;

	public SimulationTabPanelManager(int val)
	{
		super(val);
		
		simulationTabs = new SimulationTabPanel[maxTabs];
		
		simulationInfoTab = new SimulationInfoTabPanel();
		
		this.add(simulationInfoTab);
		this.setTitleAt(this.getTabCount()-1, "Status Info");
		
		this.add(addPanel);
		this.setTitleAt(this.getTabCount()-1, " + ");
		
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
			}
		});

		tabPopUpMenu  = new JPopupMenu();
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
					simulationTabs[i] = new SimulationTabPanel();	
					this.add(simulationTabs[i],this.getTabCount()-1);
					this.setTitleAt(this.getTabCount()-2, "Simulation " + (launchCount+1));

					
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
			this.remove(simulationTabs[selectedTabIndex]);
			simulationTabs[selectedTabIndex] = null;	
			tabCount--;
			this.setSelectedIndex(0);
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
		
		//System.out.println("Clicked");
		
		if((e.getButton() == 3))
		{
			for(int i=0;i<maxTabs;i++)
			{
				if(simulationTabs[i] == this.getSelectedComponent())
				{
					selectedTabIndex=i;
					System.out.println(" " + this.getSelectedIndex());
					System.out.println(" " + selectedTabIndex);
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
