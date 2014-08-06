package jCompute.Gui.Standard.Tab;

import jCompute.JComputeEventBus;
import jCompute.Gui.Component.TablePanel;
import jCompute.Gui.Component.TableCell.ProgressBarTableCellRenderer;
import jCompute.Gui.Standard.GUITabManager;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JTable;

import com.google.common.eventbus.Subscribe;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SimulationListTabPanel extends JPanel
{
	private static final long serialVersionUID = 76641721672552215L;
	
	// The table object
	private TablePanel table;	
	
	// Name for the Table / This Object
	private String name = "Simulations List";
	
	// References to the needed objects
	private GUITabManager tabManager;
	private SimulationsManagerInf simsManager;
	
	private int selectedRowIndex = -1;
	
	public SimulationListTabPanel(GUITabManager tabManager, SimulationsManagerInf simsManager) 
	{
		super();
		
		this.tabManager = tabManager;
		this.simsManager = simsManager;
				
		setLayout(new BorderLayout(0, 0));
		
		setMinimumSize(new Dimension(400,600));
		
		setUpTable();
		
		// Register on the event bus
		JComputeEventBus.register(this);

	}
	
	/*
	 * SetsUp the Table with the correct 
	 */
	private void setUpTable()
	{
		table = new TablePanel("Simulation List",new String[]{"Sim Id","Status","Step No","Progress","Avg Sps","Run Time"}, true);
			
		table.setColumWidth(0,65);
		table.setColumWidth(1,50);
		table.setColumWidth(2,50);
		table.setColumWidth(3,65);
		table.setColumWidth(4,50);
		//table.setColumWidth(5,25);		
		
		// Progress Column uses a progress bar for display
		table.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		this.add(table);

		registerTableMouseListener();

	}
	
	/*
	 * Sets up the handler for the mouse clicks on the Table
	 */
	private void registerTableMouseListener()
	{
		table.addMouseListener(new MouseAdapter() 
		{
			public void mousePressed(MouseEvent e) 
			{
				if(e.getButton() == 1)
				{
					JTable table =(JTable) e.getSource();
					Point p = e.getPoint();
					
					selectedRowIndex = table.rowAtPoint(p);
					
					if (e.getClickCount() == 2) 
					{
						// Get the String "Simulation (int)" and remove "Simulation "
						String simId = ((String) table.getValueAt(selectedRowIndex, 0)).replace("Simulation ", "");

						//System.out.println("Button " + e.getButton() + " Clicked " + row);

						tabManager.displayTab(Integer.parseInt(simId));	

					}
				}
				else
				{
					selectedRowIndex = -1;
					
					table.clearSelection();
				}
			}
		});
	}
		
	private void clearTable()
	{
		table.clearTable();
	}
	
	private void addRow(String columnValues[])
	{
		table.addRow(columnValues);
	}
	
	private void updateRow(String rowKey,String columnValues[])
	{
		table.updateRow(rowKey, columnValues);
	}
	
	private void updateCells(String rowKey,int columns[], String columnValues[])
	{
		table.updateCells(rowKey,columns,columnValues);
	}
	
	private void updateCell(String rowKey,int column, String columnValue)
	{
		table.updateCell(rowKey, column,columnValue);
	}
	
	private void removeRow(String rowKey)
	{
		table.removeRow(rowKey);
	}
	
	public String getTabName()
	{
		return name;
	}

	/**
	 * SimulationsManagerEvent handler method
	 * @param e
	 */
	@Subscribe
	public void SimulationsManagerEvent(SimulationsManagerEvent e)
	{
		SimulationsManagerEventType type = e.getEventType();
		final int simId = e.getSimId();
		
		// Getting access to simulationListTabPanel via this is not possible in the runnable
		final SimulationListTabPanel simulationListTabPanel = this;
		
		if(type == SimulationsManagerEventType.AddedSim)
		{			
			
		    javax.swing.SwingUtilities.invokeLater(new Runnable() 
		    {
		        public void run() 
		        {	
		        	// Add the row
		        	simulationListTabPanel.addRow(new String[] {"Simulation " + simId,"New", "0", "0","0","0"});
		        	
					// RegiserStateListener
					//simsManager.addSimulationStateListener(simId, simulationListTabPanel);
					
					
					// RegisterStatsListerner
					//simsManager.addSimulationStatListener(simId, simulationListTabPanel);
		        }
		    });
			
		}
		else if(type == SimulationsManagerEventType.RemovedSim)
		{					
		    javax.swing.SwingUtilities.invokeLater(new Runnable() 
		    {
		        public void run() 
		        {						
		        	// UnRegisterStatsListerner
		        	//simsManager.removeSimulationStatListener(simId, simulationListTabPanel);
		        	
					// UnRegisterStateListener
					//simsManager.removeSimulationStateListener(simId, simulationListTabPanel);
					
					// Remove the Row
					simulationListTabPanel.removeRow("Simulation " + simId);

		        }
		    });
		    
		}
		else
		{
			System.out.println("Unhandled SimulationManagerEvent in Simulations List");
		}
	}

	@Subscribe
	public void SimulationStatChanged(SimulationStatChangedEvent e)
	{
		updateCells("Simulation " + e.getSimId(), new int[]{2,3,4,5},new String[]{ Integer.toString(e.getStepNo()), Integer.toString(e.getProgress()), Integer.toString(e.getAsps()), jCompute.util.Text.longTimeToDHMS(e.getTime()) });	
	}

	@Subscribe
	public void SimulationStateChangedEvent(SimulationStateChangedEvent e)
	{		
		// Simulation State
		updateCell("Simulation " + e.getSimId(), 1 , e.getState().toString());		
	}
	
}
