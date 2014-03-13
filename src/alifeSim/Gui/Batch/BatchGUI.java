package alifeSim.Gui.Batch;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import alifeSim.Simulation.SimulationsManager;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import alifeSim.Batch.Batch;
import alifeSim.Batch.BatchManager;
import alifeSim.Gui.Component.ProgressBarTableCellRenderer;
import alifeSim.Gui.Component.TablePanel;

public class BatchGUI implements ActionListener, ItemListener, WindowListener
{
	// Main Frame
	private JFrame guiFrame;

	// Open Batch
	JMenuItem mntmOpenBatch;
	
	// Batch Manager
	private BatchManager batchManager;
	
	public BatchGUI(SimulationsManager simsManager)
	{		
		batchManager = new BatchManager(simsManager);
		
		lookandFeel();
		
		/* Frame */
		guiFrame = new JFrame();
		guiFrame.setMinimumSize(new Dimension(800,600));
		guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		guiFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setEnabled(false);
		splitPane.setContinuousLayout(true);
		guiFrame.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		TablePanel batchListTable = new TablePanel("Batches", new String[]{"Batch Id","Base File","Type","Total Simulations","Progress","Simulations Completed"});
		splitPane.setLeftComponent(batchListTable);
		
		TablePanel simulationListTable;
		
		simulationListTable = new TablePanel("Simulation List",new String[]{"Sim Id","Status","Step No","Progress","Avg Sps","Run Time"});
		
		simulationListTable.setColumWidth(0,65);
		simulationListTable.setColumWidth(1,50);
		simulationListTable.setColumWidth(2,50);
		simulationListTable.setColumWidth(3,65);
		simulationListTable.setColumWidth(4,50);
		//table.setColumWidth(5,25);		
		
		// Progress Column uses a progress bar for display
		simulationListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		
		splitPane.setRightComponent(simulationListTable);
		
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

			System.out.println("Batch Open Dialog");

			int val = filechooser.showOpenDialog(filechooser);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				System.out.println("New Batch Choosen");
				
				String batchFile = filechooser.getSelectedFile().getAbsolutePath();

				System.out.println(batchFile);
				
				try
				{
					batchManager.addBatch(new Batch(batchFile));
				}
				catch (IOException e1)
				{
					System.out.println("Error Creating Batch from : " + batchFile);
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
	
}
