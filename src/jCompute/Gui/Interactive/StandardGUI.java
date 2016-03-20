package jCompute.Gui.Interactive;

import jCompute.Gui.Component.Swing.AboutWindow;
import jCompute.Gui.View.View;
import jCompute.SimulationManager.SimulationsManagerInf;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JSplitPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

public class StandardGUI implements ActionListener, WindowListener
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(StandardGUI.class);
	
	// Main Frame
	private JFrame guiFrame;

	// SplitPane Container
	private JPanel containerPanel;

	// Split Pane
	private JSplitPane splitPane;

	// Tab Manager (Left Split)
	private GUITabManager simTabs;

	// Simulation View (Right Split)
	private View simView;

	private SimulationsManagerInf simsManager;

	// Menu Bar
	private JMenuBar menuBar;

	// Menus
	private JMenu mnFile, mnHelp;

	// Menu Items
	private JMenuItem mntmQuit, mntmAbout;

	public StandardGUI(SimulationsManagerInf simsManager)
	{
		this.simsManager = simsManager;

		// Generate the GUI
		try
		{
			javax.swing.SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					createGUIComponents();

					registerGUIListeners();

					layoutGUI();
					
					/* We control the exit */
					guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					
					// Make it Visible
					guiFrame.setVisible(true);
					guiFrame.setExtendedState(Frame.MAXIMIZED_BOTH);

					log.info("Created Standard GUI");
				}
			});
		}
		catch(InvocationTargetException | InterruptedException e)
		{
			log.error("Failed to Create GUI");
		}
		
	}

	private void addView()
	{
		/* Simulation View */
		simView = new View();

		/* Link up the view with the simulation manager */
		simsManager.setSimView(simView);

		/* Add the View to the right split pane */
		splitPane.setRightComponent(simView.getCanvas());
	}

	private void createGUIComponents()
	{
		/* Frame */
		guiFrame = new JFrame();
		guiFrame.setMinimumSize(new Dimension(800, 600));

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
		mnHelp = new JMenu("Help");

		mntmQuit = new JMenuItem("Quit");
		mntmAbout = new JMenuItem("About");
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
		
		/* Window Closing */
		guiFrame.addWindowListener(this);
	}

	private void addMenu()
	{
		menuBar.add(mnFile);
		menuBar.add(mnHelp);
		mnFile.add(mntmQuit);
		mnHelp.add(mntmAbout);
		guiFrame.setJMenuBar(menuBar);
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if(e.getSource() == mntmQuit)
				{
					doProgramExit();
				}
				else if(e.getSource() == mntmAbout)
				{
					AboutWindow jvmInfo = new AboutWindow();
					jvmInfo.setLocationRelativeTo(guiFrame);
					jvmInfo.pack();
					jvmInfo.setVisible(true);
				}
				else
				{
					log.error("Unknown Event Source :" + e.getSource().getClass().getName());
				}
			}
		});

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
			@Override
			public void run()
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

		if(value == JOptionPane.YES_OPTION)
		{

			if(simView != null)
			{
				simView.stopDisplay();
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
