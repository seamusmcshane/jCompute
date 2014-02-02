package alifeSim.Gui;

import java.awt.Dimension;
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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class NewGUI 
{
	private static JFrame guiFrame;
	private static SimulationTabPanelManager simTabs;
	private static JSplitPane splitPane;
	private static NewSimView simView;
	private static JCheckBoxMenuItem chckbxmntmDisplaySimulation,chckbxmntmDrawFieldOf,chckbxmntDrawAgentViews;
	private static JMenu mnFrameRate,mnVerticalSync,mnOverlay;
	private static JRadioButtonMenuItem rdbtnmntm15FramesPerSecond,rdbtnmntm60FramesPerSecond,rdbtnmntmUnlimitedFrameRate,rdbtnmntmVsyncOn,rdbtnmntmVsyncOff,rdbtnmntmOverlayDisabled,rdbtnmntmOverlayEnabled;
	private static final ButtonGroup frameRateButtonGroup = new ButtonGroup();
	private static final ButtonGroup vSyncButtonGroup = new ButtonGroup();
	private static final ButtonGroup overlayButtonGroup = new ButtonGroup();
			
	public static void main(String args[])
	{
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
	
	private static void setUpMenu()
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
					NewSimView.setVisible(true);
				}
				else
				{
					// have been unchecked
					NewSimView.setVisible(false);
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
					NewSimView.setViewRangeDrawing(true);
				}
				else
				{
					// have been unchecked
					NewSimView.setViewRangeDrawing(false);
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
					NewSimView.setViewsDrawing(true);
				}
				else
				{
					// have been unchecked
					NewSimView.setViewsDrawing(false);
				}
			}
		});
		mnAgentDrawing.add(chckbxmntDrawAgentViews);

		mnFrameRate = new JMenu("Frame Rate");
		mnView.add(mnFrameRate);

		rdbtnmntm15FramesPerSecond = new JRadioButtonMenuItem("15 Frames Per Second");
		rdbtnmntm15FramesPerSecond.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// Change the frame rate of the view to standard (15)
				NewSimView.setStandardUpdateRate();
			}
		});
		frameRateButtonGroup.add(rdbtnmntm15FramesPerSecond);
		mnFrameRate.add(rdbtnmntm15FramesPerSecond);
		rdbtnmntm15FramesPerSecond.setSelected(true);

		rdbtnmntm60FramesPerSecond = new JRadioButtonMenuItem("60 Frames Per Second");
		
		rdbtnmntm60FramesPerSecond.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Change the frame rate of the view to high (60)
				NewSimView.setHighUpdateRate();
			}
		});
		frameRateButtonGroup.add(rdbtnmntm60FramesPerSecond);
		mnFrameRate.add(rdbtnmntm60FramesPerSecond);

		rdbtnmntmUnlimitedFrameRate = new JRadioButtonMenuItem("Unlimited");
		rdbtnmntmUnlimitedFrameRate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Unlock the frame rate of the view - as fast as your computer
				// can churn them out.
				SimulationView.setUnlimitedUpdateRate();
			}
		});

		frameRateButtonGroup.add(rdbtnmntmUnlimitedFrameRate);
		mnFrameRate.add(rdbtnmntmUnlimitedFrameRate);

		mnVerticalSync = new JMenu("Vertical Sync");
		mnView.add(mnVerticalSync);

		rdbtnmntmVsyncOn = new JRadioButtonMenuItem("VSync On");
		vSyncButtonGroup.add(rdbtnmntmVsyncOn);
		rdbtnmntmVsyncOn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Sync frames to the monitor refresh
				NewSimView.setVerticalSync(true);
			}
		});
		rdbtnmntmVsyncOn.setSelected(true);
		mnVerticalSync.add(rdbtnmntmVsyncOn);

		rdbtnmntmVsyncOff = new JRadioButtonMenuItem("VSync Off");
		
		vSyncButtonGroup.add(rdbtnmntmVsyncOff);
		rdbtnmntmVsyncOff.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Dont Sync frames to the monitor refresh
				NewSimView.setVerticalSync(false);
			}
		});
		mnVerticalSync.add(rdbtnmntmVsyncOff);

		mnOverlay = new JMenu("Overlay");
		mnView.add(mnOverlay);

		rdbtnmntmOverlayEnabled = new JRadioButtonMenuItem("Enabled");
		rdbtnmntmOverlayEnabled.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Enable some view debug text which may be of interest
				NewSimView.setViewOverLay(true);
			}
		});
		overlayButtonGroup.add(rdbtnmntmOverlayEnabled);
		mnOverlay.add(rdbtnmntmOverlayEnabled);

		rdbtnmntmOverlayDisabled = new JRadioButtonMenuItem("Disabled");
		rdbtnmntmOverlayDisabled.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Disable the view debug text
				NewSimView.setViewOverLay(false);
			}
		});
		rdbtnmntmOverlayDisabled.setSelected(true);
		overlayButtonGroup.add(rdbtnmntmOverlayDisabled);
		mnOverlay.add(rdbtnmntmOverlayDisabled);

	}
	
	private static void setUpGUI()
	{
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
		
		simView = new NewSimView();
		
		/* Split Pane Left - Sim Tabs */
		simTabs = new SimulationTabPanelManager(simView);
		simTabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		splitPane.setLeftComponent(simTabs);

		
		
		//splitPane.setRightComponent(SimulationView.getView(null, guiFrame.getWidth() , guiFrame.getHeight()));
		
		splitPane.setRightComponent(simView.getAwtCanvas());
		
		/* We control the exit */
		guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
		
		guiFrame.setVisible(true);
		//guiFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
	}
	
	private static void registerGUIListeners()
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
	private static void doProgramExit()
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
	private static void lookandFeel()
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
