package alifeSim.Gui.Standard;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import javax.swing.JTable;
import java.awt.Color;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.border.TitledBorder;
import alifeSim.Gui.Component.ProgressBarTableCellRenderer;
import alifeSim.Gui.Component.TablePanel;
import alifeSim.Simulation.SimulationStatListenerInf;
import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Simulation.SimulationStateListenerInf;
import alifeSim.Simulation.SimulationsManager;
import alifeSim.Simulation.SimulationsManager.SimulationManagerEvent;
import alifeSim.Simulation.SimulationsManagerEventListenerInf;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SimulationListTabPanel extends JPanel implements SimulationsManagerEventListenerInf,SimulationStateListenerInf,SimulationStatListenerInf
{
	private static final long serialVersionUID = 76641721672552215L;
	
	// The table object
	private TablePanel table;	
	
	// Name for the Table / This Object
	private String name = "Simulations List";
	
	// References to the needed objects
	private GUITabManager tabManager;
	private SimulationsManager simsManager;
		
	// The update time used to redraw the table and graphs at a slower rate than the data rate.
	private Timer statUpdateTimer;
	
	private int selectedRowIndex = -1;
	
	public SimulationListTabPanel(GUITabManager tabManager, SimulationsManager simsManager) 
	{
		super();
		
		this.tabManager = tabManager;
		this.simsManager = simsManager;
				
		setLayout(new BorderLayout(0, 0));
		
		setUpTable();
		
		// A slow timer to update GUI at a rate independent of SimulationStatChanged notifications.
		statUpdateTimer = new Timer("Simulation List Stat Update Timer");
		statUpdateTimer.schedule(new TimerTask()
		{
			@Override
			public void run() 
			{
				table.RedrawTable(selectedRowIndex);								
			}
			  
		},0,1000);
		
	}
	
	/*
	 * SetsUp the Table with the correct 
	 */
	private void setUpTable()
	{
		table = new TablePanel("Simulation List",new String[]{"Sim Id","Status","Step No","Progress","Avg Sps","Run Time"});
			
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
	
	private void addRow(String rowKey,String columnValues[])
	{
		table.addRow(rowKey, columnValues);
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

	@Override
	public void SimulationsManagerEvent(final int simId, SimulationManagerEvent event)
	{
		// Getting access to simulationListTabPanel via this is not possible in the runnable
		final SimulationListTabPanel simulationListTabPanel = this;
		
		if(event == SimulationManagerEvent.AddedSim)
		{			
			
		    javax.swing.SwingUtilities.invokeLater(new Runnable() 
		    {
		        public void run() 
		        {	
		        	// Add the row
		        	simulationListTabPanel.addRow("Simulation " + simId, new String[] {"New", "0", "0","0","0"});
		        	
					// RegiserStateListener
					simsManager.addSimulationStateListener(simId, simulationListTabPanel);
					
					// RegisterStatsListerner
					simsManager.addSimulationStatListener(simId, simulationListTabPanel);
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
		        	simsManager.removeSimulationStatListener(simId, simulationListTabPanel);
		        	
					// UnRegisterStateListener
					simsManager.removeSimulationStateListener(simId, simulationListTabPanel);
					
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

	@Override
	public void simulationStatChanged(int simId, long time, int stepNo, int progress, int asps)
	{
		updateCells("Simulation " + simId, new int[]{2,3,4,5},new String[]{ Integer.toString(stepNo), Integer.toString(progress), Integer.toString(asps), alifeSim.util.Text.longTimeToDHMS(time) });	
	}

	@Override
	public void simulationStateChanged(int simId, SimState state)
	{
		// Simulation State
		updateCell("Simulation " + simId, 1 , state.toString());		
	}
	
}
