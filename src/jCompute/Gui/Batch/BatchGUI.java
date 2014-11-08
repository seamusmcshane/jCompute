package jCompute.Gui.Batch;

import jCompute.IconManager;
import jCompute.JComputeEventBus;
import jCompute.Batch.Batch;
import jCompute.Batch.Batch.BatchPriority;
import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Batch.BatchManager.BatchManagerEventListenerInf;
import jCompute.Gui.Batch.TableRowItems.ActiveSimulationRowItem;
import jCompute.Gui.Batch.TableRowItems.BatchCompletedRowItem;
import jCompute.Gui.Batch.TableRowItems.NodeInfoRowItem;
import jCompute.Gui.Batch.TableRowItems.SimpleInfoRowItem;
import jCompute.Gui.Batch.TableRowItems.BatchQueueRowItem;
import jCompute.Gui.Component.SimpleTabPanel;
import jCompute.Gui.Component.TablePanel;
import jCompute.Gui.Component.XMLPreviewPanel;
import jCompute.Gui.Component.TableCell.BooleanIconRenderer;
import jCompute.Gui.Component.TableCell.EmptyCellColorRenderer;
import jCompute.Gui.Component.TableCell.HeaderRowRenderer;
import jCompute.Gui.Component.TableCell.PriorityIconRenderer;
import jCompute.Gui.Component.TableCell.ProgressBarTableCellRenderer;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Simulation.SimulationManager.Network.Node.NodeConfiguration;
import jCompute.util.FileUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
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
import javax.swing.JSplitPane;
import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JToolBar;
import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchGUI
		implements
			ActionListener,
			ItemListener,
			WindowListener,
			PropertyChangeListener,
			BatchManagerEventListenerInf
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(BatchGUI.class);

	// Batch Manager
	private BatchManager batchManager;

	// Main Frame
	private JFrame guiFrame;
	private JMenuItem mntmQuit;

	// Container Split Pane
	private JSplitPane splitPaneOuterNSSplit;

	// Top of container Split pane - Batches section (Left/Right)
	private JSplitPane splitPaneBatchInfo;

	// Left Split
	private JPanel batchQueuedAndCompletePanel;
	private TablePanel batchQueuedTable;
	private TablePanel batchCompletedTable;

	// BatchInfo selection
	private int queuedSelectedBatchRowIndex = -1;
	private int completedSelectedBatchRowIndex = -1;
	// 0 = none,1 queued,2 completed
	private int queuedOrCompleted = 0;

	// Right Split
	private TablePanel batchInfo;

	// Bottom of container split pane
	private TablePanel activeSimulationsListTable;
	private JSplitPane bottomSplitContainerSplit;
	// Left Split
	private JPanel clusterActivityPanel;
	// Right Split
	private SimpleTabPanel batchStatusTabPanel;
	private TablePanel clusterStatusTablePanel;
	private TablePanel clusterNodesTablePanel;
	private int nodeCount = 0;

	private Timer activeSimulationsListTableUpdateTimer;

	private ProgressMonitor openBatchProgressMonitor;
	private OpenBatchFileTask openBatchProgressMonitorTask;
	private JToolBar toolBar;
	private JButton btnStart;
	private JButton btnPause;
	private JButton btnMoveForward;
	private JButton btnMoveFirst;
	private JButton btnMoveBackward;
	private JButton btnMoveLast;
	private JButton btnHighpriority;
	private JButton btnStandardpriority;

	// Queue Table Positions
	private int positionColumn = 0;
	private int idColumn = 1;
	private int nameColumn = 2;
	private int priorityColumn = 3;
	private int enabledColumn = 4;
	private int progressColumn = 5;
	private int estimatedTimeColumn = 6;
	private int batchQueueIndexColumn = idColumn;
	private JButton btnAdd;
	private JButton btnRemove;
	private boolean buttonText = true;

	public BatchGUI(boolean buttonText)
	{
		log.info("Started BatchGUI");
		batchManager = new BatchManager();

		this.buttonText = buttonText;

		setUpFrame();

		// A slow timer to update GUI
		activeSimulationsListTableUpdateTimer = new Timer("Simulation List Stat Update Timer");
		activeSimulationsListTableUpdateTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				updateBatchInfo(queuedOrCompleted);

				updateClusterInfo();

				updateNodeInfo();
			}

		}, 0, 2000);

		batchManager.addBatchManagerListener(this);

		// Register on the event bus
		JComputeEventBus.register(this);
		log.info("BatchGUI registered on event bus");
	}

	private void setUpFrame()
	{
		/* Frame */
		guiFrame = new JFrame("Batch Interface");
		guiFrame.setMinimumSize(new Dimension(900, 700));
		guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		guiFrame.getContentPane().setLayout(new BorderLayout(0, 0));

		setUpBatchesPane();

		splitPaneOuterNSSplit = new JSplitPane();
		splitPaneOuterNSSplit.setOneTouchExpandable(true);
		splitPaneOuterNSSplit.setContinuousLayout(true);
		splitPaneOuterNSSplit.setResizeWeight(0.5);
		splitPaneOuterNSSplit.setDividerSize(10);
		splitPaneOuterNSSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		guiFrame.getContentPane().add(splitPaneOuterNSSplit, BorderLayout.CENTER);

		splitPaneOuterNSSplit.setLeftComponent(splitPaneBatchInfo);
		splitPaneBatchInfo.setLeftComponent(batchQueuedAndCompletePanel);

		bottomSplitContainerSplit = new JSplitPane();
		bottomSplitContainerSplit.setOneTouchExpandable(false);
		bottomSplitContainerSplit.setContinuousLayout(true);
		bottomSplitContainerSplit.setResizeWeight(0.5);
		bottomSplitContainerSplit.setDividerSize(0);

		clusterActivityPanel = new JPanel();
		clusterActivityPanel.setMinimumSize(new Dimension(300, 200));

		batchStatusTabPanel = new SimpleTabPanel();
		batchStatusTabPanel.setMinimumSize(new Dimension(300, 150));
		batchStatusTabPanel.setPreferredSize(new Dimension(300, 150));

		clusterStatusTablePanel = new TablePanel(SimpleInfoRowItem.class, 0, false, false);
		clusterStatusTablePanel.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());
		clusterStatusTablePanel.addColumRenderer(new HeaderRowRenderer(clusterStatusTablePanel.getJTable()), 0);

		clusterStatusTablePanel.setColumWidth(0, 125);
		clusterNodesTablePanel = new TablePanel(NodeInfoRowItem.class, 0, true, false);

		clusterNodesTablePanel.setColumWidth(0, 50);
		clusterNodesTablePanel.setColumWidth(1, 75);
		// clusterNodesTablePanel.setColumWidth(2, 60);
		clusterNodesTablePanel.setColumWidth(3, 75);
		clusterNodesTablePanel.setColumWidth(4, 75);

		GridBagLayout gbl_clusterActivityPanel = new GridBagLayout();
		gbl_clusterActivityPanel.columnWidths = new int[]
		{
			0
		};
		gbl_clusterActivityPanel.rowHeights = new int[]
		{
			0
		};
		gbl_clusterActivityPanel.columnWeights = new double[]
		{
			1.0
		};
		gbl_clusterActivityPanel.rowWeights = new double[]
		{
			1.0
		};
		clusterActivityPanel.setLayout(gbl_clusterActivityPanel);

		activeSimulationsListTable = new TablePanel(ActiveSimulationRowItem.class, 0, "Active Simulations", true, false);

		activeSimulationsListTable.setColumWidth(0, 80);

		activeSimulationsListTable.setColumWidth(1, 70);
		activeSimulationsListTable.setColumWidth(2, 80);
		// activeSimulationsListTable.setColumWidth(3, 65);
		activeSimulationsListTable.setColumWidth(4, 80);
		activeSimulationsListTable.setColumWidth(5, 110);
		// Progress Column uses a progress bar for display
		activeSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		activeSimulationsListTable.setMinimumSize(new Dimension(800, 200));

		GridBagConstraints gbc_activeSimulationsListTable = new GridBagConstraints();
		gbc_activeSimulationsListTable.fill = GridBagConstraints.BOTH;
		gbc_activeSimulationsListTable.gridx = 0;
		gbc_activeSimulationsListTable.gridy = 0;

		clusterActivityPanel.add(activeSimulationsListTable, gbc_activeSimulationsListTable);

		bottomSplitContainerSplit.setLeftComponent(clusterActivityPanel);

		batchStatusTabPanel.addTab(clusterStatusTablePanel, "Cluster Info");
		batchStatusTabPanel.addTab(clusterNodesTablePanel, "Nodes");

		bottomSplitContainerSplit.setRightComponent(batchStatusTabPanel);

		splitPaneOuterNSSplit.setRightComponent(bottomSplitContainerSplit);

		toolBar = new JToolBar();

		toolBar.setFloatable(false);

		guiFrame.getContentPane().add(toolBar, BorderLayout.NORTH);

		btnStart = new JButton();
		btnStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;

					// invalid row selected
					return;
				}

				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

				batchManager.setEnabled(batchId, true);
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

				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;

					// invalid row selected
					return;
				}

				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

				clearQueuedSelection();

				batchQueuedTable.removeRow(batchId);
				batchManager.removeBatch(batchId);

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
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					// invalid row selected
					return;
				}

				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

				batchManager.setEnabled(batchId, false);
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
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0
						|| queuedSelectedBatchRowIndex == batchQueuedTable.getRowsCount() - 1)
				{
					// invalid row selected
					return;
				}

				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

				log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
						+ " moveToEnd...");

				batchManager.moveToEnd(batchId);

				queuedSelectedBatchRowIndex = batchQueuedTable.getRowsCount() - 1;
				batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);

				log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
						+ " MOVED...");

			}
		});
		btnMoveLast.setIcon(IconManager.getIcon("moveToBack"));
		toolBar.add(btnMoveLast);

		btnMoveBackward = new JButton();
		btnMoveBackward.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0
						|| queuedSelectedBatchRowIndex == batchQueuedTable.getRowsCount() - 1)
				{
					// invalid row selected
					return;
				}

				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

				log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
						+ " moveToBack...");

				batchManager.moveBackward(batchId);

				queuedSelectedBatchRowIndex = queuedSelectedBatchRowIndex + 1;
				batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);

				log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
						+ " MOVED...");

			}
		});
		btnMoveBackward.setIcon(IconManager.getIcon("moveBackward"));
		toolBar.add(btnMoveBackward);

		btnMoveForward = new JButton();
		btnMoveForward.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					// invalid row selected
					return;
				}

				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

				log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
						+ " moveToFront...");

				batchManager.moveForward(batchId);

				queuedSelectedBatchRowIndex = queuedSelectedBatchRowIndex - 1;
				batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);
			}
		});
		btnMoveForward.setIcon(IconManager.getIcon("moveForward"));
		toolBar.add(btnMoveForward);

		btnMoveFirst = new JButton();
		btnMoveFirst.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;

					// invalid row selected
					return;
				}

				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

				log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
						+ " moveToFront...");

				batchManager.moveToFront(batchId);

				queuedSelectedBatchRowIndex = 0;
				batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);

				log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
						+ " MOVED...");

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
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;

					// invalid row selected
					return;
				}

				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

				batchManager.setPriority(batchId, BatchPriority.STANDARD);
			}
		});
		btnStandardpriority.setIcon(IconManager.getIcon("standardPriority"));
		toolBar.add(btnStandardpriority);

		btnHighpriority = new JButton();
		btnHighpriority.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;

					// invalid row selected
					return;
				}

				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

				batchManager.setPriority(batchId, BatchPriority.HIGH);
			}
		});
		btnHighpriority.setIcon(IconManager.getIcon("highPriority"));
		toolBar.add(btnHighpriority);

		toolBar.addSeparator();

		if(true)
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

		JMenuBar menuBar = new JMenuBar();
		guiFrame.setJMenuBar(menuBar);

		JMenu mnFileMenu = new JMenu("File");
		menuBar.add(mnFileMenu);

		mntmQuit = new JMenuItem("Quit");
		mnFileMenu.add(mntmQuit);
		mntmQuit.addActionListener(this);

		/* Window Closing */
		guiFrame.addWindowListener(this);

		/* Display */
		guiFrame.setVisible(true);

	}

	private void setUpBatchesPane()
	{
		// Left Tables (Queue and Completed)
		batchQueuedAndCompletePanel = new JPanel();
		batchQueuedAndCompletePanel.setMinimumSize(new Dimension(300, 200));

		// Top Queue Batches Table
		GridBagLayout gbl_batchQueuedCompletePanel = new GridBagLayout();
		gbl_batchQueuedCompletePanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_batchQueuedCompletePanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_batchQueuedCompletePanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_batchQueuedCompletePanel.rowWeights = new double[]
		{
				1.0, 1.0
		};
		batchQueuedAndCompletePanel.setLayout(gbl_batchQueuedCompletePanel);

		batchQueuedTable = new TablePanel(BatchQueueRowItem.class, batchQueueIndexColumn, "Queued", true, true);

		// Batch Priority
		batchQueuedTable.addColumRenderer(
				new PriorityIconRenderer(IconManager.getIcon("highPriorityIcon"), IconManager
						.getIcon("standardPriorityIcon")), priorityColumn);

		// Batch State
		batchQueuedTable.addColumRenderer(
				new BooleanIconRenderer(IconManager.getIcon("startSimIcon"), IconManager.getIcon("pausedSimIcon")),
				enabledColumn);

		// Progress Column uses a progress bar for display
		batchQueuedTable.addColumRenderer(new ProgressBarTableCellRenderer(), progressColumn);

		GridBagConstraints gbc_batchQueuedTable = new GridBagConstraints();
		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;

		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;

		batchQueuedTable.setColumWidth(positionColumn, 80);
		batchQueuedTable.setColumWidth(idColumn, 80);

		// batchQueuedTable.setColumWidth(nameColumn, 175);
		batchQueuedTable.setColumWidth(priorityColumn, 60);
		batchQueuedTable.setColumWidth(enabledColumn, 70);
		batchQueuedTable.setColumWidth(progressColumn, 60);
		batchQueuedTable.setColumWidth(estimatedTimeColumn, 120);
		// batchQueuedTable.setColumWidth(6, 40);
		// batchQueuedTable.setColumWidth(7, 40);
		// batchQueuedTable.setColumWidth(8, 60);

		batchQueuedAndCompletePanel.add(batchQueuedTable, gbc_batchQueuedTable);

		// Bottom Completed Batches
		batchCompletedTable = new TablePanel(BatchCompletedRowItem.class, 0, "Completed", true, true);

		GridBagConstraints gbc_batchCompleteTable = new GridBagConstraints();
		gbc_batchCompleteTable.gridx = 0;
		gbc_batchCompleteTable.gridy = 1;
		gbc_batchCompleteTable.fill = GridBagConstraints.BOTH;

		batchCompletedTable.setColumWidth(0, 80);
		batchCompletedTable.setColumWidth(2, 120);
		batchCompletedTable.setColumWidth(3, 175);
		// batchCompletedTable.setColumWidth(3, 50);

		batchQueuedAndCompletePanel.add(batchCompletedTable, gbc_batchCompleteTable);

		/* Left */
		splitPaneBatchInfo = new JSplitPane();
		splitPaneBatchInfo.setOneTouchExpandable(false);
		splitPaneBatchInfo.setContinuousLayout(true);
		splitPaneBatchInfo.setResizeWeight(0.5);
		splitPaneBatchInfo.setDividerSize(0);

		/* Right Split */
		batchInfo = new TablePanel(SimpleInfoRowItem.class, 0, "Batch Info", false, false);
		batchInfo.setColumWidth(0, 125);
		batchInfo.setMinimumSize(new Dimension(300, 150));
		batchInfo.setPreferredSize(new Dimension(300, 150));
		batchInfo.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());

		batchInfo.addColumRenderer(new HeaderRowRenderer(batchInfo.getJTable()), 0);

		splitPaneBatchInfo.setRightComponent(batchInfo);
		batchInfo.setVisible(false);
		splitPaneBatchInfo.resetToPreferredSizes();

		registerTableMouseListeners();
	}

	private void clearQueuedSelection()
	{
		queuedSelectedBatchRowIndex = -1;

		batchQueuedTable.clearSelection();
	}

	private void clearCompletedSelection()
	{
		completedSelectedBatchRowIndex = -1;

		batchCompletedTable.clearSelection();
	}

	private void registerTableMouseListeners()
	{
		batchQueuedTable.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == 1)
				{
					JTable table = (JTable) e.getSource();
					Point p = e.getPoint();

					queuedOrCompleted = 1;

					if(queuedSelectedBatchRowIndex == table.rowAtPoint(p))
					{
						queuedOrCompleted = 0;

						clearQueuedSelection();
					}
					else
					{
						queuedSelectedBatchRowIndex = table.rowAtPoint(p);
					}

					// Clear any selection in the completed table
					clearCompletedSelection();
				}

				updateBatchInfo(queuedOrCompleted);

			}
		});

		batchCompletedTable.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == 1)
				{
					JTable table = (JTable) e.getSource();
					Point p = e.getPoint();

					queuedOrCompleted = 2;

					if(completedSelectedBatchRowIndex == table.rowAtPoint(p))
					{
						queuedOrCompleted = 0;

						clearCompletedSelection();
					}
					else
					{
						completedSelectedBatchRowIndex = table.rowAtPoint(p);

					}

					// Clear any selection in the queued table
					clearQueuedSelection();
				}

				updateBatchInfo(queuedOrCompleted);

			}
		});
	}

	private void updateNodeInfo()
	{
		NodeConfiguration[] nodesInfo = batchManager.getNodesInfo();

		// In case Nodes have been added or removed - compare the previous row
		// count to the current.
		if(nodesInfo.length != nodeCount)
		{
			nodeCount = nodesInfo.length;

			clusterNodesTablePanel.clearTable();

			for(int i = 0; i < nodeCount; i++)
			{
				clusterNodesTablePanel.addRow(new NodeInfoRowItem(nodesInfo[i]));
			}
		}
		else
		{
			for(int i = 0; i < nodeCount; i++)
			{
				clusterNodesTablePanel.updateRow(nodesInfo[i].getUid(), new NodeInfoRowItem(nodesInfo[i]));
			}
		}

	}

	private void updateClusterInfo()
	{
		String clusterStatus[] = batchManager.getClusterStatus();

		// Batch Info
		int clusterStatusLength = clusterStatus.length;

		if(clusterStatusTablePanel.getRowsCount() <= 0)
		{
			for(int i = 0; i < clusterStatusLength; i += 2)
			{
				clusterStatusTablePanel.addRow(new SimpleInfoRowItem(clusterStatus[i], clusterStatus[i + 1]));
			}
		}
		else
		{
			for(int i = 0; i < clusterStatusLength; i += 2)
			{
				clusterStatusTablePanel.updateRow(clusterStatus[i], new SimpleInfoRowItem(clusterStatus[i],
						clusterStatus[i + 1]));

			}
		}

	}

	private void updateBatchInfo(int srcTable)
	{
		int batchId = 0;

		// Should the data fetch be skipped
		boolean skipData = false;

		// find the table and batch the row relates to.
		if(srcTable > 0)
		{
			if(srcTable == 1)
			{
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;
					batchQueuedTable.clearSelection();
					srcTable = 0;

					// invalid row selected
					skipData = true;
				}
				else
				{
					batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
				}
			}
			else
			{

				if(completedSelectedBatchRowIndex < 0 || batchCompletedTable.getRowsCount() == 0)
				{
					completedSelectedBatchRowIndex = 0;
					batchCompletedTable.clearSelection();
					srcTable = 0;

					// invalid row selected
					skipData = true;
				}
				else
				{
					batchId = (int) batchCompletedTable.getValueAt(completedSelectedBatchRowIndex, 0);
				}
			}
		}
		else
		{
			// No table selected
			skipData = true;
		}

		if(!skipData)
		{
			String info[] = batchManager.getBatchInfo(batchId);

			// Batch Info
			int batchInfoLength = info.length;

			if(batchInfo.getRowsCount() <= 0)
			{
				for(int i = 0; i < batchInfoLength; i += 2)
				{
					batchInfo.addRow(new SimpleInfoRowItem(info[i], info[i + 1]));
				}
			}
			else
			{
				for(int i = 0; i < batchInfoLength; i += 2)
				{
					batchInfo.updateRow(info[i], new SimpleInfoRowItem(info[i], info[i + 1]));

				}
			}

		}
		else
		{
			// Clear Batch info tables
			batchInfo.clearTable();
		}
		
		// Display the info pane based on if we have data to put in it.
		batchInfo.setVisible(!skipData);
		splitPaneBatchInfo.resetToPreferredSizes();
		
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

	/**
	 * SimulationsManagerEvent handler method
	 * 
	 * @param e
	 * @return
	 */
	@Subscribe
	public void SimulationsManagerEvent(SimulationsManagerEvent e)
	{
		SimulationsManagerEventType type = e.getEventType();
		int simId = e.getSimId();

		if(type == SimulationsManagerEventType.AddedSim)
		{
			log.debug("Add Row for " + "Simulation " + simId);

			// Add the row
			activeSimulationsListTable.addRow(new ActiveSimulationRowItem(simId));
		}
		else if(type == SimulationsManagerEventType.RemovedSim)
		{
			log.debug("Removing Row for " + "Simulation " + simId);

			// Remove the Row
			activeSimulationsListTable.removeRow(simId);
		}
		else
		{
			log.error("Unhandled SimulationManagerEvent in Batch GUI");
		}
	}

	@Subscribe
	public void SimulationStatChanged(SimulationStatChangedEvent e)
	{
		activeSimulationsListTable.updateCells(e.getSimId(), new int[]
		{
				2, 3, 4, 5
		}, new Object[]
		{
				e.getStepNo(), e.getProgress(), e.getAsps(), e.getTime()
		});
	}

	@Subscribe
	public void SimulationStateChangedEvent(SimulationStateChangedEvent e)
	{
		// Simulation State
		activeSimulationsListTable.updateCell(e.getSimId(), 1, e.getState());
	}

	@Override
	public void batchAdded(final Batch batch)
	{
		// add new row
		batchQueuedTable.addRow(new BatchQueueRowItem(batch));

	}

	@Override
	public void batchFinished(final Batch batch)
	{
		log.info("Batch Finished " + batch.getBatchId());

		// remove row
		batchQueuedTable.removeRow(batch.getBatchId());

		batchCompletedTable.addRow(new BatchCompletedRowItem(batch.getBatchId(), batch.getFileName(),
				jCompute.util.Text.longTimeToDHMS(batch.getRunTime()), batch.getFinished()));

		queuedSelectedBatchRowIndex = -1;

		if(queuedOrCompleted == 1)
		{
			queuedOrCompleted = 0;
		}

		batchQueuedTable.clearSelection();
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

	@Override
	public void batchQueuePositionChanged(final Batch batch)
	{
		batchQueuedTable.updateRow(batch.getBatchId(), new BatchQueueRowItem(batch));

		log.debug("batchQueuePositionChanged " + batch.getBatchId() + " Pos" + batch.getPosition());
	}

	@Override
	public void batchProgress(final Batch batch)
	{
		int id = batch.getBatchId();

		batchQueuedTable.updateCell(id, priorityColumn, batch.getPriority());
		batchQueuedTable.updateCell(id, enabledColumn, batch.getEnabled());
		batchQueuedTable.updateCell(id, progressColumn, batch.getProgress());
		batchQueuedTable.updateCell(id, estimatedTimeColumn, jCompute.util.Text.longTimeToDHMS(batch.getETT()));
	}

}
