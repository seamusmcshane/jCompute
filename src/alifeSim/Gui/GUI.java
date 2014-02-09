package alifeSim.Gui;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JSplitPane;

import alifeSim.Simulation.SimulationsManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class GUI 
{
	// Main Frame
	private JFrame guiFrame;
	
	// Menu Check Boxes
	private JCheckBoxMenuItem chckbxmntmDisplaySimulation,chckbxmntmDrawFieldOf,chckbxmntDrawAgentViews;
	
	// Split Pane
	private JSplitPane splitPane;
	
	// Tab Manager (Left Split)
	private SimulationTabPanelManager simTabs;
			
	// Simulation View (Right Split)
	private NewSimView simView;
	
	private SimulationsManager simsManager;
	
	public GUI(SimulationsManager simsManager)
	{
		this.simsManager = simsManager;
		
	    javax.swing.SwingUtilities.invokeLater(new Runnable() 
	    {
	        public void run() 
	        {
	    		lookandFeel();
	    		
	    		setUpGUI();
	    		
	    		registerGUIListeners();
	        }
	    });
	}
	
	private void setUpMenu()
	{
		JMenuBar menuBar = new JMenuBar();
		guiFrame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// Exit the sim
				doProgramExit();
			}
		});
		mnFile.add(mntmQuit);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// Exit the sim
				System.out.println("About");
			}
		});
		mnHelp.add(mntmAbout);



		chckbxmntmDisplaySimulation = new JCheckBoxMenuItem("Display Simulation");
		chckbxmntmDisplaySimulation.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				if (chckbxmntmDisplaySimulation.isSelected())
				{
					// have been checked
					simView.setVisible(true);
				}
				else
				{
					// have been unchecked
					simView.setVisible(false);
				}

			}
		});
		
		mnView.add(chckbxmntmDisplaySimulation);
		chckbxmntmDisplaySimulation.setSelected(true);

		JMenu mnAgentDrawing = new JMenu("Agent Drawing");
		mnView.add(mnAgentDrawing);

		chckbxmntmDrawFieldOf = new JCheckBoxMenuItem("Draw Field of Views");
		chckbxmntmDrawFieldOf.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				if (chckbxmntmDrawFieldOf.isSelected())
				{
					// have been checked
					simView.setViewRangeDrawing(true);
				}
				else
				{
					// have been unchecked
					simView.setViewRangeDrawing(false);
				}
			}
		});
		mnAgentDrawing.add(chckbxmntmDrawFieldOf);

		chckbxmntDrawAgentViews = new JCheckBoxMenuItem("Draw Agent Views");
		chckbxmntDrawAgentViews.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				if (chckbxmntDrawAgentViews.isSelected())
				{
					// have been checked
					simView.setViewsDrawing(true);
				}
				else
				{
					// have been unchecked
					simView.setViewsDrawing(false);
				}
			}
		});
		mnAgentDrawing.add(chckbxmntDrawAgentViews);

	}
	
	private void setUpGUI()
	{
		
		/* Simulation View */
		simView = new NewSimView();
		
		/* Link up the view with the simulation manager */
		simsManager.setSimView(simView);
		
		/* Frame */
		guiFrame = new JFrame();
		guiFrame.setMinimumSize(new Dimension(800,600));
		
		/* Menu Bar */
		setUpMenu();
		
		/* Container for Split Pane */		
		JPanel containerPanel = new JPanel();
		guiFrame.getContentPane().add(containerPanel, BorderLayout.CENTER);
		containerPanel.setLayout(new BorderLayout(0, 0));
		
		/* Split Pane */
		splitPane = new JSplitPane();
		containerPanel.add(splitPane);
		splitPane.setDividerSize(10);
		splitPane.setDoubleBuffered(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		
		/* Add the View to the right split pane */
		splitPane.setRightComponent(simView.getAwtCanvas());
		
		/* Split Pane Left - Sim Tabs */
		simTabs = new SimulationTabPanelManager(simsManager);
		simTabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		splitPane.setLeftComponent(simTabs);
		
		/* We control the exit */
		guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
		
		guiFrame.setVisible(true);
		guiFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
	}
	
	private void registerGUIListeners()
	{
		/* Window Closing */
		guiFrame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				// Exit the sim
				doProgramExit();
			}

		});
		
	}
	
	/* Ensure the user wants to exit then exit the program */
	private void doProgramExit()
	{

		String message;
		message = "Do you want to quit?";

		JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		// Center Dialog on the GUI
		JDialog dialog = pane.createDialog(guiFrame, "Close Application");

		dialog.pack();
		dialog.setVisible(true);

		int value = ((Integer) pane.getValue()).intValue();

		if (value == JOptionPane.YES_OPTION)
		{
			simView.exitDisplay(); // Tell OpenGL we are done and free
											// the resources used in the canvas.
											// - must be done else sim will
											// lockup.
			// Do EXIT
			System.exit(0);
		}

	}
	
	/* Use the java provided system look and feel */
	private void lookandFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (InstantiationException e1)
		{
			e1.printStackTrace();
		}
		catch (IllegalAccessException e1)
		{
			e1.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e1)
		{
			e1.printStackTrace();
		}
	}
		
}