package jCompute.Gui.Cluster;

import java.awt.Dimension;

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
import jCompute.Gui.Cluster.TableRowItems.NodeInfoRowItem;
import jCompute.Gui.Component.Swing.GlobalStatChartPanel;
import jCompute.Gui.Component.Swing.TablePanel;
import jCompute.Gui.Component.TableCell.ColorLabelRenderer;
import jCompute.Gui.Component.TableCell.NodeControlButtonRenderer;

import java.awt.GridLayout;

public class NodeStatusTab extends JPanel
{
	private final int MEGABYTE = 1048576;
	private final int CHART_HEIGHT = 200;
	
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
	
	// Left
	private TablePanel clusterNodesTablePanel;
	private int stateColumn = 10;
	
	private final int legendMaxNodes = 6;
	
	public NodeStatusTab(int rightPanelsMinWidth)
	{
		setLayout(new GridLayout(0, 2, 0, 0));
		
		// Nodes Tab
		clusterNodesTablePanel = new TablePanel(NodeInfoRowItem.class, 0, true, false,true);
		
		clusterNodesTablePanel.setColumWidth(0, 50);
		clusterNodesTablePanel.setColumWidth(1, 65);
		clusterNodesTablePanel.setColumWidth(2, 80);
		clusterNodesTablePanel.setColumWidth(3, 65);
		clusterNodesTablePanel.setColumWidth(4, 65);
		clusterNodesTablePanel.setColumWidth(5, 75);
		clusterNodesTablePanel.setColumWidth(6, 60);
		clusterNodesTablePanel.setColumWidth(7, 65);
		clusterNodesTablePanel.setColumWidth(8, 75);
		clusterNodesTablePanel.setColumWidth(9, 120);
		clusterNodesTablePanel.setColumWidth(stateColumn, 75);
		
		clusterNodesTablePanel.addColumRenderer(
				new NodeControlButtonRenderer(clusterNodesTablePanel, stateColumn, IconManager.getIcon("startSimIcon"), IconManager
						.getIcon("pauseSimIcon"), IconManager.getIcon("stopIcon")), stateColumn);
		
		clusterNodesTablePanel.addColumRenderer(new ColorLabelRenderer(), 0);
		
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
		
		// this.add(clusterNodesTablePanel, BorderLayout.CENTER);
		// splitPane.setRightComponent(clusterNodesTablePanel);
		// nodeStatusTabPanel.addTab(clusterNodesTablePanel, "Information");
		// nodeStatusTabPanel.addTab(scrollPane, "Activity");
		
		// this.add(nodeStatusTabPanel);
		
		this.add(clusterNodesTablePanel);
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
		clusterNodesTablePanel.addRow(new NodeInfoRowItem(e.getNodeConfiguration(), NodeManagerState.RUNNING.ordinal()));
		
		clusterSimProChart.addStat(nodeId, nid);
		clusterNodeActiveSims.addStat(nodeId, nid);
		clusterNodeStatsPending.addStat(nodeId, nid);
		clusterNodeUtilChar.addStat(nodeId, nid);
		clusterNodeMemUsedPerChar.addStat(nodeId, nid);
		clusterNodeTXChar.addStat(nodeId, nid);
		clusterNodeRXChar.addStat(nodeId, nid);
	}
	
	@Subscribe
	public void NodeManagerStateChange(NodeManagerStateChange e)
	{
		clusterNodesTablePanel.updateCell(e.getUid(), stateColumn, e.getState().ordinal());
	}
	
	@Subscribe
	public void ControlNodeEvent(NodeRemoved e)
	{
		int nid = e.getNodeConfiguration().getUid();
		String nodeId = "Node " + e.getNodeConfiguration().getUid();
		
		clusterNodesTablePanel.removeRow(nid);
		
		clusterNodeUtilChar.removeStat(nodeId);
		clusterNodeMemUsedPerChar.removeStat(nodeId);
		clusterSimProChart.removeStat(nodeId);
		clusterNodeActiveSims.removeStat(nodeId);
		clusterNodeStatsPending.removeStat(nodeId);
		clusterNodeTXChar.removeStat(nodeId);
		clusterNodeRXChar.removeStat(nodeId);
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
