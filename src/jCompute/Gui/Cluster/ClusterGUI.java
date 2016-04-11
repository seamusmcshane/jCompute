package jCompute.Gui.Cluster;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jCompute.IconManager;
import jCompute.IconManager.IconIndex;
import jCompute.Cluster.BatchManager.BatchManager;
import jCompute.Gui.Cluster.Tab.BatchTab;
import jCompute.Gui.Cluster.Tab.BenchmarkTab;
import jCompute.Gui.Cluster.Tab.ClusterTab;
import jCompute.Gui.Component.swing.jdialog.AboutWindow;
import jCompute.Gui.Component.swing.jpanel.GlobalProgressMonitor;
import jCompute.Gui.Component.swing.jpanel.SimpleTabPanel;
import jCompute.Gui.Component.swing.jpanel.SimpleTabTabTitle;
import jCompute.Gui.Component.swing.logtab.LogTab;
import jCompute.Scenario.ScenarioPluginManager;

public class ClusterGUI implements WindowListener
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ClusterGUI.class);
	
	// Main Frame
	private JFrame guiFrame;
	
	// Menu Bar
	private JMenuBar menuBar;
	private JMenuItem mntmQuit;
	private JMenu mnTab;
	
	private JMenu mnHelp;
	private JMenuItem mntmBenchmnark;
	private JMenuItem mntmAbout;
	
	private final int rightPanelsMinWidth = 400;
	private final int tabWidth = 100;
	
	// Permanent Tabs
	private SimpleTabPanel guiTabs;
	private BatchTab batchTab;
	private ClusterTab clusterTab;
	private GlobalProgressMonitor globalMuliplexedProgressBar;

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

					globalMuliplexedProgressBar = GlobalProgressMonitor.getInstance();

					guiFrame.getContentPane().add(globalMuliplexedProgressBar, BorderLayout.SOUTH);

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
		guiTabs = new SimpleTabPanel(SimpleTabPanel.LEFT);
		
		batchTab = new BatchTab(rightPanelsMinWidth, buttonText);
		
		guiTabs.addTab(batchTab, new SimpleTabTabTitle(tabWidth, IconManager.retrieveIcon(IconIndex.batchTab32), "Batch"));
		
		clusterTab = new ClusterTab(rightPanelsMinWidth);
		
		guiTabs.addTab(clusterTab, new SimpleTabTabTitle(tabWidth, IconManager.retrieveIcon(IconIndex.clusterTab32), "Cluster"));
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
		
		mnTab = new JMenu("Tabs");
		
		if(ScenarioPluginManager.hasScenario("SAPPv2"))
		{
			mntmBenchmnark = new JMenuItem("Benchmark");
			mnTab.add(mntmBenchmnark);
			mntmBenchmnark.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if(!guiTabs.hasTabComponentWithClass(BenchmarkTab.class))
					{
						BenchmarkTab benchmark = new BenchmarkTab(batchManager, guiTabs);
						
						guiTabs.addTab(benchmark, new SimpleTabTabTitle(tabWidth, IconManager.retrieveIcon(IconIndex.benchmarkTab32), "Benchmark"));
						
						guiTabs.setSelectedTab(benchmark);
					}
					else
					{
						guiTabs.setSelectedTab(BenchmarkTab.class);
					}
				}
			});
			
			log.info("SAPPv2 found benchmark enabled");
		}
		else
		{
			log.warn("SAPPv2 not found benchmark disabled");
		}
		
		JMenuItem mntmLogging = new JMenuItem("Logging");
		mnTab.add(mntmLogging);
		mntmLogging.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!guiTabs.hasTabComponentWithClass(LogTab.class))
				{
					LogTab logTab = new LogTab(guiTabs);
					
					guiTabs.addTab(logTab, new SimpleTabTabTitle(tabWidth, IconManager.retrieveIcon(IconIndex.loggingTab32), "Logging"));
					guiTabs.setSelectedTab(logTab);
					logTab.start();
				}
				else
				{
					guiTabs.setSelectedTab(LogTab.class);
				}
			}
		});
		
		menuBar.add(mnTab);
		
		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
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
