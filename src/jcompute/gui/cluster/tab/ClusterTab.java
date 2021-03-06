package jcompute.gui.cluster.tab;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import jcompute.JComputeEventBus;
import jcompute.cluster.controlnode.NodeManagerStateMachine.NodeManagerState;
import jcompute.cluster.controlnode.NodeManagerStateMachine.NodeManagerStateMachineEvent;
import jcompute.cluster.controlnode.computenodemanager.event.ComputeNodeStatsUpdate;
import jcompute.cluster.controlnode.event.NodeEvent;
import jcompute.cluster.controlnode.event.NodeEvent.NodeEventType;
import jcompute.cluster.controlnode.event.StatusChanged;
import jcompute.gui.IconManager;
import jcompute.gui.IconManager.IconIndex;
import jcompute.gui.cluster.tablerowitems.NodeConnectionLogRowItem;
import jcompute.gui.cluster.tablerowitems.NodeInfoRowItem;
import jcompute.gui.cluster.tablerowitems.SimpleInfoRowItem;
import jcompute.gui.cluster.tablerowitems.SimulationListRowItem;
import jcompute.gui.component.swing.SwingHelper;
import jcompute.gui.component.swing.jpanel.GlobalStatChartPanel;
import jcompute.gui.component.swing.jpanel.SimpleTabPanel;
import jcompute.gui.component.swing.jpanel.SimpleTabTabTitle;
import jcompute.gui.component.swing.jpanel.TablePanel;
import jcompute.gui.component.tablecell.ColorLabelRenderer;
import jcompute.gui.component.tablecell.EmptyCellColorRenderer;
import jcompute.gui.component.tablecell.HeaderRowRenderer;
import jcompute.gui.component.tablecell.NodeControlButtonRenderer;
import jcompute.gui.component.tablecell.ProgressBarTableCellRenderer;
import jcompute.math.NumericConstants;
import jcompute.simulation.SimulationState.SimState;
import jcompute.simulation.event.SimulationStatChangedEvent;
import jcompute.simulation.event.SimulationStateChangedEvent;
import jcompute.simulationmanager.event.SimulationsManagerEvent;
import jcompute.simulationmanager.event.SimulationsManagerEventType;

public class ClusterTab extends JPanel
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ClusterTab.class);
	private static final long serialVersionUID = 5930193868612200324L;
	
	private final int CHART_HEIGHT = 200;
	
	// Left
	private SimpleTabPanel tabPanel;
	private int stateColumn = 10;
	
	// Tabs
	private JPanel simulationListsContainer;
	private TablePanel<Integer, SimulationListRowItem> activeSimulationsListTable;
	private TablePanel<Integer, SimulationListRowItem> finishedSimulationsListTable;
	
	private TablePanel<Integer, NodeInfoRowItem> clusterConnectedNodesTablePanel;
	private TablePanel<Integer, NodeConnectionLogRowItem> clusterNodesLogTablePanel;
	
	private AtomicInteger eventIds;
	
	// Right
	private TablePanel<String, SimpleInfoRowItem> clusterStatusTablePanel;
	private JScrollPane graphScrollPane;
	private JPanel graphsJPanelContainer;
	
	// Processing
	private GlobalStatChartPanel clusterNodeActiveSims;
	private GlobalStatChartPanel clusterNodeStatsPending;
	private GlobalStatChartPanel clusterSimProChart;
	
	// ComputeNode OS/JVM
	private GlobalStatChartPanel clusterNodeUtilChar;
	private GlobalStatChartPanel clusterNodeMemUsedPerChar;
	private GlobalStatChartPanel clusterNodeBytesTXChar;
	private GlobalStatChartPanel clusterNodeBytesRXChar;
	private GlobalStatChartPanel clusterNodeTXSChar;
	private GlobalStatChartPanel clusterNodeRXSChar;
	private GlobalStatChartPanel clusterNodeRTT;
	private final int legendMaxNodes = 6;
	
	private final int IP4_ADDRESS_SIZE;
	
	public ClusterTab(int rightPanelsMinWidth)
	{
		setLayout(new GridLayout(0, 2, 0, 0));
		
		tabPanel = new SimpleTabPanel();
		
		// Cluster Activity
		simulationListsContainer = new JPanel(new GridLayout(2, 0, 0, 0));
		
		activeSimulationsListTable = new TablePanel<Integer, SimulationListRowItem>(SimulationListRowItem.class, "Active Simulations", true, false);
		
		activeSimulationsListTable.setColumWidth(0, 80);
		activeSimulationsListTable.setColumWidth(1, 70);
		activeSimulationsListTable.setColumWidth(2, 80);
		// activeSimulationsListTable.setColumWidth(3, 65);
		activeSimulationsListTable.setColumWidth(4, 80);
		activeSimulationsListTable.setColumWidth(5, 110);
		// Progress Column uses a progress bar for display
		activeSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(activeSimulationsListTable.getJTable()), 3);
		
		simulationListsContainer.add(activeSimulationsListTable);
		
		finishedSimulationsListTable = new TablePanel<Integer, SimulationListRowItem>(SimulationListRowItem.class, "Finished Simulations", true, false);
		
		finishedSimulationsListTable.setColumWidth(0, 80);
		finishedSimulationsListTable.setColumWidth(1, 70);
		finishedSimulationsListTable.setColumWidth(2, 80);
		// activeSimulationsListTable.setColumWidth(3, 65);
		finishedSimulationsListTable.setColumWidth(4, 80);
		finishedSimulationsListTable.setColumWidth(5, 110);
		// Progress Column uses a progress bar for display
		finishedSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(finishedSimulationsListTable.getJTable()), 3);
		simulationListsContainer.add(finishedSimulationsListTable);
		
		// Connected Nodes Tab
		clusterConnectedNodesTablePanel = new TablePanel<Integer, NodeInfoRowItem>(NodeInfoRowItem.class, true, false, true);
		
		clusterConnectedNodesTablePanel.setColumWidth(0, 50);
		clusterConnectedNodesTablePanel.setColumWidth(1, 65);
		
		IP4_ADDRESS_SIZE = SwingHelper.getInstance().getFontStringWidth(clusterConnectedNodesTablePanel.getFont(), "255.255.255.255");
		
		clusterConnectedNodesTablePanel.setColumWidth(2, IP4_ADDRESS_SIZE);
		clusterConnectedNodesTablePanel.setColumWidth(3, 65);
		clusterConnectedNodesTablePanel.setColumWidth(4, 65);
		clusterConnectedNodesTablePanel.setColumWidth(5, 75);
		clusterConnectedNodesTablePanel.setColumWidth(6, 60);
		clusterConnectedNodesTablePanel.setColumWidth(7, 65);
		clusterConnectedNodesTablePanel.setColumWidth(8, 75);
		clusterConnectedNodesTablePanel.setColumWidth(9, 120);
		clusterConnectedNodesTablePanel.setColumWidth(stateColumn, 75);
		
		clusterConnectedNodesTablePanel.addColumRenderer(new NodeControlButtonRenderer(clusterConnectedNodesTablePanel, stateColumn, IconManager.retrieveIcon(
		IconIndex.start16), IconManager.retrieveIcon(IconIndex.pause16), IconManager.retrieveIcon(IconIndex.stop16)), stateColumn);
		
		clusterConnectedNodesTablePanel.addColumRenderer(new ColorLabelRenderer(), 0);
		
		clusterNodesLogTablePanel = new TablePanel<Integer, NodeConnectionLogRowItem>(NodeConnectionLogRowItem.class, true, false, false);
		clusterNodesLogTablePanel.addColumRenderer(new ColorLabelRenderer(), 1);
		eventIds = new AtomicInteger();
		
		clusterNodesLogTablePanel.setColumWidth(0, 50);
		clusterNodesLogTablePanel.setColumWidth(1, 50);
		clusterNodesLogTablePanel.setColumWidth(2, IP4_ADDRESS_SIZE);
		clusterNodesLogTablePanel.setColumWidth(3, 90);
		
		/*
		 * ****************************************************
		 * Right
		 ****************************************************/
		
		// Cluster Info
		clusterStatusTablePanel = new TablePanel<String, SimpleInfoRowItem>(SimpleInfoRowItem.class, false, false);
		clusterStatusTablePanel.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());
		clusterStatusTablePanel.addColumRenderer(new HeaderRowRenderer(clusterStatusTablePanel.getJTable()), 0);
		
		// Populate Fields
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Address", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Port", ""));
		// clusterStatusTablePanel.addRow(new SimpleInfoRowItem("", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Connecting Nodes", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Active Nodes", ""));
		// clusterStatusTablePanel.addRow(new SimpleInfoRowItem("", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Max Active Sims", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Added Sims", ""));
		
		clusterStatusTablePanel.setMaximumSize(new Dimension(1920, (int) (clusterStatusTablePanel.getJTable().getRowHeight() * 1.5) + (clusterStatusTablePanel
		.getJTable().getRowCount() * clusterStatusTablePanel.getJTable().getRowHeight())));
		clusterStatusTablePanel.setPreferredSize(new Dimension(600, (int) (clusterStatusTablePanel.getJTable().getRowHeight() * 1.5) + (clusterStatusTablePanel
		.getJTable().getRowCount() * clusterStatusTablePanel.getJTable().getRowHeight())));
		
		graphsJPanelContainer = new JPanel();
		GridBagLayout gbl_graphsJPanelContainer = new GridBagLayout();
		gbl_graphsJPanelContainer.rowWeights = new double[]
		{
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0
		};
		gbl_graphsJPanelContainer.columnWeights = new double[]
		{
			1.0
		};
		graphsJPanelContainer.setLayout(gbl_graphsJPanelContainer);
		
		graphScrollPane = new JScrollPane(graphsJPanelContainer);
		
		graphScrollPane.setPreferredSize(new Dimension(600, 240));
		graphScrollPane.getVerticalScrollBar().setUnitIncrement(15);
		
		clusterSimProChart = new GlobalStatChartPanel("Simulations Processed", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterSimProChart.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterSimProChart.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeActiveSims = new GlobalStatChartPanel("Active Simulations", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeActiveSims.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeActiveSims.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeStatsPending = new GlobalStatChartPanel("Statistics Pending", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeStatsPending.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeStatsPending.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeUtilChar = new GlobalStatChartPanel("CPU Utilisation", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeUtilChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeUtilChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeMemUsedPerChar = new GlobalStatChartPanel("JVM Mem Utilisation", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeMemUsedPerChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeMemUsedPerChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeBytesTXChar = new GlobalStatChartPanel("Network Mbits Tx", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeBytesTXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeBytesTXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeTXSChar = new GlobalStatChartPanel("Network TXs", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeTXSChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeTXSChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeBytesRXChar = new GlobalStatChartPanel("Network Mbits Rx", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeBytesRXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeBytesRXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeRXSChar = new GlobalStatChartPanel("Network RXs", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeRXSChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeRXSChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeRTT = new GlobalStatChartPanel("Round Trip (ms)", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeRTT.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeRTT.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		// Cluster Info
		GridBagConstraints gbConstraints0 = new GridBagConstraints();
		gbConstraints0.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints0.gridx = 0;
		gbConstraints0.gridy = 0;
		graphsJPanelContainer.add(clusterStatusTablePanel, gbConstraints0);
		
		// Processing
		GridBagConstraints gbConstraints1 = new GridBagConstraints();
		gbConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints1.gridx = 0;
		gbConstraints1.gridy = 1;
		graphsJPanelContainer.add(clusterNodeActiveSims, gbConstraints1);
		
		GridBagConstraints gbConstraints2 = new GridBagConstraints();
		gbConstraints2.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints2.gridx = 0;
		gbConstraints2.gridy = 2;
		graphsJPanelContainer.add(clusterNodeStatsPending, gbConstraints2);
		
		GridBagConstraints gbConstraints3 = new GridBagConstraints();
		gbConstraints3.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints3.gridx = 0;
		gbConstraints3.gridy = 3;
		graphsJPanelContainer.add(clusterSimProChart, gbConstraints3);
		
		// ComputeNode OS/JVM
		GridBagConstraints gbConstraints4 = new GridBagConstraints();
		gbConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints4.gridx = 0;
		gbConstraints4.gridy = 4;
		graphsJPanelContainer.add(clusterNodeUtilChar, gbConstraints4);
		
		GridBagConstraints gbConstraints5 = new GridBagConstraints();
		gbConstraints5.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints5.gridx = 0;
		gbConstraints5.gridy = 5;
		graphsJPanelContainer.add(clusterNodeMemUsedPerChar, gbConstraints5);
		
		GridBagConstraints gbConstraints6 = new GridBagConstraints();
		gbConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints6.gridx = 0;
		gbConstraints6.gridy = 6;
		graphsJPanelContainer.add(clusterNodeBytesTXChar, gbConstraints6);
		
		GridBagConstraints gbConstraints7 = new GridBagConstraints();
		gbConstraints7.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints7.gridx = 0;
		gbConstraints7.gridy = 7;
		graphsJPanelContainer.add(clusterNodeBytesRXChar, gbConstraints7);
		
		GridBagConstraints gbConstraints8 = new GridBagConstraints();
		gbConstraints8.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints8.gridx = 0;
		gbConstraints8.gridy = 8;
		graphsJPanelContainer.add(clusterNodeTXSChar, gbConstraints8);
		
		GridBagConstraints gbConstraints9 = new GridBagConstraints();
		gbConstraints9.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints9.gridx = 0;
		gbConstraints9.gridy = 9;
		graphsJPanelContainer.add(clusterNodeRXSChar, gbConstraints9);
		
		GridBagConstraints gbConstraints10 = new GridBagConstraints();
		gbConstraints10.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints10.gridx = 0;
		gbConstraints10.gridy = 10;
		graphsJPanelContainer.add(clusterNodeRTT, gbConstraints10);
		
		tabPanel.addTab(simulationListsContainer, new SimpleTabTabTitle(160, IconManager.retrieveIcon(IconIndex.simListTab16), "Activity"));
		
		tabPanel.addTab(clusterConnectedNodesTablePanel, new SimpleTabTabTitle(160, IconManager.retrieveIcon(IconIndex.nodesTab16), "Connected Nodes"));
		
		tabPanel.addTab(clusterNodesLogTablePanel, new SimpleTabTabTitle(160, IconManager.retrieveIcon(IconIndex.loggingTab16), "Nodes Log"));
		
		this.add(tabPanel);
		this.add(graphScrollPane);
		
		// Register on the event bus
		JComputeEventBus.register(this);
	}
	
	/*
	 * ************************************************************************************************************************************************************
	 * Event Bus Subscribers
	 * ************************************************************************************************************************************************************
	 */
	
	@Subscribe
	public void ControlNodeEvent(NodeEvent e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				NodeEventType eventType = e.getEventType();
				
				int nid = e.getNodeConfiguration().getUid();
				String nodeId = Integer.toString(e.getNodeConfiguration().getUid());
				
				switch(eventType)
				{
					case CONNECTING:
						clusterNodesLogTablePanel.addRow(new NodeConnectionLogRowItem(eventIds.incrementAndGet(), nid, e.getNodeConfiguration().getAddress(),
						eventType.name(), new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
					break;
					case CONNECTED:
						
						// Assuming Starting State
						clusterConnectedNodesTablePanel.addRow(new NodeInfoRowItem(e.getNodeConfiguration(), NodeManagerState.RUNNING.getNumber()));
						
						clusterSimProChart.addStat(nodeId, nid);
						clusterNodeActiveSims.addStat(nodeId, nid);
						clusterNodeStatsPending.addStat(nodeId, nid);
						clusterNodeUtilChar.addStat(nodeId, nid);
						clusterNodeMemUsedPerChar.addStat(nodeId, nid);
						clusterNodeBytesTXChar.addStat(nodeId, nid);
						clusterNodeBytesRXChar.addStat(nodeId, nid);
						clusterNodeTXSChar.addStat(nodeId, nid);
						clusterNodeRXSChar.addStat(nodeId, nid);
						clusterNodeRTT.addStat(nodeId, nid);
						
						clusterNodesLogTablePanel.addRow(new NodeConnectionLogRowItem(eventIds.incrementAndGet(), nid, e.getNodeConfiguration().getAddress(),
						eventType.name(), new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
					break;
					// Deliberate
					case REMOVED:
					case DISCONNECTED:
						
						clusterConnectedNodesTablePanel.removeRow(nid);
						
						clusterNodeUtilChar.removeStat(nodeId);
						clusterNodeMemUsedPerChar.removeStat(nodeId);
						clusterSimProChart.removeStat(nodeId);
						clusterNodeActiveSims.removeStat(nodeId);
						clusterNodeStatsPending.removeStat(nodeId);
						clusterNodeBytesTXChar.removeStat(nodeId);
						clusterNodeBytesRXChar.removeStat(nodeId);
						clusterNodeTXSChar.removeStat(nodeId);
						clusterNodeRXSChar.removeStat(nodeId);
						clusterNodeRTT.removeStat(nodeId);
						
						clusterNodesLogTablePanel.addRow(new NodeConnectionLogRowItem(eventIds.incrementAndGet(), nid, e.getNodeConfiguration().getAddress(),
						eventType.name(), new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
					default:
					
					break;
				}
			}
		});
		
	}
	
	@Subscribe
	public void ControlNodeEvent(StatusChanged e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				clusterStatusTablePanel.updateRow("Address", new SimpleInfoRowItem("Address", e.getAddress()));
				clusterStatusTablePanel.updateRow("Port", new SimpleInfoRowItem("Port", e.getPort()));
				clusterStatusTablePanel.updateRow("Connecting Nodes", new SimpleInfoRowItem("Connecting Nodes", e.getConnectingNodes()));
				clusterStatusTablePanel.updateRow("Active Nodes", new SimpleInfoRowItem("Active Nodes", e.getActiveNodes()));
				clusterStatusTablePanel.updateRow("Max Active Sims", new SimpleInfoRowItem("Max Active Sims", e.getMaxActiveSims()));
				clusterStatusTablePanel.updateRow("Added Sims", new SimpleInfoRowItem("Added Sims", e.getAddedSims()));
			}
		});
	}
	
	@Subscribe
	public void ComputeNodeManagerStateChange(NodeManagerStateMachineEvent e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				clusterConnectedNodesTablePanel.updateCell(e.getUid(), stateColumn, e.getStateNum());
			}
		});
	}
	
	@Subscribe
	public void NodeStatsUpdateEvent(ComputeNodeStatsUpdate e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				String nodeId = Integer.toString(e.getNodeId());
				
				clusterSimProChart.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getSimulationsProcessed());
				clusterNodeActiveSims.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getSimulationsActive());
				clusterNodeStatsPending.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getStatisticsPendingFetch());
				clusterNodeUtilChar.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getCpuUsage());
				clusterNodeMemUsedPerChar.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getJvmMemoryUsedPercentage());
				
				// MegaBits sent since last update eg (bits / 1000000)
				double txMBits = NumericConstants.BytesToBits(e.getStats().getBytesTX()) / 1000000.0;
				double rxMBits = NumericConstants.BytesToBits(e.getStats().getBytesRX()) / 1000000.0;
				
				clusterNodeBytesTXChar.statUpdate(nodeId, e.getSequenceNum(), txMBits);
				clusterNodeBytesRXChar.statUpdate(nodeId, e.getSequenceNum(), rxMBits);
				
				clusterNodeTXSChar.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getTXS());
				clusterNodeRXSChar.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getRXS());
				clusterNodeRTT.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getAvgRTT() / 1000000.0);
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
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				SimulationsManagerEventType type = e.getEventType();
				int simId = e.getSimId();
				
				if(type == SimulationsManagerEventType.AddedSim)
				{
					log.debug("Add Row for " + "Simulation " + simId);
					
					// Add the row
					activeSimulationsListTable.addRow(new SimulationListRowItem(simId));
				}
				else if(type == SimulationsManagerEventType.RemovedSim)
				{
					log.debug("Removing Row for " + "Simulation " + simId);
					
					// Remove the Row
					finishedSimulationsListTable.removeRow(simId);
				}
				else
				{
					log.error("Unhandled SimulationManagerEvent in Batch GUI");
				}
			}
		});
	}
	
	@Subscribe
	public void SimulationStatChanged(SimulationStatChangedEvent e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				activeSimulationsListTable.updateCells(e.getSimId(), new int[]
				{
					2, 3, 4, 5
				}, new Object[]
				{
					e.getStepNo(), e.getProgress(), e.getAsps(), e.getTime()
				});
			}
		});
	}
	
	@Subscribe
	public void SimulationStateChanged(SimulationStateChangedEvent e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if(e.getState() == SimState.FINISHED)
				{
					activeSimulationsListTable.removeRow(e.getSimId());
					finishedSimulationsListTable.addRow(new SimulationListRowItem(e.getSimId(), e.getState(), (int) e.getStepCount(), 100, 0, e.getRunTime()));
				}
				else
				{
					activeSimulationsListTable.updateCells(e.getSimId(), new int[]
					{
						1
					}, new Object[]
					{
						e.getState()
					});
				}
			}
		});
	}
}
