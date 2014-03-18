package alifeSim.Gui.Batch;

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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import alifeSim.Simulation.SimulationStatListenerInf;
import alifeSim.Simulation.SimulationStateListenerInf;
import alifeSim.Simulation.SimulationsManager;
import alifeSim.Simulation.SimulationsManagerEventListenerInf;
import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Simulation.SimulationsManager.SimulationManagerEvent;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import alifeSim.Batch.BatchItem;
import alifeSim.Batch.BatchManager;
import alifeSim.Batch.BatchManagerEventListenerInf;
import alifeSim.Debug.DebugLogger;
import alifeSim.Gui.Component.ProgressBarTableCellRenderer;
import alifeSim.Gui.Component.TablePanel;

import javax.swing.JPanel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Point;

public class BatchGUI implements ActionListener, ItemListener, WindowListener, SimulationsManagerEventListenerInf,SimulationStateListenerInf,SimulationStatListenerInf,BatchManagerEventListenerInf
{
	// Batch Manager
	private BatchManager batchManager;
	
	// Simulations Manager
	private SimulationsManager simsManager;
	
	// Main Frame
	private JFrame guiFrame;
	
	// Open Batch
	private JMenuItem mntmOpenBatch;
	
	// Container Split Pane
	private JSplitPane splitPaneOuterNSSplit;
	
	// Top of container Split pane - Batches section
	private JSplitPane splitPaneBatches;
	private TablePanel batchListTable;
	private int selectedBatchRowIndex=-1;

	private JPanel batchInfoPanel;
	private TablePanel activeItemsListTable;
	private TablePanel queuedItemsListTable;
	// Bottom of container split pane
	private TablePanel activeSimulationsListTable;
	
	private Timer activeSimulationsListTableUpdateTimer;
	
	public BatchGUI(SimulationsManager simsManager)
	{
		this.simsManager = simsManager;
		
		batchManager = new BatchManager(simsManager);
		
		setUpFrame();
		
		simsManager.addSimulationManagerListener(this);
		
		// A slow timer to update GUI at a rate independent of SimulationStatChanged notifications.
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
			  
		},0,1500);
		
		batchManager.addBatchManagerListener(this);
	}
	
	private void setUpFrame()
	{
		lookandFeel();
		
		/* Frame */
		guiFrame = new JFrame();
		guiFrame.setMinimumSize(new Dimension(800,600));
		guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		guiFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		setUpBatchesPane();
		
		activeSimulationsListTable = new TablePanel("Active Simulations",new String[]{"Sim Id","Status","Step No","Progress","Avg Sps","Run Time"});
		activeSimulationsListTable.setColumWidth(0,65);
		activeSimulationsListTable.setColumWidth(1,50);
		activeSimulationsListTable.setColumWidth(2,50);
		activeSimulationsListTable.setColumWidth(3,65);
		activeSimulationsListTable.setColumWidth(4,50);
		// Progress Column uses a progress bar for display
		activeSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		
		splitPaneOuterNSSplit = new JSplitPane();
		splitPaneOuterNSSplit.setEnabled(false);
		splitPaneOuterNSSplit.setContinuousLayout(true);
		splitPaneOuterNSSplit.setResizeWeight(0.85);
		splitPaneOuterNSSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		guiFrame.getContentPane().add(splitPaneOuterNSSplit,BorderLayout.CENTER);
		
		splitPaneOuterNSSplit.setLeftComponent(splitPaneBatches);
		splitPaneOuterNSSplit.setRightComponent(activeSimulationsListTable);
		
		JMenuBar menuBar = new JMenuBar();
		guiFrame.setJMenuBar(menuBar);
		
		JMenu mnFileMenu = new JMenu("File");
		menuBar.add(mnFileMenu);
		
		mntmOpenBatch = new JMenuItem("Open Batch");
		mntmOpenBatch.addActionListener(this);
		mnFileMenu.add(mntmOpenBatch);
		
		JMenuItem mntmQuit = new JMenuItem("Quit");
		mnFileMenu.add(mntmQuit);

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
		batchListTable = new TablePanel("Batches", new String[]{"Batch Id","Base File","Type","Items","Progress","Completed"});
		// Progress Column uses a progress bar for display
		batchListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 4);
		splitPaneBatches.setLeftComponent(batchListTable);

		/* Right */
		batchInfoPanel = new JPanel();
		GridBagLayout gbl_batchInfoPanel = new GridBagLayout();
		gbl_batchInfoPanel.columnWidths = new int[] {0, 0};
		gbl_batchInfoPanel.rowHeights = new int[] {0, 0};
		gbl_batchInfoPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_batchInfoPanel.rowWeights = new double[]{1.0, 1.0};
		batchInfoPanel.setLayout(gbl_batchInfoPanel);
		
		/* Top */
		GridBagConstraints gbc_queuedItemsListTable = new GridBagConstraints();
		gbc_queuedItemsListTable.fill = GridBagConstraints.BOTH;
		gbc_queuedItemsListTable.gridx = 0;
		gbc_queuedItemsListTable.gridy = 0;
		
		queuedItemsListTable = new TablePanel("Queued Items",new String[]{"Item Id","Batch Id","Hash"});
		queuedItemsListTable.setColumWidth(0,15);
		queuedItemsListTable.setColumWidth(1,15);
		queuedItemsListTable.setColumWidth(2,250);
		
		/* Bottom */
		GridBagConstraints gbc_activeSimulationListTable = new GridBagConstraints();
		gbc_activeSimulationListTable.fill = GridBagConstraints.BOTH;
		gbc_activeSimulationListTable.gridx = 0;
		gbc_activeSimulationListTable.gridy = 1;
		activeItemsListTable = new TablePanel("Active Items",new String[]{"Item Id","Batch Id","Hash"});
		activeItemsListTable.setColumWidth(0,15);
		activeItemsListTable.setColumWidth(1,15);
		activeItemsListTable.setColumWidth(2,250);	

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
				if(e.getButton() == 1)
				{
					JTable table =(JTable) e.getSource();
					Point p = e.getPoint();
					
					selectedBatchRowIndex = table.rowAtPoint(p);
					
					if (e.getClickCount() == 2) 
					{
						displayBatchInfo();
						
						//tabManager.displayTab(Integer.parseInt(simId));	

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
		
		if(!( selectedBatchRowIndex >= 0 && batchListTable.getRowsCount() > 0))
		{
			return;
		}
		
		int batchId = Integer.parseInt(((String) batchListTable.getValueAt(selectedBatchRowIndex, 0)).replace("Batch ", ""));		
		
		BatchItem queued[] = batchManager.getBatchQueue(batchId);
		BatchItem active[] = batchManager.getActiveQueue(batchId);
				
		for(int q=0;q<queued.length;q++)
		{
			//queued
			queuedItemsListTable.addRow(String.valueOf(queued[q].getItemId()), new String[]{ String.valueOf(queued[q].getBatchId()), queued[q].getItemHash()});
		}
		
		for(int a=0;a<active.length;a++)
		{
			//active
			activeItemsListTable.addRow(String.valueOf(active[a].getItemId()), new String[]{ String.valueOf(active[a].getBatchId()), active[a].getItemHash()});
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
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
		    public void run() 
		    {
				// Exit the sim
				doProgramExit();	
		    }
		}
		);		
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
		if(e.getSource() == mntmOpenBatch)
		{
			final JFileChooser filechooser = new JFileChooser(new File("./scenarios"));

			DebugLogger.output("Batch Open Dialog");

			int val = filechooser.showOpenDialog(filechooser);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				DebugLogger.output("New Batch Choosen");
				
				String batchFile = filechooser.getSelectedFile().getAbsolutePath();

				DebugLogger.output(batchFile);
				
				if(!batchManager.addBatch(batchFile))
				{
					DebugLogger.output("Error Creating Batch from : " + batchFile);
				}
				
			}
		}
		
	}
	
	/* Ensure the user wants to exit then exit the program */
	private void doProgramExit()
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
		// Getting access to simulationListTabPanel via this is not possible in the runnable
		final BatchGUI batchGUI = this;
		
		if(event == SimulationManagerEvent.AddedSim)
		{	
		    javax.swing.SwingUtilities.invokeLater(new Runnable() 
		    {
		        public void run() 
		        {
		        	// Add the row
		        	activeSimulationsListTable.addRow("Simulation " + simId, new String[] {"New", "0", "0","0","0"});
		        	
					// RegiserStateListener
		        	simsManager.addSimulationStateListener(simId, batchGUI);
					
					// RegisterStatsListerner
		        	simsManager.addSimulationStatListener(simId, batchGUI);
		        }
		    });
			
		}
		else if( event == SimulationManagerEvent.RemovedSim)
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
		activeSimulationsListTable.updateCells("Simulation " + simId, new int[]{2,3,4,5},new String[]{ Integer.toString(stepNo), Integer.toString(progress), Integer.toString(asps), longTimeToString(time) });
	}

	@Override
	public void simulationStateChanged(int simId, SimState state)
	{
		// Simulation State
		activeSimulationsListTable.updateCell("Simulation " + simId, 1 , state.toString());		
	}
	
	public String longTimeToString(long time)
	{
		time = time / 1000; // seconds
		int days = (int) (time / 86400); // to days
		int hrs = (int) (time / 3600) % 24; // to hrs
		int mins = (int) ((time / 60) % 60);	// to seconds
		int sec = (int) (time % 60);
	
		return String.format("%d:%02d:%02d:%02d", days, hrs, mins, sec);
	}
		
	@Override
	public void batchAdded(int batchId, String baseFile, String scenarioType, int batchItems, int progress, int completedItems)
	{
		// add new row
		batchListTable.addRow("Batch " + batchId, new String[] {baseFile, scenarioType, Integer.toString(batchItems),Integer.toString(progress),Integer.toString(completedItems)});

	}

	@Override
	public void batchRemoved(int batchId)
	{
		DebugLogger.output("Batch Removed " + batchId);
		
		// remove row
		batchListTable.removeRow("Batch " + batchId);
		
		
		// If we have the first row selected, reselect it if there are more rows
		if(selectedBatchRowIndex == 0 && batchListTable.getRowsCount() > 1)
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
	public void batchProgress(int batchId, int progress, int completedItems)
	{
		batchListTable.updateCell("Batch " + batchId, 4, Integer.toString(progress));
		batchListTable.updateCell("Batch " + batchId, 5, Integer.toString(completedItems));	
	}
	
}
