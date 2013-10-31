package alifeSim.Gui;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JButton;
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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class NewGUI 
{
	private static JFrame guiFrame;
	private static SimulationTabPanelManager simTabs;
	
	public static void main(String args[])
	{
		lookandFeel();
		
		setUpGUI();
		
		registerGUIListeners();
	}
	
	private static void setUpGUI()
	{
		/* Frame */
		guiFrame = new JFrame();
		guiFrame.setMinimumSize(new Dimension(1300,800));
		
		/* Menu Bar */
		JMenuBar menuBar = new JMenuBar();
		guiFrame.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpenScenario = new JMenuItem("OpenScenario");
		mnFile.add(mntmOpenScenario);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		
		/* Container for Split Pane */		
		JPanel containerPanel = new JPanel();
		guiFrame.getContentPane().add(containerPanel, BorderLayout.CENTER);
		containerPanel.setLayout(new BorderLayout(0, 0));
		
		/* Split Pane */
		JSplitPane splitPane = new JSplitPane();
		containerPanel.add(splitPane);
		splitPane.setDividerSize(10);
		splitPane.setDoubleBuffered(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		
		/* Split Pane Left - Sim Tabs */
		simTabs = new SimulationTabPanelManager(JTabbedPane.LEFT);
		simTabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		splitPane.setLeftComponent(simTabs);
		
		splitPane.setRightComponent(SimulationView.displayView(null, guiFrame.getWidth() , guiFrame.getHeight()));
		
		JToolBar mainToolBar = new JToolBar();
		mainToolBar.setFloatable(false);
		containerPanel.add(mainToolBar, BorderLayout.NORTH);
		
		JButton btnNewButton = new JButton("Open Scenario");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				simTabs.addTab();
			}
		});
		mainToolBar.add(btnNewButton);
		//guiFrame.getContentPane().add(SimulationView.displayView(null, guiFrame.getWidth() , guiFrame.getHeight()),BorderLayout.CENTER);
		
		//guiFrame.getContentPane().add(SimulationView.displayView(sim, gui.getWidth() - statsPanelWidth, gui.getHeight()), BorderLayout.CENTER);
		
		/* We control the exit */
		guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
		
		guiFrame.setVisible(true);
		//guiFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		SimulationView.startView();
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
			SimulationView.exitDisplay(); // Tell OpenGL we are done and free
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
