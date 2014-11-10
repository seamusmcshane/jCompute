package jCompute.Gui.Batch;

import jCompute.IconManager;
import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Gui.Component.SimpleTabPanel;
import jCompute.Gui.Component.XMLPreviewPanel;
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
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JToolBar;
import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchGUI implements ActionListener, ItemListener, WindowListener, PropertyChangeListener
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(BatchGUI.class);

	// Batch Manager
	private BatchManager batchManager;

	// Main Frame
	private JFrame guiFrame;
	private JMenuItem mntmQuit;

	// Menu Bar
	private JMenuBar menuBar;

	// Update Timer
	private Timer activeSimulationsListTableUpdateTimer;

	// Batch Add
	private ProgressMonitor openBatchProgressMonitor;
	private OpenBatchFileTask openBatchProgressMonitorTask;
	private JToolBar toolBar;

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

	// GUI Tabs
	private SimpleTabPanel guiTabs;

	// Batch Tab
	private BatchTab batchTab;

	// Cluster Tab
	private ClusterStatusTab clusterStatusTab;

	public BatchGUI(boolean buttonText)
	{
		log.info("Started BatchGUI");
		batchManager = new BatchManager();

		this.buttonText = buttonText;

		createFrame();

		// A slow timer to update GUI
		activeSimulationsListTableUpdateTimer = new Timer("Simulation List Stat Update Timer");
		activeSimulationsListTableUpdateTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				// updateBatchInfo(queuedOrCompleted);

				// updateClusterInfo();

				// updateNodeInfo();
			}

		}, 0, 2000);

		// Show Frame
		guiFrame.setVisible(true);
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

		createAndAddTabs();

		guiFrame.getContentPane().add(guiTabs, BorderLayout.CENTER);
	}

	public void createAndAddTabs()
	{
		guiTabs = new SimpleTabPanel();

		batchTab = new BatchTab(batchManager);

		guiTabs.addTab(batchTab, "Batches");

		clusterStatusTab = new ClusterStatusTab(batchManager);

		guiTabs.addTab(clusterStatusTab, "Cluster");
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

					openBatchProgressMonitor = new ProgressMonitor(guiFrame, "Loading BatchFiles", "", 0, 100);

					openBatchProgressMonitor.setMillisToDecideToPopup(0);
					openBatchProgressMonitor.setMillisToPopup(0);
					openBatchProgressMonitor.setProgress(0);

					openBatchProgressMonitorTask = new OpenBatchFileTask(files);

					openBatchProgressMonitorTask.addPropertyChangeListener(BatchGUI.this);

					openBatchProgressMonitorTask.execute();

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

	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		if("progress" == e.getPropertyName())
		{
			int progress = (Integer) e.getNewValue();

			if(openBatchProgressMonitor.isCanceled())
			{
				Toolkit.getDefaultToolkit().beep();
				if(openBatchProgressMonitor.isCanceled())
				{
					// openBatchProgressMonitorTask.cancel(true);
					System.out.println("Cannot abort Batch File loading task");
				}
			}
			else
			{
				openBatchProgressMonitor.setProgress(progress);

				String message = String.format("Completed %d%%.\n", progress);

				openBatchProgressMonitor.setNote(message);
			}
		}
	}

	private class OpenBatchFileTask extends SwingWorker<Void, Void>
	{
		private File[] files;

		private float progressInc;
		private int loaded = 0;
		private int error = 0;

		public OpenBatchFileTask(File[] files)
		{
			this.files = files;

			progressInc = 100f / files.length;

			log.info("Requested that " + files.length + " Batch Files be loaded");

		}

		@Override
		public Void doInBackground()
		{
			int progress = 0;
			setProgress(0);

			StringBuilder errorMessage = new StringBuilder();

			// Thread.sleep(1000);

			for(File file : files)
			{
				String batchFile = file.getAbsolutePath();

				log.info(batchFile);

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

				progress += Math.ceil(progressInc);
				setProgress(Math.min(progress, 100));
			}

			if(error > 0)
			{
				JOptionPane.showMessageDialog(guiFrame, errorMessage.toString());
			}

			return null;
		}

		@Override
		public void done()
		{
			log.info(loaded + " Batch Files were loaded");
			log.info(error + " Batch Files were NOT loaded!");
		}
	}

}
