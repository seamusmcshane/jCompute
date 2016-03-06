package jCompute.Gui.Cluster;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jCompute.IconManager;
import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.ControlNode.Event.NodeEvent;
import jCompute.Cluster.Controller.ControlNode.Event.StatusChanged;
import jCompute.Cluster.Controller.ControlNode.Event.NodeEvent.NodeEventType;
import jCompute.Cluster.Controller.NodeManager.Event.NodeManagerStateChange;
import jCompute.Cluster.Controller.NodeManager.Event.NodeStatsUpdate;
import jCompute.Cluster.Controller.NodeManager.NodeManager.NodeManagerState;
import jCompute.Gui.Cluster.TableRowItems.NodeConnectionLogRowItem;
import jCompute.Gui.Cluster.TableRowItems.NodeInfoRowItem;
import jCompute.Gui.Cluster.TableRowItems.SimpleInfoRowItem;
import jCompute.Gui.Cluster.TableRowItems.SimulationListRowItem;
import jCompute.Gui.Component.Swing.GlobalStatChartPanel;
import jCompute.Gui.Component.Swing.SimpleTabPanel;
import jCompute.Gui.Component.Swing.SimpleTabTabTitle;
import jCompute.Gui.Component.Swing.TablePanel;
import jCompute.Gui.Component.TableCell.ColorLabelRenderer;
import jCompute.Gui.Component.TableCell.EmptyCellColorRenderer;
import jCompute.Gui.Component.TableCell.HeaderRowRenderer;
import jCompute.Gui.Component.TableCell.NodeControlButtonRenderer;
import jCompute.Gui.Component.TableCell.ProgressBarTableCellRenderer;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEventType;

import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class ClusterTab extends JPanel
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ClusterTab.class);
	private static final long serialVersionUID = 5930193868612200324L;
	
	private final int CHART_HEIGHT = 200;
	
	// Left
	private SimpleTabPanel tabPanel;
	private int stateColumn = 10;
	
	// Tabs
	private JPanel simulationListsContainer;
	private TablePanel activeSimulationsListTable;
	private TablePanel finishedSimulationsListTable;
	
	private TablePanel clusterConnectedNodesTablePanel;
	private TablePanel clusterNodesLogTablePanel;
	
	private AtomicInteger eventIds;
	
	// Right
	private TablePanel clusterStatusTablePanel;
	private JScrollPane graphScrollPane;
	private JPanel graphsJPanelContainer;
	
	// Processing
	private GlobalStatChartPanel clusterNodeActiveSims;
	private GlobalStatChartPanel clusterNodeStatsPending;
	private GlobalStatChartPanel clusterSimProChart;
	
	// Node OS/JVM
	private GlobalStatChartPanel clusterNodeUtilChar;
	private GlobalStatChartPanel clusterNodeMemUsedPerChar;
	private GlobalStatChartPanel clusterNodeBytesTXChar;
	private GlobalStatChartPanel clusterNodeBytesRXChar;
	private GlobalStatChartPanel clusterNodeTXSChar;
	private GlobalStatChartPanel clusterNodeRXSChar;
	private final int legendMaxNodes = 6;
	
	public ClusterTab(int rightPanelsMinWidth)
	{
		setLayout(new GridLayout(0, 2, 0, 0));
		
		tabPanel = new SimpleTabPanel();
		
		// Cluster Activity
		simulationListsContainer = new JPanel(new GridLayout(2, 0, 0, 0));
		
		activeSimulationsListTable = new TablePanel(SimulationListRowItem.class, 0, "Active Simulations", true, false);
		
		activeSimulationsListTable.setColumWidth(0, 80);
		activeSimulationsListTable.setColumWidth(1, 70);
		activeSimulationsListTable.setColumWidth(2, 80);
		// activeSimulationsListTable.setColumWidth(3, 65);
		activeSimulationsListTable.setColumWidth(4, 80);
		activeSimulationsListTable.setColumWidth(5, 110);
		// Progress Column uses a progress bar for display
		activeSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		
		simulationListsContainer.add(activeSimulationsListTable);
		
		finishedSimulationsListTable = new TablePanel(SimulationListRowItem.class, 0, "Finished Simulations", true, false);
		
		finishedSimulationsListTable.setColumWidth(0, 80);
		finishedSimulationsListTable.setColumWidth(1, 70);
		finishedSimulationsListTable.setColumWidth(2, 80);
		// activeSimulationsListTable.setColumWidth(3, 65);
		finishedSimulationsListTable.setColumWidth(4, 80);
		finishedSimulationsListTable.setColumWidth(5, 110);
		// Progress Column uses a progress bar for display
		finishedSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		simulationListsContainer.add(finishedSimulationsListTable);
		
		// Connected Nodes Tab
		clusterConnectedNodesTablePanel = new TablePanel(NodeInfoRowItem.class, 0, true, false, true);
		
		clusterConnectedNodesTablePanel.setColumWidth(0, 50);
		clusterConnectedNodesTablePanel.setColumWidth(1, 65);
		clusterConnectedNodesTablePanel.setColumWidth(2, 90);
		clusterConnectedNodesTablePanel.setColumWidth(3, 65);
		clusterConnectedNodesTablePanel.setColumWidth(4, 65);
		clusterConnectedNodesTablePanel.setColumWidth(5, 75);
		clusterConnectedNodesTablePanel.setColumWidth(6, 60);
		clusterConnectedNodesTablePanel.setColumWidth(7, 65);
		clusterConnectedNodesTablePanel.setColumWidth(8, 75);
		clusterConnectedNodesTablePanel.setColumWidth(9, 120);
		clusterConnectedNodesTablePanel.setColumWidth(stateColumn, 75);
		
		clusterConnectedNodesTablePanel.addColumRenderer(
				new NodeControlButtonRenderer(clusterConnectedNodesTablePanel, stateColumn, IconManager.getIcon("startSimIcon"), IconManager.getIcon("pauseSimIcon"), IconManager.getIcon("stopIcon")),
				stateColumn);
				
		clusterConnectedNodesTablePanel.addColumRenderer(new ColorLabelRenderer(), 0);
		
		clusterNodesLogTablePanel = new TablePanel(NodeConnectionLogRowItem.class, 0, true, false, false);
		clusterNodesLogTablePanel.addColumRenderer(new ColorLabelRenderer(), 1);
		eventIds = new AtomicInteger();
		
		clusterNodesLogTablePanel.setColumWidth(0, 50);
		clusterNodesLogTablePanel.setColumWidth(1, 50);
		clusterNodesLogTablePanel.setColumWidth(2, 90);
		clusterNodesLogTablePanel.setColumWidth(3, 90);
		
		/*
		 * ****************************************************
		 * Right
		 ****************************************************/
		
		// Cluster Info
		clusterStatusTablePanel = new TablePanel(SimpleInfoRowItem.class, 0, false, false);
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
		
		clusterStatusTablePanel.setMaximumSize(new Dimension(1920,
				(int) (clusterStatusTablePanel.getJTable().getRowHeight() * 1.5) + (clusterStatusTablePanel.getJTable().getRowCount() * clusterStatusTablePanel.getJTable().getRowHeight())));
		clusterStatusTablePanel.setPreferredSize(new Dimension(600,
				(int) (clusterStatusTablePanel.getJTable().getRowHeight() * 1.5) + (clusterStatusTablePanel.getJTable().getRowCount() * clusterStatusTablePanel.getJTable().getRowHeight())));
				
		graphsJPanelContainer = new JPanel();
		GridBagLayout gbl_graphsJPanelContainer = new GridBagLayout();
		gbl_graphsJPanelContainer.rowWeights = new double[]
		{
			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0
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
		
		clusterNodeUtilChar = new GlobalStatChartPanel("Node CPU Utilisation", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeUtilChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeUtilChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeMemUsedPerChar = new GlobalStatChartPanel("Node JVM Mem Utilisation", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeMemUsedPerChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeMemUsedPerChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeBytesTXChar = new GlobalStatChartPanel("Network (Avg)kBytes Tx", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeBytesTXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeBytesTXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeTXSChar = new GlobalStatChartPanel("Network TXs", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeTXSChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeTXSChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeBytesRXChar = new GlobalStatChartPanel("Network (Avg)kBytes Rx", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeBytesRXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeBytesRXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeRXSChar = new GlobalStatChartPanel("Network RXs", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeRXSChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeRXSChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
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
		
		// Node OS/JVM
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
		
		ImageIcon clusterIcon = IconManager.getIcon("simulationListTabIcon16");
		tabPanel.addTab(simulationListsContainer, new SimpleTabTabTitle(160, clusterIcon, "Activity"));
		
		ImageIcon nodesIcon = IconManager.getIcon("Nodes16");
		tabPanel.addTab(clusterConnectedNodesTablePanel, new SimpleTabTabTitle(160, nodesIcon, "Connected Nodes"));
		
		ImageIcon logIcon = IconManager.getIcon("Log16");
		tabPanel.addTab(clusterNodesLogTablePanel, new SimpleTabTabTitle(160, logIcon, "Nodes Log"));
		
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
		NodeEventType eventType = e.getEventType();
		
		int nid = e.getNodeConfiguration().getUid();
		String nodeId = "Node " + e.getNodeConfiguration().getUid();
		
		switch(eventType)
		{
			case CONNECTING:
				clusterNodesLogTablePanel.addRow(new NodeConnectionLogRowItem(eventIds.incrementAndGet(), nid, e.getNodeConfiguration().getAddress(), eventType.name(),
						new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
			break;
			case CONNECTED:
				
				// Assuming Starting State
				clusterConnectedNodesTablePanel.addRow(new NodeInfoRowItem(e.getNodeConfiguration(), NodeManagerState.RUNNING.ordinal()));
				
				clusterSimProChart.addStat(nodeId, nid);
				clusterNodeActiveSims.addStat(nodeId, nid);
				clusterNodeStatsPending.addStat(nodeId, nid);
				clusterNodeUtilChar.addStat(nodeId, nid);
				clusterNodeMemUsedPerChar.addStat(nodeId, nid);
				clusterNodeBytesTXChar.addStat(nodeId, nid);
				clusterNodeBytesRXChar.addStat(nodeId, nid);
				clusterNodeTXSChar.addStat(nodeId, nid);
				clusterNodeRXSChar.addStat(nodeId, nid);
				
				clusterNodesLogTablePanel.addRow(new NodeConnectionLogRowItem(eventIds.incrementAndGet(), nid, e.getNodeConfiguration().getAddress(), eventType.name(),
						new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
			break;
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
				
				clusterNodesLogTablePanel.addRow(new NodeConnectionLogRowItem(eventIds.incrementAndGet(), nid, e.getNodeConfiguration().getAddress(), eventType.name(),
						new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
			break;
			default:
			
			break;
		}
	}
	
	@Subscribe
	public void ControlNodeEvent(StatusChanged e)
	{
		clusterStatusTablePanel.updateRow("Address", new SimpleInfoRowItem("Address", e.getAddress()));
		clusterStatusTablePanel.updateRow("Port", new SimpleInfoRowItem("Port", e.getPort()));
		clusterStatusTablePanel.updateRow("Connecting Nodes", new SimpleInfoRowItem("Connecting Nodes", e.getConnectingNodes()));
		clusterStatusTablePanel.updateRow("Active Nodes", new SimpleInfoRowItem("Active Nodes", e.getActiveNodes()));
		clusterStatusTablePanel.updateRow("Max Active Sims", new SimpleInfoRowItem("Max Active Sims", e.getMaxActiveSims()));
		clusterStatusTablePanel.updateRow("Added Sims", new SimpleInfoRowItem("Added Sims", e.getAddedSims()));
	}
	
	@Subscribe
	public void NodeManagerStateChange(NodeManagerStateChange e)
	{
		clusterConnectedNodesTablePanel.updateCell(e.getUid(), stateColumn, e.getState().ordinal());
	}
	
	@Subscribe
	public void NodeStatsUpdateEvent(NodeStatsUpdate e)
	{
		String nodeId = "Node " + e.getNodeId();
		
		clusterSimProChart.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getSimulationsProcessed());
		clusterNodeActiveSims.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getSimulationsActive());
		clusterNodeStatsPending.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getStatisticsPendingFetch());
		clusterNodeUtilChar.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getCpuUsage());
		clusterNodeMemUsedPerChar.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getJvmMemoryUsedPercentage());
		
		// To Avg KiloBytes per second from bytes per 60 seconds
		clusterNodeBytesTXChar.statUpdate(nodeId, e.getSequenceNum(), (e.getStats().getBytesTX() / 1000L / 60L));
		clusterNodeBytesRXChar.statUpdate(nodeId, e.getSequenceNum(), (e.getStats().getBytesRX() / 1000L / 60L));
		
		clusterNodeTXSChar.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getTXS());
		clusterNodeRXSChar.statUpdate(nodeId, e.getSequenceNum(), e.getStats().getRXS());
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
	public void SimulationStateChanged(SimulationStateChangedEvent e)
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
}
