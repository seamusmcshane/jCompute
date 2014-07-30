package jCompute.Gui.Batch;

import jCompute.Debug.DebugLogger;
import jCompute.Gui.Component.TablePanel;
import jCompute.Gui.Component.TableCell.EmptyCellColorRenderer;
import jCompute.Gui.Component.TableCell.HeaderRowRenderer;
import jCompute.Gui.Component.TableCell.ProgressBarTableCellRenderer;
import jCompute.Simulation.Listener.SimulationStatListenerInf;
import jCompute.Simulation.Listener.SimulationStateListenerInf;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Local.SimulationsManagerEventListenerInf;
import jCompute.Simulation.SimulationManager.Local.SimulationsManager.SimulationManagerEvent;
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

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BatchGUI implements ActionListener, ItemListener, WindowListener, PropertyChangeListener, SimulationsManagerEventListenerInf, SimulationStateListenerInf, SimulationStatListenerInf, BatchManagerEventListenerInf
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
	
	public BatchGUI(SimulationsManagerInf simsManager)
	{
		this.simsManager = simsManager;

		batchManager = new BatchManager(simsManager);

		setUpFrame();

		simsManager.addSimulationManagerListener(this);

		// A slow timer to update GUI at a rate independent of
		// SimulationStatChanged notifications.
		activeSimulationsListTableUpdateTimer = new Timer("Simulation List Stat Update Timer");
		activeSimulationsListTableUpdateTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				activeSimulationsListTable.RedrawTable(-1);
				batchQueuedTable.RedrawTable(queuedSelectedBatchRowIndex);
				batchCompletedTable.RedrawTable(completedSelectedBatchRowIndex);

				displayBatchInfo(queuedOrCompleted);
			}

		}, 0, 1000);

		batchManager.addBatchManagerListener(this);
	}

	private void setUpFrame()
	{
		lookandFeel();

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
				"Sim Id", "Status", "Step No", "Progress", "Avg Sps", "Run Time"
		}, true);
		activeSimulationsListTable.setColumWidth(0, 65);
		activeSimulationsListTable.setColumWidth(1, 50);
		activeSimulationsListTable.setColumWidth(2, 50);
		activeSimulationsListTable.setColumWidth(3, 65);
		activeSimulationsListTable.setColumWidth(4, 50);
		// Progress Column uses a progress bar for display
		activeSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		activeSimulationsListTable.setMinimumSize(new Dimension(800,200));

		GridBagConstraints gbc_activeSimulationsListTable = new GridBagConstraints();
		gbc_activeSimulationsListTable.fill = GridBagConstraints.BOTH;
		gbc_activeSimulationsListTable.gridx = 0;
		gbc_activeSimulationsListTable.gridy = 0;
		bottomSplitContainer.add(activeSimulationsListTable, gbc_activeSimulationsListTable);

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

		batchQueuedTable = new TablePanel(new String[]
		{
				"Id", "Name", "Type", "Items", " % ", "Done", "ETT"
		},true);
		// Progress Column uses a progress bar for display
		batchQueuedTable.addColumRenderer(new ProgressBarTableCellRenderer(), 4);
		GridBagConstraints gbc_batchQueuedTable = new GridBagConstraints();
		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;

		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;
		
		batchQueuedTable.setColumWidth(0, 25);
		batchQueuedTable.setColumWidth(1, 200);
		batchQueuedTable.setColumWidth(2, 50);
		batchQueuedTable.setColumWidth(3, 50);
		batchQueuedTable.setColumWidth(4, 50);
		batchQueuedTable.setColumWidth(5, 50);
		
		batchQueuedAndCompletePanel.add(batchQueuedTable, gbc_batchQueuedTable);
		
		// Bottom Completed Batches
		batchCompletedTable = new TablePanel(new String[]
		{
				"Id", "Name", "Type", "Items", "Run Time"
		}, true);
		GridBagConstraints gbc_batchCompleteTable = new GridBagConstraints();
		gbc_batchCompleteTable.gridx = 0;
		gbc_batchCompleteTable.gridy = 1;
		gbc_batchCompleteTable.fill = GridBagConstraints.BOTH;
		
		batchCompletedTable.setColumWidth(0, 25);
		batchCompletedTable.setColumWidth(1, 200);
		batchCompletedTable.setColumWidth(2, 50);
		batchCompletedTable.setColumWidth(3, 50);

		batchQueuedAndCompletePanel.add(batchCompletedTable, gbc_batchCompleteTable);
		
		/* Left */
		splitPaneBatchInfo = new JSplitPane();
		splitPaneBatchInfo.setOneTouchExpandable(true);
		splitPaneBatchInfo.setContinuousLayout(true);
		splitPaneBatchInfo.setResizeWeight(0.5);
		splitPaneBatchInfo.setDividerSize(10);

		/* Right Split */
		
		batchInfoQueueTabPanel = new ItemsTabPanel();
		
		batchInfo = new TablePanel(new String[]{"Parameter", "Value"}, false);
		batchInfo.setColumWidth(0, 100);
		
		batchInfo.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());
		
		batchInfo.addColumRenderer(new HeaderRowRenderer(batchInfo.getJTable()), 0);
		
		batchInfoQueueTabPanel.addTab(batchInfo,"Information");
		
		activeItemsListTable = new TablePanel(new String[]
		{
				"Item Id", "Batch Id", "Name", "Hash"
		}, true);
		activeItemsListTable.setColumWidth(0, 50);
		activeItemsListTable.setColumWidth(1, 50);
		//activeItemsListTable.setColumWidth(2, 200);
		activeItemsListTable.setColumWidth(3, 200);

		batchInfoQueueTabPanel.addTab(activeItemsListTable,"Active");
		
		queuedItemsListTable = new TablePanel(new String[]
		{
				"Item Id", "Batch Id", "Name", "Hash"
		}, true);
		queuedItemsListTable.setColumWidth(0, 50);
		queuedItemsListTable.setColumWidth(1, 50);
		queuedItemsListTable.setColumWidth(3, 200);
		
		batchInfoQueueTabPanel.addTab(queuedItemsListTable,"Queued");

		completedItemsListTable = new TablePanel(new String[]
		{
				"Item Id", "Batch Id", "Name", "Hash"
		}, true);
		completedItemsListTable.setColumWidth(0, 50);
		completedItemsListTable.setColumWidth(1, 50);
		completedItemsListTable.setColumWidth(3, 200);
		
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
		// Clear Batch info tables
		batchInfo.clearTable();
		activeItemsListTable.clearTable();
		queuedItemsListTable.clearTable();
		completedItemsListTable.clearTable();
		
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
			// Load the batch info
			BatchItem queued[] = batchManager.getItemQueue(batchId);
			BatchItem active[] = batchManager.getActiveItems(batchId);
			BatchItem completed[] = batchManager.getCompletedItems(batchId);
			// TODO getCompletedItems BatchItem completed[] = batchManager.getCompletedItems(batchId);
			
			// Batch Info
			ArrayList<String> info = batchManager.getBatchInfo(batchId);
			int batchInfoSize = info.size();
			
			for(int i = 0; i < batchInfoSize;i+=2) 
			{	
				batchInfo.addRow(info.get(i),new String[]{info.get(i+1)});				
			}
			
			// Queued Items
			for (int q = 0; q < queued.length; q++)
			{
				// queued
				queuedItemsListTable.addRow(String.valueOf(queued[q].getItemId()), new String[]
				{
						String.valueOf(queued[q].getBatchId()), queued[q].getItemName(), queued[q].getItemHash()
				});
			}
			
			// Active Items
			for (int a = 0; a < active.length; a++)
			{
				// active
				activeItemsListTable.addRow(String.valueOf(active[a].getItemId()), new String[]
				{
						String.valueOf(active[a].getBatchId()), active[a].getItemName(), active[a].getItemHash()
				});
			}
			
			// Completed Items
			for (int a = 0; a < completed.length; a++)
			{
				// active
				completedItemsListTable.addRow(String.valueOf(completed[a].getItemId()), new String[]
				{
						String.valueOf(completed[a].getBatchId()), completed[a].getItemName(), completed[a].getItemHash()
				});
			}
		}
		
		// Always redraw item tables as one of the batch tables may have been deselected then item tables need to have previous data cleared
		redrawItemTables();
	}
	
	private void redrawItemTables()
	{
		batchInfo.RedrawTable(-1);
		
		queuedItemsListTable.RedrawTable(-1);

		activeItemsListTable.RedrawTable(-1);

		completedItemsListTable.RedrawTable(-1);
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

	/* Use the java provided system look and feel */
	private void lookandFeel()
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

	@Override
	public void SimulationsManagerEvent(final int simId, SimulationManagerEvent event)
	{
		// Getting access to simulationListTabPanel via this is not possible in
		// the runnable
		final BatchGUI batchGUI = this;

		if (event == SimulationManagerEvent.AddedSim)
		{
			javax.swing.SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					// Add the row
					activeSimulationsListTable.addRow("Simulation " + simId, new String[]
					{
							"New", "0", "0", "0", "0"
					});

					// RegiserStateListener
					simsManager.addSimulationStateListener(simId, batchGUI);

					// RegisterStatsListerner
					simsManager.addSimulationStatListener(simId, batchGUI);
				}
			});

		}
		else if (event == SimulationManagerEvent.RemovedSim)
		{
			javax.swing.SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					// UnRegisterStatsListerner
					simsManager.removeSimulationStatListener(simId, batchGUI);

					// UnRegisterStateListener
					simsManager.removeSimulationStateListener(simId, batchGUI);

					// Remove the Row
					activeSimulationsListTable.removeRow("Simulation " + simId);
				}
			});
		}
		else
		{
			DebugLogger.output("Unhandled SimulationManagerEvent in Batch GUI");
		}
	}

	@Override
	public void simulationStatChanged(int simId, long time, int stepNo, int progress, int asps)
	{
		activeSimulationsListTable.updateCells("Simulation " + simId, new int[]
		{
				2, 3, 4, 5
		}, new String[]
		{
				Integer.toString(stepNo), Integer.toString(progress), Integer.toString(asps), jCompute.util.Text.longTimeToDHMS(time)
		});
	}

	@Override
	public void simulationStateChanged(int simId, SimState state)
	{
		// Simulation State
		activeSimulationsListTable.updateCell("Simulation " + simId, 1, state.toString());
	}

	@Override
	public void batchAdded(final Batch batch)
	{
		// add new row
		batchQueuedTable.addRow(String.valueOf(batch.getBatchId()), new String[]
		{
			batch.getFileName(), batch.getType(), Integer.toString(batch.getBatchItems()), Integer.toString(batch.getProgress()), Integer.toString(batch.getCompleted()), "0"
		});

	}

	@Override
	public void batchFinished(final Batch batch)
	{
		DebugLogger.output("Batch Finished " + batch.getBatchId());

		// remove row
		batchQueuedTable.removeRow(String.valueOf(batch.getBatchId()));

		batchCompletedTable.addRow(String.valueOf(batch.getBatchId()), new String[]
		{
			batch.getFileName(), batch.getType(), Integer.toString(batch.getBatchItems()), jCompute.util.Text.longTimeToDHMS(batch.getRunTime()),
		});
		
		// If we have the first row selected, reselect it if there are more rows
		if (queuedSelectedBatchRowIndex == 0 && batchQueuedTable.getRowsCount() > 1)
		{
			queuedSelectedBatchRowIndex = 0;
		}
		else
		{
			// Decrement the selected row count.
			queuedSelectedBatchRowIndex--;			
		}

	}

	@Override
	public void batchProgress(final Batch batch)
	{
		String id  = String.valueOf(batch.getBatchId());
		batchQueuedTable.updateCell(id, 4, Integer.toString(batch.getProgress()));
		batchQueuedTable.updateCell(id, 5, Integer.toString(batch.getCompleted()));
		batchQueuedTable.updateCell(id, 6, jCompute.util.Text.longTimeToDHMS(batch.getETT()));
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
					// System.out.println("Batch File loading canceled at " + progress + "%");
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
			
			System.out.println("Requested that " + files.length + " Batch Files be loaded");

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
			System.out.println(files.length + " Batch Files were loaded");
		}
	}

}
