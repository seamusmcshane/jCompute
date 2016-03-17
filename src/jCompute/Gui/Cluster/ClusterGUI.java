package jCompute.Gui.Cluster;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.IconManager;
import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Gui.Component.Swing.AboutWindow;
import jCompute.Gui.Component.Swing.BenchmarkWindow;
import jCompute.Gui.Component.Swing.SimpleTabPanel;
import jCompute.Gui.Component.Swing.SimpleTabTabTitle;
import jCompute.Scenario.ScenarioManager;

public class ClusterGUI implements WindowListener
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ClusterGUI.class);

	// Main Frame
	private JFrame guiFrame;

	// Menu Bar
	private JMenuBar menuBar;
	private JMenuItem mntmQuit;

	private JMenu mnHelp;
	private JMenuItem mntmBenchmnark;
	private JMenuItem mntmAbout;

	private int rightPanelsMinWidth = 400;

	// GUI Tabs
	private SimpleTabPanel guiTabs;
	private BatchTab batchTab;
	private ClusterTab clusterTab;

	/* Icons */
	ImageIcon batchTabIcon = IconManager.getIcon("list");
	ImageIcon clusterIcon = IconManager.getIcon("Nodes32");

	public ClusterGUI(final boolean buttonText, BatchManager batchManager)
	{
		try
		{
			javax.swing.SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					createFrame(batchManager);

					createAndAddTabs(buttonText);

					guiFrame.getContentPane().add(guiTabs, BorderLayout.CENTER);

					// Show Frame
					guiFrame.setVisible(true);

					log.info("Created GUI");

					batchTab.setBatchManager(batchManager);

					// GUI is ready, now start BatchManager
					batchManager.start();
				}
			});
		}
		catch(InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private void createFrame(BatchManager batchManager)
	{
		// Frame
		guiFrame = new JFrame("Cluster Interface");
		guiFrame.getContentPane().setLayout(new BorderLayout());
		guiFrame.setMinimumSize(new Dimension(900, 700));
		guiFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Window Closing
		guiFrame.addWindowListener(this);

		// Menu Bar
		createMenuBar(batchManager);
		guiFrame.setJMenuBar(menuBar);

	}

	public void createAndAddTabs(boolean buttonText)
	{
		int tabWidth = 100;

		guiTabs = new SimpleTabPanel(SimpleTabPanel.LEFT);

		batchTab = new BatchTab(rightPanelsMinWidth, buttonText);

		guiTabs.addTab(batchTab, new SimpleTabTabTitle(tabWidth, batchTabIcon, "Batch"));

		clusterTab = new ClusterTab(rightPanelsMinWidth);

		guiTabs.addTab(clusterTab, new SimpleTabTabTitle(tabWidth, clusterIcon, "Cluster"));
	}

	public void createMenuBar(BatchManager batchManager)
	{
		menuBar = new JMenuBar();

		JMenu mnFileMenu = new JMenu("File");
		menuBar.add(mnFileMenu);

		mntmQuit = new JMenuItem("Quit");
		mnFileMenu.add(mntmQuit);
		mntmQuit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				doProgramExit();
			}
		});

		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		if(ScenarioManager.hasScenario("SAPPv2"))
		{
			mntmBenchmnark = new JMenuItem("Benchmark");
			mnHelp.add(mntmBenchmnark);
			mntmBenchmnark.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					BenchmarkWindow test = new BenchmarkWindow(batchManager);
					test.pack();
					test.setLocationRelativeTo(guiFrame);
					test.setVisible(true);
				}
			});

			log.info("SAPPv2 found benchmark enabled");
		}
		else
		{
			log.warn("SAPPv2 not found benchmark disabled");
		}

		mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		mntmAbout.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				AboutWindow about = new AboutWindow();
				about.setLocationRelativeTo(guiFrame);
				about.setVisible(true);
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

	/* Ensure the user wants to exit then exit the program */
	private void doProgramExit()
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
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
