package alifeSim.Gui;

import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JSplitPane;
import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Simulation.SimulationsManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

public class GUI  implements ActionListener, ItemListener, WindowListener
{
	// Main Frame
	private JFrame guiFrame;
	
	// SplitPane Container
	JPanel containerPanel;
		
	// Split Pane
	private JSplitPane splitPane;
	
	// Tab Manager (Left Split)
	private GUITabManager simTabs;
			
	// Simulation View (Right Split)
	private GUISimulationView simView;
	
	private SimulationsManager simsManager;

	// Menu Bar
	private JMenuBar menuBar;
	
	// Menus
	private JMenu mnFile,mnView,mnAgentDrawing,mnHelp;

	// Menu Items
	private JMenuItem mntmQuit, mntmAbout;
	
	// Menu Check Boxes
	private JCheckBoxMenuItem chckbxmntmDisplaySimulation,chckbxmntmDrawFieldOf,chckbxmntDrawAgentViews;
		
	public GUI(SimulationsManager simsManager)
	{
		this.simsManager = simsManager;

		// Generate the GUI
	    try
		{
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() 
			{
			    public void run() 
			    {
					lookandFeel();
					
					createGUIComponents();
					
			    	registerGUIListeners();
			    	
					layoutGUI();
					
			    }
			});
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			System.out.println("Failed to Create GUI");
		}
	    
	    // Make it Visible
	    javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
		    public void run() 
		    {
				/* We control the exit */
				guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
				guiFrame.setVisible(true);
				guiFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		    }
		});    
	}
		
	private void addView()
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
		    public void run() 
		    {
				if(simView == null)
				{
					/* Simulation View */
					simView = new GUISimulationView();
					
					/* Link up the view with the simulation manager */
					simsManager.setSimView(simView);
					
					/* Add the View to the right split pane */
					splitPane.setRightComponent(simView.getAwtCanvas());
				}
		    }
		});
	}
	
	private void removeView()
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
		    public void run() 
		    {
				simsManager.clearActiveSim();
				
				simsManager.setSimView(null);
				
				splitPane.setRightComponent(null);
				
				simView.exitDisplay();
				
				simView = null;

		    }
		});

	}
	
	
	private void createGUIComponents()
	{
		/* Frame */
		guiFrame = new JFrame();
		guiFrame.setMinimumSize(new Dimension(800,600));
				
		/* Container for Split Pane */		
		containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout(0, 0));
		
		/* Split Pane */
		splitPane = new JSplitPane();
		splitPane.setDividerSize(8);
		splitPane.setDoubleBuffered(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		
		/* Sim Tabs */
		simTabs = new GUITabManager(simsManager);
		simTabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		
		/* Menu Bar */
		menuBar = new JMenuBar();
		
		mnFile = new JMenu("File");
		mnView = new JMenu("View");
		mnAgentDrawing = new JMenu("Agent Drawing");
		mnHelp = new JMenu("Help");

		mntmQuit = new JMenuItem("Quit");
		mntmAbout = new JMenuItem("About");

		chckbxmntmDisplaySimulation = new JCheckBoxMenuItem("Display Simulation");
		chckbxmntmDisplaySimulation.setSelected(true);
		chckbxmntmDrawFieldOf = new JCheckBoxMenuItem("Draw Field of Views");
		chckbxmntDrawAgentViews = new JCheckBoxMenuItem("Draw Agent Views");
		
	}
	
	private void layoutGUI()
	{		
		// Add Container to Frame
		guiFrame.getContentPane().add(containerPanel, BorderLayout.CENTER);
		
		// Add SplitPane to Container
		containerPanel.add(splitPane);

		/* Split Pane Left - Sim Tabs */		
		splitPane.setLeftComponent(simTabs);
		
		// creates and add the view (invoke later)
		addView();
		
		/* Menu Bar */
		addMenu();
		
	}
	
	private void registerGUIListeners()
	{
		mntmQuit.addActionListener(this);		
		mntmAbout.addActionListener(this);

		chckbxmntmDisplaySimulation.addItemListener(this);
		chckbxmntmDrawFieldOf.addItemListener(this);		
		chckbxmntDrawAgentViews.addItemListener(this);
		
		/* Window Closing */
		guiFrame.addWindowListener(this);
		
	}
	
	private void addMenu()
	{
		menuBar.add(mnFile);
		menuBar.add(mnView);
		menuBar.add(mnHelp);
		mnFile.add(mntmQuit);
		mnHelp.add(mntmAbout);
		
		mnView.add(chckbxmntmDisplaySimulation);
		mnView.add(mnAgentDrawing);
		
		mnAgentDrawing.add(chckbxmntmDrawFieldOf);
		mnAgentDrawing.add(chckbxmntDrawAgentViews);

		guiFrame.setJMenuBar(menuBar);		
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
		    public void run() 
		    {
				if(e.getSource() == mntmQuit)
				{
					doProgramExit();
				}
				else if(e.getSource() == mntmAbout)
				{
					// TODO
					System.out.println("About");
				}
				else
				{
					System.out.println("Unknown Event Source :" + e.getSource().getClass().getName());
				}	
		    }
		});
		
	}

	@Override
	public void itemStateChanged(final ItemEvent e)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
		    public void run() 
		    {
				if(e.getSource() == chckbxmntmDisplaySimulation)
				{
					if (chckbxmntmDisplaySimulation.isSelected())
					{
						// have been checked
					addView();
					}
					else
					{
						// have been unchecked
						removeView();
					}
				}
				else if(e.getSource() == chckbxmntmDrawFieldOf)
				{
					if (chckbxmntmDrawFieldOf.isSelected())
					{
						if(simView !=null)
						{
							// have been checked
							simView.setViewRangeDrawing(true);
						}
					}
					else
					{
						if(simView !=null)
						{
							// have been unchecked
							simView.setViewRangeDrawing(false);
						}
					}			
				}
				else if(e.getSource() == chckbxmntDrawAgentViews)
				{
					if (chckbxmntDrawAgentViews.isSelected())
					{
						if(simView !=null)
						{
							// have been checked
							simView.setViewsDrawing(true);
						}
					}
					else
					{
						if(simView !=null)
						{
							// have been unchecked
							simView.setViewsDrawing(false);
						}
					}
				}
				else
				{
					System.out.println("Unknown Event Source :" + e.getSource().getClass().getName());
				}
		    }
		});
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

	@Override
	public void windowActivated(WindowEvent arg0)
	{		
	}

	@Override
	public void windowClosed(WindowEvent arg0)
	{		
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
		    public void run() 
		    {
				// Exit the sim
				doProgramExit();	
		    }
		}
		);
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
			
			if(simView !=null)
			{
				simView.exitDisplay(); 
			}

			// Do EXIT
			System.exit(0);
		}

	}
	
	@Override
	public void windowDeactivated(WindowEvent arg0)
	{		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0)
	{		
	}

	@Override
	public void windowIconified(WindowEvent arg0)
	{		
	}

	@Override
	public void windowOpened(WindowEvent arg0)
	{		
	}
	
}
