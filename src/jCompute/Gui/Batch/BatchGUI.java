package jCompute.Gui.Batch;

import jCompute.Debug.DebugLogger;
import jCompute.Gui.Component.ProgressBarTableCellRenderer;
import jCompute.Gui.Component.TablePanel;
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
import javax.swing.JTextPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JPanel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.text.BadLocationException;

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

	// Top of container Split pane - Batches section
	private JSplitPane splitPaneBatches;
	private TablePanel batchListTable;
	private int selectedBatchRowIndex = -1;

	private JPanel batchInfoPanel;
	private TablePanel activeItemsListTable;
	private TablePanel queuedItemsListTable;
	// Bottom of container split pane
	private TablePanel activeSimulationsListTable;

	private Timer activeSimulationsListTableUpdateTimer;
	private JPanel bottomSplitContainer;
	private JTextPane consoleTextPane;

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
				batchListTable.RedrawTable(selectedBatchRowIndex);

				displayBatchInfo();
			}

		}, 0, 1500);

		batchManager.addBatchManagerListener(this);
	}

	private void appendToConsole(final String text)
	{
		final int maxConsoleLength = 10000;

		Calendar calender = Calendar.getInstance();

		final String date = new SimpleDateFormat("yyyy-MMMM-dd").format(calender.getTime());
		final String time = new SimpleDateFormat("HH:mm").format(calender.getTime());

		final String output;

		if (text.length() > 2)
		{
			output = "[" + date + "@" + time + "] : " + text;
		}
		else
		{
			output = text;
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					if (consoleTextPane.getDocument().getLength() > maxConsoleLength)
					{
						consoleTextPane.getDocument().remove(consoleTextPane.getDocument().getDefaultRootElement().getElement(0).getStartOffset(), consoleTextPane.getDocument().getDefaultRootElement().getElement(0).getEndOffset());
					}
					consoleTextPane.getDocument().insertString(consoleTextPane.getDocument().getLength(), output, null);
				}
				catch (BadLocationException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}

	private void setUpFrame()
	{
		lookandFeel();

		/* Frame */
		guiFrame = new JFrame();
		guiFrame.setMinimumSize(new Dimension(800, 600));
		guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		guiFrame.getContentPane().setLayout(new BorderLayout(0, 0));

		setUpBatchesPane();

		splitPaneOuterNSSplit = new JSplitPane();
		splitPaneOuterNSSplit.setEnabled(false);
		splitPaneOuterNSSplit.setContinuousLayout(true);
		splitPaneOuterNSSplit.setResizeWeight(0.5);
		splitPaneOuterNSSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		guiFrame.getContentPane().add(splitPaneOuterNSSplit, BorderLayout.CENTER);

		splitPaneOuterNSSplit.setLeftComponent(splitPaneBatches);

		bottomSplitContainer = new JPanel();
		GridBagLayout gbl_bottomSplitContainer = new GridBagLayout();
		gbl_bottomSplitContainer.columnWidths = new int[]
		{
				0, 0
		};
		gbl_bottomSplitContainer.rowHeights = new int[]
		{
				0, 0
		};
		gbl_bottomSplitContainer.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_bottomSplitContainer.rowWeights = new double[]
		{
				1.0, 1.0
		};
		bottomSplitContainer.setLayout(gbl_bottomSplitContainer);

		OutputStream out = new OutputStream()
		{
			@Override
			public void write(int integer) throws IOException
			{
				appendToConsole(String.valueOf((char) integer));
			}

			@Override
			public void write(final byte[] bytes, final int off, final int len) throws IOException
			{
				appendToConsole(new String(bytes, off, len));
			}

			@Override
			public void write(final byte[] bytes) throws IOException
			{
				appendToConsole(new String(bytes, 0, bytes.length));
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));

		splitPaneOuterNSSplit.setRightComponent(bottomSplitContainer);

		activeSimulationsListTable = new TablePanel("Active Simulations", new String[]
		{
				"Sim Id", "Status", "Step No", "Progress", "Avg Sps", "Run Time"
		});
		activeSimulationsListTable.setColumWidth(0, 65);
		activeSimulationsListTable.setColumWidth(1, 50);
		activeSimulationsListTable.setColumWidth(2, 50);
		activeSimulationsListTable.setColumWidth(3, 65);
		activeSimulationsListTable.setColumWidth(4, 50);
		// Progress Column uses a progress bar for display
		activeSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 3);

		GridBagConstraints gbc_activeSimulationsListTable = new GridBagConstraints();
		gbc_activeSimulationsListTable.insets = new Insets(0, 0, 0, 0);
		gbc_activeSimulationsListTable.fill = GridBagConstraints.BOTH;
		gbc_activeSimulationsListTable.gridx = 0;
		gbc_activeSimulationsListTable.gridy = 0;
		bottomSplitContainer.add(activeSimulationsListTable, gbc_activeSimulationsListTable);
		JScrollPane consoleTextAreaScrollPane = new JScrollPane();
		GridBagConstraints gbc_consoleTextAreaScrollPane = new GridBagConstraints();
		gbc_consoleTextAreaScrollPane.insets = new Insets(0, 0, 0, 0);
		gbc_consoleTextAreaScrollPane.fill = GridBagConstraints.BOTH;
		gbc_consoleTextAreaScrollPane.gridx = 0;
		gbc_consoleTextAreaScrollPane.gridy = 1;

		consoleTextPane = new JTextPane();

		consoleTextAreaScrollPane.setViewportView(consoleTextPane);
		// consoleTextAreaScrollPane.setAutoscrolls(false);
		consoleTextPane.setEditable(false);
		bottomSplitContainer.add(consoleTextAreaScrollPane, gbc_consoleTextAreaScrollPane);

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
		/* Left */
		splitPaneBatches = new JSplitPane();
		splitPaneBatches.setResizeWeight(0.5);
		splitPaneBatches.setEnabled(false);
		splitPaneBatches.setContinuousLayout(true);
		batchListTable = new TablePanel("Batches", new String[]
		{
				"Batch Id", "Base File", "Type", "Items", "Progress", "Completed", "Run Time", "ETT"
		});
		// Progress Column uses a progress bar for display
		batchListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 4);
		splitPaneBatches.setLeftComponent(batchListTable);

		/* Right */
		batchInfoPanel = new JPanel();
		GridBagLayout gbl_batchInfoPanel = new GridBagLayout();
		gbl_batchInfoPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_batchInfoPanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_batchInfoPanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_batchInfoPanel.rowWeights = new double[]
		{
				1.0, 1.0
		};
		batchInfoPanel.setLayout(gbl_batchInfoPanel);

		/* Top */
		GridBagConstraints gbc_queuedItemsListTable = new GridBagConstraints();
		gbc_queuedItemsListTable.fill = GridBagConstraints.BOTH;
		gbc_queuedItemsListTable.gridx = 0;
		gbc_queuedItemsListTable.gridy = 0;

		queuedItemsListTable = new TablePanel("Queued Items", new String[]
		{
				"Item Id", "Batch Id", "Name", "Hash"
		});
		queuedItemsListTable.setColumWidth(0, 10);
		queuedItemsListTable.setColumWidth(1, 10);
		queuedItemsListTable.setColumWidth(2, 120);
		queuedItemsListTable.setColumWidth(3, 120);

		/* Bottom */
		GridBagConstraints gbc_activeSimulationListTable = new GridBagConstraints();
		gbc_activeSimulationListTable.fill = GridBagConstraints.BOTH;
		gbc_activeSimulationListTable.gridx = 0;
		gbc_activeSimulationListTable.gridy = 1;
		activeItemsListTable = new TablePanel("Active Items", new String[]
		{
				"Item Id", "Batch Id", "Name", "Hash"
		});
		activeItemsListTable.setColumWidth(0, 10);
		activeItemsListTable.setColumWidth(1, 10);
		activeItemsListTable.setColumWidth(2, 120);
		activeItemsListTable.setColumWidth(3, 120);

		batchInfoPanel.add(queuedItemsListTable, gbc_queuedItemsListTable);
		batchInfoPanel.add(activeItemsListTable, gbc_activeSimulationListTable);
		splitPaneBatches.setRightComponent(batchInfoPanel);

		registerTableMouseListeners();
	}

	private void registerTableMouseListeners()
	{
		batchListTable.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (e.getButton() == 1)
				{
					JTable table = (JTable) e.getSource();
					Point p = e.getPoint();

					selectedBatchRowIndex = table.rowAtPoint(p);

					if (e.getClickCount() == 2)
					{
						displayBatchInfo();

						// tabManager.displayTab(Integer.parseInt(simId));

					}
				}
				else
				{
					selectedBatchRowIndex = -1;

					batchListTable.clearSelection();

					activeItemsListTable.clearTable();
					queuedItemsListTable.clearTable();

					activeItemsListTable.RedrawTable(-1);
					queuedItemsListTable.RedrawTable(-1);
				}
			}
		});
	}

	private void displayBatchInfo()
	{
		activeItemsListTable.clearTable();
		queuedItemsListTable.clearTable();

		if (!(selectedBatchRowIndex >= 0 && batchListTable.getRowsCount() > 0))
		{
			return;
		}

		int batchId = Integer.parseInt(((String) batchListTable.getValueAt(selectedBatchRowIndex, 0)).replace("Batch ", ""));

		BatchItem queued[] = batchManager.getBatchQueue(batchId);
		BatchItem active[] = batchManager.getActiveQueue(batchId);

		for (int q = 0; q < queued.length; q++)
		{
			// queued
			queuedItemsListTable.addRow(String.valueOf(queued[q].getItemId()), new String[]
			{
					String.valueOf(queued[q].getBatchId()), queued[q].getItemName(), queued[q].getItemHash()
			});
		}

		for (int a = 0; a < active.length; a++)
		{
			// active
			activeItemsListTable.addRow(String.valueOf(active[a].getItemId()), new String[]
			{
					String.valueOf(active[a].getBatchId()), active[a].getItemName(), active[a].getItemHash()
			});
		}

		activeItemsListTable.RedrawTable(-1);
		queuedItemsListTable.RedrawTable(-1);

	}

	@Override
	public void windowActivated(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void itemStateChanged(ItemEvent arg0)
	{
		// TODO Auto-generated method stub

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
	public void batchAdded(int batchId, String baseFile, String scenarioType, int batchItems, int progress, int completedItems)
	{
		// add new row
		batchListTable.addRow("Batch " + batchId, new String[]
		{
				baseFile, scenarioType, Integer.toString(batchItems), Integer.toString(progress), Integer.toString(completedItems), "0", "0"
		});

	}

	@Override
	public void batchRemoved(int batchId)
	{
		DebugLogger.output("Batch Removed " + batchId);

		// remove row
		batchListTable.removeRow("Batch " + batchId);

		// If we have the first row selected, reselect it if there are more rows
		if (selectedBatchRowIndex == 0 && batchListTable.getRowsCount() > 1)
		{
			selectedBatchRowIndex = 0;
		}
		else
		{
			// Decrement the selected row count.
			selectedBatchRowIndex--;
		}

	}

	@Override
	public void batchProgress(int batchId, int progress, int completedItems, long runTime, long ett)
	{
		batchListTable.updateCell("Batch " + batchId, 4, Integer.toString(progress));
		batchListTable.updateCell("Batch " + batchId, 5, Integer.toString(completedItems));
		batchListTable.updateCell("Batch " + batchId, 6, jCompute.util.Text.longTimeToDHMS(runTime));
		batchListTable.updateCell("Batch " + batchId, 7, jCompute.util.Text.longTimeToDHMS(ett));
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