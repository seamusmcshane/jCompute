package jCompute.Gui.Batch;

import jCompute.IconManager;
import jCompute.JComputeEventBus;
import jCompute.Batch.Batch;
import jCompute.Batch.Batch.BatchPriority;
import jCompute.Batch.BatchItem;
import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Batch.BatchManager.BatchManagerEventListenerInf;
import jCompute.Debug.DebugLogger;
import jCompute.Gui.Component.TablePanel;
import jCompute.Gui.Component.TableCell.BooleanIconRenderer;
import jCompute.Gui.Component.TableCell.EmptyCellColorRenderer;
import jCompute.Gui.Component.TableCell.HeaderRowRenderer;
import jCompute.Gui.Component.TableCell.PriorityIconRenderer;
import jCompute.Gui.Component.TableCell.ProgressBarTableCellRenderer;
import jCompute.Gui.Standard.Tab.SimulationListTabPanel;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Simulation.SimulationState.SimState;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class BatchGUI implements ActionListener, ItemListener, WindowListener, PropertyChangeListener, BatchManagerEventListenerInf
{
	// Batch Manager
	private BatchManager batchManager;

	// Simulations Manager
	private SimulationsManagerInf simsManager;

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
	
	public BatchGUI(SimulationsManagerInf simsManager)
	{
		this.simsManager = simsManager;

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

		activeSimulationsListTable = new TablePanel("Active Simulations", new String[]
		{
				"Id", "Status", "Step", "Progress", "Step Rate", "Run Time"
		}, true);
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
				
				int batchId = Integer.parseInt(batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,0));
				
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
				
				int batchId = Integer.parseInt(batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,0));
				
				batchManager.setEnabled(batchId,false);
			}
		});
		btnPause.setIcon(IconManager.getIcon("simPausedIcon"));
		toolBar.add(btnPause);
		
		toolBar.addSeparator();
		
		btnMoveLast = new JButton();
		btnMoveLast.setIcon(IconManager.getIcon("moveToBack"));
		toolBar.add(btnMoveLast);
		
		btnMoveBackward = new JButton();
		btnMoveBackward.setIcon(IconManager.getIcon("moveBackward"));
		toolBar.add(btnMoveBackward);
		
		btnMoveForward = new JButton();
		btnMoveForward.setIcon(IconManager.getIcon("moveForward"));
		toolBar.add(btnMoveForward);
		
		btnMoveFirst = new JButton();
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
				
				int batchId = Integer.parseInt(batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,0));
				
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
				
				int batchId = Integer.parseInt(batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex,0));
				
				batchManager.setPriority(batchId, BatchPriority.HIGH);
			}
		});
		btnHighpriority.setIcon(IconManager.getIcon("highPriority"));
		toolBar.add(btnHighpriority);
		
		toolBar.addSeparator();
		
		boolean showText = true;
		
		//if(showText)
		{
			btnStart.setText("Start");
			btnPause.setText("Pause");
			btnMoveForward.setText("Forward");
			btnMoveBackward.setText("Backward");
			btnMoveFirst.setText("First");
			btnMoveLast.setText("Last");

			btnHighpriority.setText("High Priority");
			btnStandardpriority.setText("Standard Priority");
			
		}
		
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

		batchQueuedTable = new TablePanel("Queued", new String[]
		{
				"Id", "Name", "Priority", "Enabled", " % ", "Estimated Time"
		},true);
		
		
		batchQueuedTable.addColumRenderer(new PriorityIconRenderer(IconManager.getIcon("highPriorityIcon"),IconManager.getIcon("standardPriorityIcon")), 2);

		batchQueuedTable.addColumRenderer(new BooleanIconRenderer(IconManager.getIcon("startSimIcon"),IconManager.getIcon("pausedSimIcon")), 3);
		
		
		// Progress Column uses a progress bar for display
		batchQueuedTable.addColumRenderer(new ProgressBarTableCellRenderer(), 4);
		GridBagConstraints gbc_batchQueuedTable = new GridBagConstraints();
		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;

		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;
		
		batchQueuedTable.setColumWidth(0, 80);
		//batchQueuedTable.setColumWidth(1, 175);
		batchQueuedTable.setColumWidth(2, 60);
		batchQueuedTable.setColumWidth(3, 70);
		batchQueuedTable.setColumWidth(4, 60);
		batchQueuedTable.setColumWidth(5, 120);
		//batchQueuedTable.setColumWidth(6, 40);
		//batchQueuedTable.setColumWidth(7, 40);
		//batchQueuedTable.setColumWidth(8, 60);
		
		batchQueuedAndCompletePanel.add(batchQueuedTable, gbc_batchQueuedTable);
		
		// Bottom Completed Batches
		batchCompletedTable = new TablePanel("Completed", new String[]
		{
				"Id", "Name",  "Run Time", "Finished"
		}, true);
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
		splitPaneBatchInfo.setOneTouchExpandable(true);
		splitPaneBatchInfo.setContinuousLayout(true);
		splitPaneBatchInfo.setResizeWeight(0.5);
		splitPaneBatchInfo.setDividerSize(10);

		/* Right Split */
		
		batchInfoQueueTabPanel = new ItemsTabPanel();
		batchInfoQueueTabPanel.setMinimumSize(new Dimension(300,150));
		batchInfoQueueTabPanel.setPreferredSize(new Dimension(300,150));
		
		batchInfo = new TablePanel(new String[]{"Parameter", "Value"}, false);
		batchInfo.setColumWidth(0, 125);
		
		batchInfo.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());
		
		batchInfo.addColumRenderer(new HeaderRowRenderer(batchInfo.getJTable()), 0);
		
		batchInfoQueueTabPanel.addTab(batchInfo,"Information");
		
		activeItemsListTable = new TablePanel(new String[]
		{
				"Item", "Batch", "Name"
		}, true);
		activeItemsListTable.setColumWidth(0, 80);
		activeItemsListTable.setColumWidth(1, 80);

		batchInfoQueueTabPanel.addTab(activeItemsListTable,"Active");
		
		queuedItemsListTable = new TablePanel(new String[]
		{
				"Item", "Batch", "Name"
		}, true);
		queuedItemsListTable.setColumWidth(0, 80);
		queuedItemsListTable.setColumWidth(1, 80);
		
		batchInfoQueueTabPanel.addTab(queuedItemsListTable,"Queued");

		completedItemsListTable = new TablePanel(new String[]
		{
				"Item", "Batch", "Name"
		}, true);
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
					batchId = Integer.parseInt(batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, 0));
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
					batchId = Integer.parseInt(batchCompletedTable.getValueAt(completedSelectedBatchRowIndex, 0));
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
					batchInfo.addRow(new String[]{info[i],info[i+1]});				
				}
			}
			else
			{
				for(int i = 0; i < batchInfoLength;i+=2) 
				{	
					batchInfo.updateRow(info[i], new String[]{info[i],info[i+1]});				
				}
			}

			// Active Items
			activeItemsListTable.clearTable();
			for (int a = 0; a < active.length; a++)
			{
				// active
				activeItemsListTable.addRow(new String[]
				{
						String.valueOf(active[a].getItemId()),String.valueOf(active[a].getBatchId()), active[a].getItemName()
				});
			}
			
			// Queued Items
			queuedItemsListTable.clearTable();
			for (int q = 0; q < queued.length; q++)
			{
				// queued
				queuedItemsListTable.addRow(new String[]
				{
						String.valueOf(queued[q].getItemId()),String.valueOf(queued[q].getBatchId()), queued[q].getItemName()
				});
			}

			// Completed Items
			completedItemsListTable.clearTable();
			for (int a = 0; a < completed.length; a++)
			{
				// active
				completedItemsListTable.addRow(new String[]
				{
						String.valueOf(completed[a].getItemId()),String.valueOf(completed[a].getBatchId()), completed[a].getItemName()
				});
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

		BatchGUI batchGUI = this;

		DebugLogger.output("BatchGUI : SimulationsManagerEvent + " + e.getEventType().toString() + " " + "(" + simId + ")");
		
		if(type == SimulationsManagerEventType.AddedSim)
		{
			DebugLogger.output("Add Row for " + "Simulation " + simId);

			// Add the row
			activeSimulationsListTable.addRow(new String[]
			{
					String.valueOf(simId), "New", "0", "0", "0", "0"
			});

			// RegiserStateListener
			//simsManager.addSimulationStateListener(simId, batchGUI);

			// RegisterStatsListerner
			// simsManager.addSimulationStatListener(simId, batchGUI);

		}
		else if(type == SimulationsManagerEventType.RemovedSim)
		{	
			// UnRegisterStatsListerner
			//simsManager.removeSimulationStatListener(simId, batchGUI);

			// UnRegisterStateListener
			//simsManager.removeSimulationStateListener(simId, batchGUI);

			DebugLogger.output("Removing Row for " + "Simulation " + simId);
			// Remove the Row
			activeSimulationsListTable.removeRow(String.valueOf(simId));

		}
		else
		{
			DebugLogger.output("Unhandled SimulationManagerEvent in Batch GUI");
		}
	}

	@Subscribe
	public void SimulationStatChanged(SimulationStatChangedEvent e)
	{
		activeSimulationsListTable.updateCells(String.valueOf(e.getSimId()), new int[]
		{
				2, 3, 4, 5
		}, new String[]
		{
				Integer.toString(e.getStepNo()), Integer.toString(e.getProgress()), Integer.toString(e.getAsps()), jCompute.util.Text.longTimeToDHMS(e.getTime())
		});
	}

	@Subscribe
	public void SimulationStateChangedEvent(SimulationStateChangedEvent e)
	{
		SimState state = e.getState();
		int simId = e.getSimId();
		
		// Simulation State
		activeSimulationsListTable.updateCell(String.valueOf(simId), 1, state.toString());
	}

	@Override
	public void batchAdded(final Batch batch)
	{		
		// add new row
		batchQueuedTable.addRow(new String[]
		{
				String.valueOf(batch.getBatchId()), batch.getFileName(), batch.getPriority().toString(), String.valueOf(batch.getEnabled()), Integer.toString(batch.getProgress()), "0"
		});

	}

	@Override
	public void batchFinished(final Batch batch)
	{
		DebugLogger.output("Batch Finished " + batch.getBatchId());

		// remove row
		batchQueuedTable.removeRow(String.valueOf(batch.getBatchId()));

		batchCompletedTable.addRow(new String[]
		{
				String.valueOf(batch.getBatchId()), batch.getFileName(), jCompute.util.Text.longTimeToDHMS(batch.getRunTime()),batch.getFinished()
		});
		
		queuedSelectedBatchRowIndex = -1;
		
		if(queuedOrCompleted==1)
		{
			queuedOrCompleted=0;
		}
		
		batchQueuedTable.clearSelection();
		
	}

	@Override
	public void batchProgress(final Batch batch)
	{
		String id  = String.valueOf(batch.getBatchId());
		batchQueuedTable.updateCell(id, 2, batch.getPriority().toString());
		batchQueuedTable.updateCell(id, 3, String.valueOf(batch.getEnabled()));
		batchQueuedTable.updateCell(id, 4, Integer.toString(batch.getProgress()));
		batchQueuedTable.updateCell(id, 5, jCompute.util.Text.longTimeToDHMS(batch.getETT()));
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

			// Thread.sleep(1000);

			for (File file : files)
			{
				String batchFile = file.getAbsolutePath();

				DebugLogger.output(batchFile);

				if (!batchManager.addBatch(batchFile))
				{
					DebugLogger.output("Error Creating Batch from : " + batchFile);
				}

				progress += Math.ceil(progressInc);
				setProgress(Math.min(progress, 100));
			}

			return null;
		}

		@Override
		public void done()
		{
			DebugLogger.output(files.length + " Batch Files were loaded");
		}
	}

}
