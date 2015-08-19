package jCompute.Gui.Cluster;

import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Gui.Component.Swing.AboutWindow;
import jCompute.Gui.Component.Swing.SimpleTabPanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterGUI implements ActionListener, ItemListener, WindowListener
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ClusterGUI.class);
	
	// Batch Manager
	private BatchManager batchManager;
	
	// Main Frame
	private JFrame guiFrame;
	
	// Menu Bar
	private JMenuBar menuBar;
	private JMenuItem mntmQuit;
	
	private JMenu mnHelp;
	private JMenuItem mntmAbout;
	
	private int rightPanelsMinWidth = 400;
	
	// GUI Tabs
	private SimpleTabPanel guiTabs;
	private BatchTab batchTab;
	private ClusterStatusTab clusterStatusTab;
	private NodeStatusTab nodeStatusTab;
	
	public ClusterGUI(final boolean buttonText, boolean allowMulti)
	{
		try
		{
			javax.swing.SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					createFrame();
					
					createAndAddTabs(buttonText);
					
					guiFrame.getContentPane().add(guiTabs, BorderLayout.CENTER);
					
					// Show Frame
					guiFrame.setVisible(true);
					
					log.info("Created GUI");
				}
			});
		}
		catch(InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
		}
		
		batchManager = new BatchManager(allowMulti);
		
		batchTab.setBatchManager(batchManager);
	}
	
	private void createFrame()
	{
		// Frame
		guiFrame = new JFrame("Cluster Interface");
		guiFrame.getContentPane().setLayout(new BorderLayout());
		guiFrame.setMinimumSize(new Dimension(900, 700));
		guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		// Window Closing
		guiFrame.addWindowListener(this);
		
		// Menu Bar
		createMenuBar();
		guiFrame.setJMenuBar(menuBar);
		
	}
	
	public void createAndAddTabs(boolean buttonText)
	{
		guiTabs = new SimpleTabPanel();
		
		batchTab = new BatchTab(rightPanelsMinWidth, buttonText);
		
		guiTabs.addTab(batchTab, "Batches");
		
		clusterStatusTab = new ClusterStatusTab(rightPanelsMinWidth);
		
		guiTabs.addTab(clusterStatusTab, "Cluster");
		
		nodeStatusTab = new NodeStatusTab(rightPanelsMinWidth);
		
		guiTabs.addTab(nodeStatusTab, "Nodes");
	}
	
	public void createMenuBar()
	{
		menuBar = new JMenuBar();
		
		JMenu mnFileMenu = new JMenu("File");
		menuBar.add(mnFileMenu);
		
		mntmQuit = new JMenuItem("Quit");
		mnFileMenu.add(mntmQuit);
		mntmQuit.addActionListener(this);
		
		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		mntmAbout.addActionListener(this);
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
		// Exit the sim
		doProgramExit();
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
	
	@Override
	public void itemStateChanged(ItemEvent arg0)
	{
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
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
	}
	
	/* Ensure the user wants to exit then exit the program */
	private void doProgramExit()
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
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
					log.info("Application exit requested.");
					// Do EXIT
					System.exit(0);
				}
			}
		});
		
	}
}
