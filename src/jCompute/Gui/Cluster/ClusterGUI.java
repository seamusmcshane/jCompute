package jCompute.Gui.Cluster;

import jCompute.IconManager;
import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Gui.Component.Swing.JComputeProgressMonitor;
import jCompute.Gui.Component.Swing.SimpleTabPanel;
import jCompute.Gui.Component.Swing.XMLPreviewPanel;
import jCompute.util.FileUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.JButton;

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
	private JMenuItem mntmQuit;

	// Menu Bar
	private JMenuBar menuBar;

	// Batch Add
	private JComputeProgressMonitor openBatchProgressMonitor;
	private OpenBatchFileTask openBatchProgressMonitorTask;
	private JToolBar toolBar;
	
	// Batch Gen
	private JComputeProgressMonitor genComboMonitor;

	// Toolbar
	private JButton btnAdd;
	private JButton btnRemove;
	private JButton btnStart;
	private JButton btnPause;
	private JButton btnMoveForward;
	private JButton btnMoveFirst;
	private JButton btnMoveBackward;
	private JButton btnMoveLast;
	private JButton btnHighpriority;
	private JButton btnStandardpriority;

	// Icons or Text Only
	private boolean buttonText = true;

	private int rightPanelsMinWidth = 400;

	// GUI Tabs
	private SimpleTabPanel guiTabs;
	private BatchTab batchTab;
	private ClusterStatusTab clusterStatusTab;
	private NodeStatusTab nodeStatusTab;
	
	public ClusterGUI(boolean buttonText)
	{
		log.info("Started ClusterGUI");

		this.buttonText = buttonText;

		createFrame();
		
		openBatchProgressMonitor = new JComputeProgressMonitor(guiFrame, "Loading BatchFiles", 0, 100);
				
		batchManager = new BatchManager();

		createAndAddTabs();

		guiFrame.getContentPane().add(guiTabs, BorderLayout.CENTER);
		
		// Show Frame
		guiFrame.setVisible(true);
		
		genComboMonitor = new JComputeProgressMonitor(guiFrame,
				"Generating Batch Combinations", 0, 100);
		
		batchManager.setProgressMonitor(genComboMonitor);
	}

	private void createFrame()
	{
		// Frame
		guiFrame = new JFrame("Batch Interface");
		guiFrame.getContentPane().setLayout(new BorderLayout());
		guiFrame.setMinimumSize(new Dimension(900, 700));
		guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// Window Closing
		guiFrame.addWindowListener(this);

		// Menu Bar
		createMenuBar();
		guiFrame.setJMenuBar(menuBar);

		// Tool Bar
		createToolbar();
		guiFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
	}

	public void createAndAddTabs()
	{
		guiTabs = new SimpleTabPanel();

		batchTab = new BatchTab(batchManager, rightPanelsMinWidth);

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
	}

	public void createToolbar()
	{
		toolBar = new JToolBar();

		toolBar.setFloatable(false);

		btnStart = new JButton();
		btnStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				batchTab.startBatch();
			}
		});

		btnAdd = new JButton();
		btnAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser filechooser = new JFileChooser(new File("./scenarios"));

				filechooser.setFileFilter(FileUtil.batchFileFilter());

				filechooser.setPreferredSize(new Dimension(800, 600));
				filechooser.setMultiSelectionEnabled(true);

				XMLPreviewPanel xmlPreview = new XMLPreviewPanel();
				filechooser.setAccessory(xmlPreview);
				filechooser.addPropertyChangeListener(xmlPreview);
				Action details = filechooser.getActionMap().get("viewTypeDetails");
				details.actionPerformed(null);

				log.info("Batch Open Dialog");

				int val = filechooser.showOpenDialog(filechooser);

				if(val == JFileChooser.APPROVE_OPTION)
				{
					log.info("New Batch Choosen");

					File[] files = filechooser.getSelectedFiles();

					openBatchProgressMonitorTask = new OpenBatchFileTask(openBatchProgressMonitor, files);

					openBatchProgressMonitorTask.start();

				}
			}
		});
		btnAdd.setIcon(IconManager.getIcon("addBatch"));
		toolBar.add(btnAdd);

		btnRemove = new JButton();
		btnRemove.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				batchTab.removeBatch();
			}
		});
		btnRemove.setIcon(IconManager.getIcon("removeBatch"));
		toolBar.add(btnRemove);
		toolBar.addSeparator();

		btnStart.setIcon(IconManager.getIcon("simRunningIcon"));
		toolBar.add(btnStart);

		btnPause = new JButton();
		btnPause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				batchTab.pauseBatch();
			}
		});
		btnPause.setIcon(IconManager.getIcon("simPausedIcon"));
		toolBar.add(btnPause);

		toolBar.addSeparator();

		btnMoveLast = new JButton();
		btnMoveLast.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				batchTab.moveLast();
			}
		});
		btnMoveLast.setIcon(IconManager.getIcon("moveToBack"));
		toolBar.add(btnMoveLast);

		btnMoveBackward = new JButton();
		btnMoveBackward.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				batchTab.moveBackward();
			}
		});
		btnMoveBackward.setIcon(IconManager.getIcon("moveBackward"));
		toolBar.add(btnMoveBackward);

		btnMoveForward = new JButton();
		btnMoveForward.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				batchTab.moveForward();
			}
		});
		btnMoveForward.setIcon(IconManager.getIcon("moveForward"));
		toolBar.add(btnMoveForward);

		btnMoveFirst = new JButton();
		btnMoveFirst.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				batchTab.moveFirst();
			}
		});
		btnMoveFirst.setIcon(IconManager.getIcon("moveToFront"));
		toolBar.add(btnMoveFirst);

		toolBar.addSeparator();

		btnStandardpriority = new JButton();
		btnStandardpriority.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				batchTab.setBatchStandardPri();
			}
		});
		btnStandardpriority.setIcon(IconManager.getIcon("standardPriority"));
		toolBar.add(btnStandardpriority);

		btnHighpriority = new JButton();
		btnHighpriority.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				batchTab.setBatchHighPri();
			}
		});
		btnHighpriority.setIcon(IconManager.getIcon("highPriority"));
		toolBar.add(btnHighpriority);

		toolBar.addSeparator();

		if(buttonText)
		{
			btnAdd.setText("Add");
			btnRemove.setText("Remove");
			btnStart.setText("Start");
			btnPause.setText("Pause");
			btnMoveForward.setText("Forward");
			btnMoveBackward.setText("Backward");
			btnMoveFirst.setText("First");
			btnMoveLast.setText("Last");

			btnHighpriority.setText("High");
			btnStandardpriority.setText("Standard");
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
					// Do EXIT
					System.exit(0);
				}
			}
		});

	}

	private class OpenBatchFileTask extends SwingWorker<Void, Void>
	{
		private JComputeProgressMonitor openBatchProgressMonitor;
		private File[] files;

		private float progressInc;
		private int loaded = 0;
		private int error = 0;

		public OpenBatchFileTask(JComputeProgressMonitor openBatchProgressMonitor, File[] files)
		{
			this.openBatchProgressMonitor = openBatchProgressMonitor;
			this.files = files;

			log.info("Requested that " + files.length + " Batch Files be loaded");

			progressInc = 100f / (float) files.length;
		}

		@Override
		public Void doInBackground()
		{
			int progress = 0;
			setProgress(progress);

			StringBuilder errorMessage = new StringBuilder();

			for(File file : files)
			{
				String batchFile = file.getAbsolutePath();

				log.info("Batch File : " + batchFile);

				if(!batchManager.addBatch(batchFile))
				{
					log.error("Error Creating Batch from : " + batchFile);

					if(error == 0)
					{
						errorMessage.append("Error Creating Batch(s) from - \n");

					}

					errorMessage.append(error + " " + batchFile + "\n");

					error++;
				}
				else
				{
					loaded++;
				}

				progress += progressInc;

				openBatchProgressMonitor.setProgress(Math.min(progress, 100));
			}

			if(error > 0)
			{
				JOptionPane.showMessageDialog(guiFrame, errorMessage.toString());
			}

			openBatchProgressMonitor.setProgress(100);

			return null;
		}

		public void start()
		{
			openBatchProgressMonitor.setProgress(0);
			this.execute();
		}

		@Override
		public void done()
		{
			log.info(loaded + " Batch Files were loaded");
			log.info(error + " Batch Files were NOT loaded!");
		}
	}

}
