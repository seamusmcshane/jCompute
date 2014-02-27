package alifeSim.Gui;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;

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
	private SimulationsManager simsManger;
		
	// The update time used to redraw the table and graphs at a slower rate than the data rate.
	private Timer statUpdateTimer;
	private int traceAdds=0;
	
	// Chart and ChartPanel Objects
	private Chart2D chart2dST;
	private ChartPanel chartPanelST;
	private Font chartFont = new Font("Sans", Font.BOLD, 12);

	// Hash Map of graph traces
	private HashMap<String,ITrace2D> traceMapST;
	
	// Trace for the run time (also ensures graph moves when nothing is present)
	private ITrace2D runTime;
	
	// Initial Color Hue offset
	private float cOffset=0.8f;
	
	// Lenght of the graph in samples
	private int stSamWin = 300;
	
	public SimulationListTabPanel(GUITabManager tabManager, SimulationsManager simsManager) 
	{
		super();
		
		this.tabManager = tabManager;
		this.simsManger = simsManager;
				
		setLayout(new BorderLayout(0, 0));
		this.setMinimumSize(new Dimension(350, 400));
		
		setUpTable();
				
		createHistoryChart2DST();
		
		// A slow timer to update GUI at a rate independent of SimulationStatChanged notifications.
		statUpdateTimer = new Timer("Simulation List Stat Update Timer");
		statUpdateTimer.schedule(new TimerTask()
		{
			@Override
			public void run() 
			{
				table.RedrawTable();
				
				updateGraph();
			}
			  
		},0,1000);
		
	}
	
	/*
	 * SetsUp the Table with the correct 
	 */
	private void setUpTable()
	{
		table = new TablePanel("Simulation List",new String[]{"Sim Id","Status","Step No","Progress","Avg Sps","Run Time"});
				
		// Progress Column uses a progress bar for display
		table.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		this.add(table);
		
		registerTableMouseListener();
		
		/* Debug
			this.addRow("TEST Sim 1", new String[] {"Running", "100", "25","1","100"});
			this.addRow("TEST Sim 2", new String[] {"Running", "100", "50","1","100"});
			this.addRow("TEST Sim 3", new String[] {"Running", "100", "75","1","100"});
			this.addRow("TEST Sim 4", new String[] {"Running", "100", "0","1","100"});
			this.addRow("TEST Sim 5", new String[] {"Running", "100", "100","1","100"});
			this.addRow("TEST Sim 6", new String[] {"Running", "100", "-1","1","100"});
		 */
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
					
					int row = table.rowAtPoint(p);
					
					if (e.getClickCount() == 2) 
					{
						// Get the String "Simulation (int)" and remove "Simulation "
						String simId = ((String) table.getValueAt(row, 0)).replace("Simulation ", "");

						//System.out.println("Button " + e.getButton() + " Clicked " + row);

						tabManager.displayTab(Integer.parseInt(simId));	

					}
				}
				else
				{
					table.clearSelection();
				}
			}
		});
	}
	
	private void createHistoryChart2DST()
	{
		traceMapST = new HashMap<String,ITrace2D>();
		chart2dST = new Chart2D();
		chart2dST.setUseAntialiasing(true);
		//chart2dST.enablePointHighlighting(false);
		//chart2dST.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
		chart2dST.getAxisY().getAxisTitle().setTitle("Step Rate");
		chart2dST.getAxisY().getAxisTitle().setTitleFont(chartFont);
		chart2dST.getAxisX().getAxisTitle().setTitle("");
		chart2dST.getAxisX().getAxisTitle().setTitleFont(chartFont);
		chart2dST.setGridColor(new Color(192,192,192));
		chart2dST.getAxisY().setPaintGrid(true);
		chart2dST.getAxisX().setPaintGrid(true);
		chart2dST.setBackground(Color.white);
		chartPanelST = new ChartPanel(chart2dST);
		
		chartPanelST.setBorder(new TitledBorder(null, "Simulation Performance Graph (30 seconds)", TitledBorder.CENTER, TitledBorder.TOP, null, null)); 	
		chartPanelST.setBackground(Color.white);
		chartPanelST.setPreferredSize(new Dimension(350,250));
		
		add(chartPanelST, BorderLayout.NORTH);
		
		runTime = new Trace2DLtd(stSamWin);
		runTime.setName("Run Time");
		
		chart2dST.addTrace(runTime);
		chart2dST.setMinPaintLatency(1000);
		
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
	
	private void updateGraph()
	{
		for (int row=0;row<table.getRowsCount();row++) 
		{
				String tabName = table.getValueAt(row,0);
				String value = table.getValueAt(row,4);
				ITrace2D tempT = traceMapST.get(tabName);
			
				// Set the values
				tempT.addPoint(traceAdds,Integer.parseInt(value));		
			
		}
		
		runTime.addPoint(traceAdds,0);
		
		traceAdds++;
	}
	
	/*
	 * Add a trace.
	 */
	private void addTrace(String name)
	{
		ITrace2D tempT = traceMapST.get(name);
				
		if(tempT == null)
		{
			tempT = new Trace2DLtd(stSamWin);
			tempT.setName(name);
		
			cOffset+=0.13f;
			cOffset=cOffset%1f;
			tempT.setColor( new Color(Color.HSBtoRGB(cOffset,0.9f,1f)));
			tempT.setStroke(new BasicStroke(1));
			traceMapST.put(name,tempT);
			chart2dST.addTrace(tempT);
			tempT.setPointHighlighter(new PointPainterDisc(4));
		}
		
	}
	
	private void clearTrace(String name)
	{
		ITrace2D tempT = traceMapST.remove(name);
		
		if(tempT!=null)
		{
			chart2dST.removeTrace(tempT);		
		}
		
	}
	
	/*private void refresh()
	{
		  simulationInfoTab.clearTable();
		  
		  for(int i = 0;i<getTabCount();i++)
		  {
			  if(getComponentAt (i)!=null)
			  {
				  if(getComponentAt(i).getClass().equals(SimulationTabPanel.class))
				  {
					  	SimulationTabPanel temp = (SimulationTabPanel) getComponentAt (i);

					  	int simId = temp.getSimulationId();
					  	
					  	SimulationState state = simsManager.getSimState(simId);
					  	
					  	if(state == SimulationState.RUNNING)
					  	{
							setIconAt(i, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-start.png")));							
							this.setTitleAt(i, temp.getTitle());
							simulationInfoTab.addRow(getTitleAt(i), new String[]{state.toString(), temp.getStepNo(),temp.getASPS(), temp.getTime()});
					  		
					  	}
					  	else if(state == SimulationState.PAUSED)
					  	{
							setIconAt(i, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-pause.png")));
							this.setTitleAt(i, temp.getTitle());					  		
					  	}
					  	else if(state == SimulationState.NEW)
					  	{
							setIconAt(i, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/media-playback-stop.png")));
							this.setTitleAt(i, temp.getTitle());
					  	}
					  	else // Finished
					  	{
							setIconAt(i, new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/task-complete.png")));
							this.setTitleAt(i, temp.getTitle());
					  	}
				  }	
			  }  
		  }
		  
		  simulationInfoTab.update();
	}*/
	
	
	public String getTabName()
	{
		return name;
	}

	@Override
	public void SimulationsManagerEvent(final int simId, SimulationManagerEvent event)
	{
		// Getting access to simulationListTabPanel via this is not possible in the runnables
		final SimulationListTabPanel simulationListTabPanel = this;
		
		if(event == SimulationManagerEvent.AddedSim)
		{			
			
		    javax.swing.SwingUtilities.invokeLater(new Runnable() 
		    {
		        public void run() 
		        {	
		        	// Add teh R
		        	simulationListTabPanel.addRow("Simulation " + simId, new String[] {"New", "0", "0","0","0"});
					
					// Add Trace
					addTrace("Simulation " + simId);
		        	
					// RegiserStateListener
					simsManger.addSimulationStateListener(simId, simulationListTabPanel);
					
					// RegisterStatsListerner
					simsManger.addSimulationStatListener(simId, simulationListTabPanel);
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
		        	simsManger.removeSimulationStatListener(simId, simulationListTabPanel);
		        	
					// UnRegiserStateListener
					simsManger.removeSimulationStateListener(simId, simulationListTabPanel);
					
					// RemoveTrace
					clearTrace("Simulation " + simId);
					
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
		updateCells("Simulation " + simId, new int[]{2,3,4,5},new String[]{ Integer.toString(stepNo), Integer.toString(progress), Integer.toString(asps), longTimeToString(time) });	
	}

	@Override
	public void simulationStateChanged(int simId, SimState state)
	{
		// Simulation State
		updateCell("Simulation " + simId, 1 , state.toString());		
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
}
