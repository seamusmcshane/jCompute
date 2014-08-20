package jCompute.Gui.Batch;

import jCompute.IconManager;
import jCompute.JComputeEventBus;
import jCompute.Batch.Batch;
import jCompute.Batch.Batch.BatchPriority;
import jCompute.Batch.BatchItem;
import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Batch.BatchManager.BatchManagerEventListenerInf;
import jCompute.Debug.DebugLogger;
import jCompute.Gui.Batch.TableRowItems.ActiveSimulationRowItem;
import jCompute.Gui.Batch.TableRowItems.BatchCompletedRowItem;
import jCompute.Gui.Batch.TableRowItems.BatchInfoQueueRowItem;
import jCompute.Gui.Batch.TableRowItems.BatchInfoRowItem;
import jCompute.Gui.Batch.TableRowItems.BatchQueueRowItem;
import jCompute.Gui.Component.TablePanel;
import jCompute.Gui.Component.TableCell.BooleanIconRenderer;
import jCompute.Gui.Component.TableCell.EmptyCellColorRenderer;
import jCompute.Gui.Component.TableCell.HeaderRowRenderer;
import jCompute.Gui.Component.TableCell.PriorityIconRenderer;
import jCompute.Gui.Component.TableCell.ProgressBarTableCellRenderer;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEventType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

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

public class BatchGUI implements ActionListener, ItemListener, WindowListener, PropertyChangeListener, BatchManagerEventListenerInf
{
	// Batch Manager
	private BatchManager batchManager;
	
	// Main Frame
	private JFrame guiFrame;

	// File Menu Items
	private JMenuItem mntmOpenBatch;
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
	private ItemsTabPanel batchInfoQueueTabPanel;
	private TablePanel batchInfo;
	private TablePanel activeItemsListTable;
	private TablePanel queuedItemsListTable;
	private TablePanel completedItemsListTable;
	
	// Bottom of container split pane
	private TablePanel activeSimulationsListTable;
	private JPanel bottomSplitContainer;

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
	
	public BatchGUI(SimulationsManagerInf simsManager)
	{
		batchManager = new BatchManager(simsManager);

		setUpFrame();
		
		// A slow timer to update GUI at a rate independent of
		// SimulationStatChanged notifications.
		activeSimulationsListTableUpdateTimer = new Timer("Simulation List Stat Update Timer");
		activeSimulationsListTableUpdateTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				//activeSimulationsListTable.RedrawTable(-1);
				//batchQueuedTable.RedrawTable(queuedSelectedBatchRowIndex);
				//batchCompletedTable.RedrawTable(completedSelectedBatchRowIndex);

				displayBatchInfo(queuedOrCompleted);
			}

		}, 0, 1000);

		batchManager.addBatchManagerListener(this);
		
		// Register on the event bus
		JComputeEventBus.register(this);
	}

	private void setUpFrame()
	{
		/* Frame */
		guiFrame = new JFrame("Batch Interface");
		guiFrame.setMinimumSize(new Dimension(800, 600));
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

		bottomSplitContainer = new JPanel();
		GridBagLayout gbl_bottomSplitContainer = new GridBagLayout();
		gbl_bottomSplitContainer.columnWidths = new int[] {0};
		gbl_bottomSplitContainer.rowHeights = new int[] {0};
		gbl_bottomSplitContainer.columnWeights = new double[]
		{
				1.0
		};
		gbl_bottomSplitContainer.rowWeights = new double[]
		{
				1.0
		};
		bottomSplitContainer.setLayout(gbl_bottomSplitContainer);

		splitPaneOuterNSSplit.setRightComponent(bottomSplitContainer);

		activeSimulationsListTable = new TablePanel(ActiveSimulationRowItem.class,0,"Active Simulations",true,false);
		
		activeSimulationsListTable.setColumWidth(0, 80);

		activeSimulationsListTable.setColumWidth(1, 70);
		activeSimulationsListTable.setColumWidth(2, 80);
		//activeSimulationsListTable.setColumWidth(3, 65);
		activeSimulationsListTable.setColumWidth(4, 80);
		activeSimulationsListTable.setColumWidth(5, 110);
		// Progress Column uses a progress bar for display
		activeSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		activeSimulationsListTable.setMinimumSize(new Dimension(800,200));

		GridBagConstraints gbc_activeSimulationsListTable = new GridBagConstraints();
		gbc_activeSimulationsListTable.fill = GridBagConstraints.BOTH;
		gbc_activeSimulationsListTable.gridx = 0;
		gbc_activeSimulationsListTable.gridy = 0;
		bottomSplitContainer.add(activeSimulationsListTable, gbc_activeSimulationsListTable);
		
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
				
				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,idColumn);
				
				batchManager.setEnabled(batchId,true);				
			}
		});
		btnStart.setIcon(IconManager.getIcon("simRunningIcon"));
		toolBar.add(btnStart);
		
		btnPause = new JButton();
		btnPause.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;
					
					// invalid row selected
					return;
				}
				
				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,idColumn);
				
				batchManager.setEnabled(batchId,false);
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
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;
					
					// invalid row selected
					return;
				}
				
				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,idColumn);
				
				DebugLogger.output("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " moveToEnd...");
				
				batchManager.moveToEnd(batchId);
				
				queuedSelectedBatchRowIndex = batchQueuedTable.getRowsCount()-1;
				batchQueuedTable.setSelection(queuedSelectedBatchRowIndex,0);
				
				DebugLogger.output("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " MOVED...");

			}
		});
		btnMoveLast.setIcon(IconManager.getIcon("moveToBack"));
		toolBar.add(btnMoveLast);
		
		btnMoveBackward = new JButton();
		btnMoveBackward.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;
					
					// invalid row selected
					return;
				}
				
				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,idColumn);
				
				DebugLogger.output("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " moveToBack...");
				
				batchManager.moveBackward(batchId);	
				
				queuedSelectedBatchRowIndex = queuedSelectedBatchRowIndex+1;
				batchQueuedTable.setSelection(queuedSelectedBatchRowIndex,0);
				
				DebugLogger.output("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " MOVED...");

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
					queuedSelectedBatchRowIndex = 0;
					
					// invalid row selected
					return;
				}
				
				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,idColumn);
				
				DebugLogger.output("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " moveToFront...");
				
				batchManager.moveForward(batchId);	
				
				queuedSelectedBatchRowIndex = queuedSelectedBatchRowIndex-1;
				batchQueuedTable.setSelection(queuedSelectedBatchRowIndex,0);
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
				
				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,idColumn);
				
				DebugLogger.output("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " moveToFront...");
				
				batchManager.moveToFront(batchId);
				
				queuedSelectedBatchRowIndex = 0;
				batchQueuedTable.setSelection(queuedSelectedBatchRowIndex,0);
				
				DebugLogger.output("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " MOVED...");

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
				
				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,idColumn);
				
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
				
				int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,idColumn);
				
				batchManager.setPriority(batchId, BatchPriority.HIGH);
			}
		});
		btnHighpriority.setIcon(IconManager.getIcon("highPriority"));
		toolBar.add(btnHighpriority);
		
		toolBar.addSeparator();
		
		btnStart.setText("Start");
		btnPause.setText("Pause");
		btnMoveForward.setText("Forward");
		btnMoveBackward.setText("Backward");
		btnMoveFirst.setText("First");
		btnMoveLast.setText("Last");

		btnHighpriority.setText("High Priority");
		btnStandardpriority.setText("Standard Priority");

		
		JMenuBar menuBar = new JMenuBar();
		guiFrame.setJMenuBar(menuBar);

		JMenu mnFileMenu = new JMenu("File");
		menuBar.add(mnFileMenu);

		mntmOpenBatch = new JMenuItem("Open Batch");
		mntmOpenBatch.addActionListener(this);
		mnFileMenu.add(mntmOpenBatch);

		mntmQuit = new JMenuItem("Quit");
		mnFileMenu.add(mntmQuit);
		mntmQuit.addActionListener(this);

		/* Window Closing */
		guiFrame.addWindowListener(this);

		/* Display */
		guiFrame.setVisible(true);
		
		//batchQueuedTable.addRow(new BatchQueueRowItem());
		//batchQueuedTable.addRow(new BatchQueueRowItem(1, 2, "a", BatchPriority.STANDARD, true,0, "NONE"));
		
		/*activeSimulationsListTable.addRow(new ActiveSimulationRowItem(0));
		//activeSimulationsListTable.removeRow(0);
		activeSimulationsListTable.addRow(new ActiveSimulationRowItem(1, SimState.NEW, 0, 0, 0, 0));
		activeSimulationsListTable.updateCells(1, new int[]
		{
				2, 3, 4, 5
		}, new Object[]
		{
				100, 100, 100, 100L
		});
		activeSimulationsListTable.updateCell(1,1,  SimState.FINISHED);
		//activeSimulationsListTable.removeRow(1);*/
		
	}

	private void setUpBatchesPane()
	{
		// Left Tables (Queue and Completed)
		batchQueuedAndCompletePanel = new JPanel();
		batchQueuedAndCompletePanel.setMinimumSize(new Dimension(300,200));
		
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

		batchQueuedTable = new TablePanel(BatchQueueRowItem.class,batchQueueIndexColumn,"Queued",true,true);
		
		// Batch Priority
		batchQueuedTable.addColumRenderer(new PriorityIconRenderer(IconManager.getIcon("highPriorityIcon"),IconManager.getIcon("standardPriorityIcon")), priorityColumn);

		// Batch State
		batchQueuedTable.addColumRenderer(new BooleanIconRenderer(IconManager.getIcon("startSimIcon"),IconManager.getIcon("pausedSimIcon")), enabledColumn);
		
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
		
		//batchQueuedTable.setColumWidth(nameColumn, 175);
		batchQueuedTable.setColumWidth(priorityColumn, 60);
		batchQueuedTable.setColumWidth(enabledColumn, 70);
		batchQueuedTable.setColumWidth(progressColumn, 60);
		batchQueuedTable.setColumWidth(estimatedTimeColumn, 120);
		//batchQueuedTable.setColumWidth(6, 40);
		//batchQueuedTable.setColumWidth(7, 40);
		//batchQueuedTable.setColumWidth(8, 60);
		
		batchQueuedAndCompletePanel.add(batchQueuedTable, gbc_batchQueuedTable);
		
		// Bottom Completed Batches
		batchCompletedTable = new TablePanel(BatchCompletedRowItem.class,0,"Completed",true,true);
		
		GridBagConstraints gbc_batchCompleteTable = new GridBagConstraints();
		gbc_batchCompleteTable.gridx = 0;
		gbc_batchCompleteTable.gridy = 1;
		gbc_batchCompleteTable.fill = GridBagConstraints.BOTH;
		
		batchCompletedTable.setColumWidth(0, 80);
		batchCompletedTable.setColumWidth(2, 120);
		batchCompletedTable.setColumWidth(3, 175);
		//batchCompletedTable.setColumWidth(3, 50);

		batchQueuedAndCompletePanel.add(batchCompletedTable, gbc_batchCompleteTable);
		
		/* Left */
		splitPaneBatchInfo = new JSplitPane();
		splitPaneBatchInfo.setOneTouchExpandable(false);
		splitPaneBatchInfo.setContinuousLayout(true);
		splitPaneBatchInfo.setResizeWeight(0.5);
		splitPaneBatchInfo.setDividerSize(0);

		/* Right Split */
		
		batchInfoQueueTabPanel = new ItemsTabPanel();
		batchInfoQueueTabPanel.setMinimumSize(new Dimension(300,150));
		batchInfoQueueTabPanel.setPreferredSize(new Dimension(300,150));
		
		batchInfo = new TablePanel(BatchInfoRowItem.class, 0, "Batch Info",false,false);
		batchInfo.setColumWidth(0, 125);
		
		batchInfo.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());
		
		batchInfo.addColumRenderer(new HeaderRowRenderer(batchInfo.getJTable()), 0);
		
		batchInfoQueueTabPanel.addTab(batchInfo,"Information");
		
		activeItemsListTable = new TablePanel(BatchInfoQueueRowItem.class,0,true,false);
		activeItemsListTable.setColumWidth(0, 80);
		activeItemsListTable.setColumWidth(1, 80);

		batchInfoQueueTabPanel.addTab(activeItemsListTable,"Active");
		
		queuedItemsListTable = new TablePanel(BatchInfoQueueRowItem.class,0,true,false);
		
		queuedItemsListTable.setColumWidth(0, 80);
		queuedItemsListTable.setColumWidth(1, 80);
		
		batchInfoQueueTabPanel.addTab(queuedItemsListTable,"Queued");

		completedItemsListTable = new TablePanel(BatchInfoQueueRowItem.class,0,true,false);
		
		completedItemsListTable.setColumWidth(0, 80);
		completedItemsListTable.setColumWidth(1, 80);
		
		batchInfoQueueTabPanel.addTab(completedItemsListTable,"Completed");

		splitPaneBatchInfo.setRightComponent(batchInfoQueueTabPanel);
		
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
				if (e.getButton() == 1)
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

				displayBatchInfo(queuedOrCompleted);

			}
		});
		
		
		batchCompletedTable.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (e.getButton() == 1)
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
				
				displayBatchInfo(queuedOrCompleted);

			}
		});
	}

	private void displayBatchInfo(int srcTable)
	{
		int batchId = 0;
		
		// Should the data fetch be skipped
		boolean skipData = false;
		
		// find the table and batch the row relates to.
		if(srcTable > 0)
		{
			if(srcTable==1)
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
			
			// Get the Item lists
			BatchItem queued[] = batchManager.getItemQueue(batchId);
			BatchItem active[] = batchManager.getActiveItems(batchId);
			BatchItem completed[] = batchManager.getCompletedItems(batchId);
			// TODO getCompletedItems BatchItem completed[] = batchManager.getCompletedItems(batchId);
			
			// Batch Info
			int batchInfoLength= info.length;	
			
			if(batchInfo.getRowsCount()<=0)
			{
				for(int i = 0; i < batchInfoLength;i+=2) 
				{	
					batchInfo.addRow(new BatchInfoRowItem(info[i],info[i+1]));				
				}
			}
			else
			{
				for(int i = 0; i < batchInfoLength;i+=2) 
				{	
					batchInfo.updateRow(info[i],new BatchInfoRowItem(info[i],info[i+1]));				

				}
			}

			// Active Items
			activeItemsListTable.clearTable();
			for (int a = 0; a < active.length; a++)
			{
				// active
				activeItemsListTable.addRow(new BatchInfoQueueRowItem(active[a].getItemId(),active[a].getBatchId(),active[a].getItemName()));
			}
			
			// Queued Items
			queuedItemsListTable.clearTable();
			for (int q = 0; q < queued.length; q++)
			{
				// queued
				queuedItemsListTable.addRow(new BatchInfoQueueRowItem(queued[q].getItemId(),queued[q].getBatchId(),queued[q].getItemName()));

			}

			// Completed Items
			completedItemsListTable.clearTable();
			for (int c = 0; c < completed.length; c++)
			{
				// active
				completedItemsListTable.addRow(new BatchInfoQueueRowItem(completed[c].getItemId(),completed[c].getBatchId(),completed[c].getItemName()));

			}
		}
		else
		{
			// Clear Batch info tables
			batchInfo.clearTable();
			activeItemsListTable.clearTable();
			queuedItemsListTable.clearTable();
			completedItemsListTable.clearTable();			
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
		if (e.getSource() == mntmOpenBatch)
		{
			final JFileChooser filechooser = new JFileChooser(new File("./scenarios"));

			filechooser.setMultiSelectionEnabled(true);

			DebugLogger.output("Batch Open Dialog");

			int val = filechooser.showOpenDialog(filechooser);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				DebugLogger.output("New Batch Choosen");

				File[] files = filechooser.getSelectedFiles();

				openBatchProgressMonitor = new ProgressMonitor(guiFrame, "Loading BatchFiles", "", 0, 100);

				openBatchProgressMonitor.setMillisToDecideToPopup(0);
				openBatchProgressMonitor.setMillisToPopup(0);
				openBatchProgressMonitor.setProgress(0);

				openBatchProgressMonitorTask = new OpenBatchFileTask(files);

				openBatchProgressMonitorTask.addPropertyChangeListener(this);

				openBatchProgressMonitorTask.execute();
				
			}
		}
		if (e.getSource() == mntmQuit)
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

				if (value == JOptionPane.YES_OPTION)
				{
					// Do EXIT
					System.exit(0);
				}
			}
		});

	}

	/**
	 * SimulationsManagerEvent handler method
	 * @param e
	 * @return 
	 */
	@Subscribe
	public void SimulationsManagerEvent(SimulationsManagerEvent e)
	{
		SimulationsManagerEventType type = e.getEventType();
		int simId = e.getSimId();

		DebugLogger.output("BatchGUI : SimulationsManagerEvent + " + e.getEventType().toString() + " " + "(" + simId + ")");
		
		if(type == SimulationsManagerEventType.AddedSim)
		{
			DebugLogger.output("Add Row for " + "Simulation " + simId);

			// Add the row
			activeSimulationsListTable.addRow(new ActiveSimulationRowItem(simId));

		}
		else if(type == SimulationsManagerEventType.RemovedSim)
		{
			DebugLogger.output("Removing Row for " + "Simulation " + simId);
			// Remove the Row
			activeSimulationsListTable.removeRow(simId);

		}
		else
		{
			DebugLogger.output("Unhandled SimulationManagerEvent in Batch GUI");
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
		activeSimulationsListTable.updateCell(e.getSimId(),1,  e.getState());
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
		DebugLogger.output("Batch Finished " + batch.getBatchId());

		// remove row
		batchQueuedTable.removeRow(batch.getBatchId());
		
		batchCompletedTable.addRow(new BatchCompletedRowItem(batch.getBatchId(),batch.getFileName(),jCompute.util.Text.longTimeToDHMS(batch.getRunTime()),batch.getFinished()));

		queuedSelectedBatchRowIndex = -1;
		
		if(queuedOrCompleted==1)
		{
			queuedOrCompleted=0;
		}
		
		batchQueuedTable.clearSelection();
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		if ("progress" == e.getPropertyName())
		{
			int progress = (Integer) e.getNewValue();

			if (openBatchProgressMonitor.isCanceled())
			{
				Toolkit.getDefaultToolkit().beep();
				if (openBatchProgressMonitor.isCanceled())
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
			
			DebugLogger.output("Requested that " + files.length + " Batch Files be loaded");

		}

		@Override
		public Void doInBackground()
		{
			int progress = 0;
			setProgress(0);

			StringBuilder errorMessage = new StringBuilder();
			
			// Thread.sleep(1000);

			for (File file : files)
			{
				String batchFile = file.getAbsolutePath();

				DebugLogger.output(batchFile);

				if (!batchManager.addBatch(batchFile))
				{
					DebugLogger.output("Error Creating Batch from : " + batchFile);
					
					
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
			DebugLogger.output(loaded + " Batch Files were loaded");
			DebugLogger.output(error + " Batch Files were NOT loaded!");
		}
	}

	@Override
	public void batchQueuePositionChanged(final Batch batch)
	{
		
		batchQueuedTable.updateRow(batch.getBatchId(),new BatchQueueRowItem(batch));
		
		DebugLogger.output("batchQueuePositionChanged " + batch.getBatchId() + " Pos" + batch.getPosition());

	}
	
	@Override
	public void batchProgress(final Batch batch)
	{
		int id  = batch.getBatchId();
		
		batchQueuedTable.updateCell(id, priorityColumn, batch.getPriority());
		batchQueuedTable.updateCell(id, enabledColumn, batch.getEnabled());
		batchQueuedTable.updateCell(id, progressColumn, batch.getProgress());
		batchQueuedTable.updateCell(id, estimatedTimeColumn, jCompute.util.Text.longTimeToDHMS(batch.getETT()));
	}
	
}
