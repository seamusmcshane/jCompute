package jCompute.Gui.Cluster;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.common.eventbus.Subscribe;

import jCompute.IconManager;
import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.NodeManager.NodeManagerState;
import jCompute.Cluster.Controller.Event.NodeManagerStateChange;
import jCompute.Cluster.Controller.Event.NodeAdded;
import jCompute.Cluster.Controller.Event.NodeRemoved;
import jCompute.Cluster.Controller.Event.NodeStatsUpdate;
import jCompute.Gui.Cluster.TableRowItems.NodeConnectionLogRowItem;
import jCompute.Gui.Cluster.TableRowItems.NodeInfoRowItem;
import jCompute.Gui.Component.Swing.GlobalStatChartPanel;
import jCompute.Gui.Component.Swing.SimpleTabPanel;
import jCompute.Gui.Component.Swing.SimpleTabTabTitle;
import jCompute.Gui.Component.Swing.TablePanel;
import jCompute.Gui.Component.TableCell.ColorLabelRenderer;
import jCompute.Gui.Component.TableCell.NodeControlButtonRenderer;

import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeStatusTab extends JPanel
{
	private static final long serialVersionUID = 5930193868612200324L;
	
	private final int MEGABYTE = 1048576;
	private final int CHART_HEIGHT = 200;
	
	// Left
	private SimpleTabPanel tabPanel;
	private int stateColumn = 10;
	
	// Tabs
	private TablePanel clusterConnectedNodesTablePanel;
	private TablePanel clusterNodesLogTablePanel;
	
	private AtomicInteger eventIds;
	
	// Right
	private JScrollPane graphScrollPane;
	private JPanel graphsJPanelContainer;
	
	// Processing
	private GlobalStatChartPanel clusterNodeActiveSims;
	private GlobalStatChartPanel clusterNodeStatsPending;
	private GlobalStatChartPanel clusterSimProChart;
	
	// Node OS/JVM
	private GlobalStatChartPanel clusterNodeUtilChar;
	private GlobalStatChartPanel clusterNodeMemUsedPerChar;
	private GlobalStatChartPanel clusterNodeTXChar;
	private GlobalStatChartPanel clusterNodeRXChar;
	private final int legendMaxNodes = 6;
	
	public NodeStatusTab(int rightPanelsMinWidth)
	{
		setLayout(new GridLayout(0, 2, 0, 0));
		
		tabPanel = new SimpleTabPanel();
		
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
		
		clusterConnectedNodesTablePanel.addColumRenderer(new NodeControlButtonRenderer(clusterConnectedNodesTablePanel, stateColumn,
				IconManager.getIcon("startSimIcon"), IconManager.getIcon("pauseSimIcon"), IconManager.getIcon("stopIcon")), stateColumn);
				
		clusterConnectedNodesTablePanel.addColumRenderer(new ColorLabelRenderer(), 0);
		
		clusterNodesLogTablePanel = new TablePanel(NodeConnectionLogRowItem.class, 0, true, false, false);
		clusterNodesLogTablePanel.addColumRenderer(new ColorLabelRenderer(), 1);
		eventIds = new AtomicInteger();
		
		clusterNodesLogTablePanel.setColumWidth(0, 50);
		clusterNodesLogTablePanel.setColumWidth(1, 50);
		clusterNodesLogTablePanel.setColumWidth(2, 90);
		clusterNodesLogTablePanel.setColumWidth(3, 75);
		
		graphsJPanelContainer = new JPanel();
		
		graphsJPanelContainer.setLayout(new GridLayout(7, 1, 0, 0));
		
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
		
		clusterNodeTXChar = new GlobalStatChartPanel("Network TX", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeTXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeTXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeRXChar = new GlobalStatChartPanel("Network RX", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeRXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeRXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		// Processing
		graphsJPanelContainer.add(clusterNodeActiveSims);
		graphsJPanelContainer.add(clusterNodeStatsPending);
		graphsJPanelContainer.add(clusterSimProChart);
		
		// Node OS/JVM
		graphsJPanelContainer.add(clusterNodeUtilChar);
		graphsJPanelContainer.add(clusterNodeMemUsedPerChar);
		graphsJPanelContainer.add(clusterNodeTXChar);
		graphsJPanelContainer.add(clusterNodeRXChar);
		
		ImageIcon nodesIcon = IconManager.getIcon("Nodes16");
		tabPanel.addTab(clusterConnectedNodesTablePanel, new SimpleTabTabTitle(160, nodesIcon, "Connected Nodes"));
		
		ImageIcon logIcon = IconManager.getIcon("Log16");
		tabPanel.addTab(clusterNodesLogTablePanel, new SimpleTabTabTitle(160, logIcon, "Nodes Log"));
		
		this.add(tabPanel);
		this.add(graphScrollPane);
		
		// Register on the event bus
		JComputeEventBus.register(this);
	}
	
	@Subscribe
	public void ControlNodeEvent(NodeAdded e)
	{
		int nid = e.getNodeConfiguration().getUid();
		String nodeId = "Node " + e.getNodeConfiguration().getUid();
		
		// Assuming Starting State
		clusterConnectedNodesTablePanel.addRow(new NodeInfoRowItem(e.getNodeConfiguration(), NodeManagerState.RUNNING.ordinal()));
		
		clusterSimProChart.addStat(nodeId, nid);
		clusterNodeActiveSims.addStat(nodeId, nid);
		clusterNodeStatsPending.addStat(nodeId, nid);
		clusterNodeUtilChar.addStat(nodeId, nid);
		clusterNodeMemUsedPerChar.addStat(nodeId, nid);
		clusterNodeTXChar.addStat(nodeId, nid);
		clusterNodeRXChar.addStat(nodeId, nid);
		
		clusterNodesLogTablePanel
				.addRow(new NodeConnectionLogRowItem(eventIds.incrementAndGet(), nid, e.getNodeConfiguration().getAddress(), "Added",
						new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
	}
	
	@Subscribe
	public void NodeManagerStateChange(NodeManagerStateChange e)
	{
		clusterConnectedNodesTablePanel.updateCell(e.getUid(), stateColumn, e.getState().ordinal());
	}
	
	@Subscribe
	public void ControlNodeEvent(NodeRemoved e)
	{
		int nid = e.getNodeConfiguration().getUid();
		String nodeId = "Node " + e.getNodeConfiguration().getUid();
		
		clusterConnectedNodesTablePanel.removeRow(nid);
		
		clusterNodeUtilChar.removeStat(nodeId);
		clusterNodeMemUsedPerChar.removeStat(nodeId);
		clusterSimProChart.removeStat(nodeId);
		clusterNodeActiveSims.removeStat(nodeId);
		clusterNodeStatsPending.removeStat(nodeId);
		clusterNodeTXChar.removeStat(nodeId);
		clusterNodeRXChar.removeStat(nodeId);
		
		clusterNodesLogTablePanel
				.addRow(new NodeConnectionLogRowItem(eventIds.incrementAndGet(), nid, e.getNodeConfiguration().getAddress(), "Removed",
						new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
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
		clusterNodeTXChar.statUpdate(nodeId, e.getSequenceNum(), (e.getStats().getBytesTX() / MEGABYTE));
		clusterNodeRXChar.statUpdate(nodeId, e.getSequenceNum(), (e.getStats().getBytesRX() / MEGABYTE));
	}
}
