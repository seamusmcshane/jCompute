package jCompute.Gui.Cluster;

import java.awt.BorderLayout;
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
import jCompute.Gui.Component.Swing.SimpleTabPanel;
import jCompute.Gui.Component.Swing.TablePanel;
import jCompute.Gui.Component.TableCell.NodeControlButtonRenderer;

import java.awt.GridLayout;

public class NodeStatusTab extends JPanel
{
	private final int MEGABYTE = 1048576;
	private final int CHART_HEIGHT = 175;
	private SimpleTabPanel nodeStatusTabPanel;
	private JScrollPane scrollPane;
	private JPanel container;
	
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
	
	private int rightPanelsMinWidth;
	
	public NodeStatusTab(int rightPanelsMinWidth)
	{
		this.nodeStatusTabPanel = new SimpleTabPanel();
		
		// Min Width of rightPanel
		this.rightPanelsMinWidth = rightPanelsMinWidth;
		
		this.setLayout(new BorderLayout());
		
		container = new JPanel();
		
		container.setLayout(new GridLayout(7, 1, 0, 0));
		
		scrollPane = new JScrollPane(container);
		
		scrollPane.setPreferredSize(new Dimension(600, 240));
		scrollPane.getVerticalScrollBar().setUnitIncrement(15);
		
		// this.add(scrollPane, BorderLayout.NORTH);
		// splitPane.setLeftComponent(scrollPane);
		
		clusterSimProChart = new GlobalStatChartPanel("Simulations Processed", "Nodes", true, false, 60, true);
		clusterSimProChart.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterSimProChart.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeActiveSims = new GlobalStatChartPanel("Active Simulations", "Nodes", true, false, 60, true);
		clusterNodeActiveSims.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeActiveSims.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeStatsPending = new GlobalStatChartPanel("Statistics Pending", "Nodes", true, false, 60, true);
		clusterNodeStatsPending.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeStatsPending.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeUtilChar = new GlobalStatChartPanel("Node CPU Utilisation", "Nodes", true, false, 60, true);
		clusterNodeUtilChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeUtilChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeMemUsedPerChar = new GlobalStatChartPanel("Node JVM Mem Utilisation", "Nodes", true, false, 60, true);
		clusterNodeMemUsedPerChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeMemUsedPerChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeTXChar = new GlobalStatChartPanel("Network TX", "Nodes", true, false, 60, true);
		clusterNodeTXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeTXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeRXChar = new GlobalStatChartPanel("Network RX", "Nodes", true, false, 60, true);
		clusterNodeRXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeRXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		// Processing
		container.add(clusterNodeActiveSims);
		container.add(clusterNodeStatsPending);
		container.add(clusterSimProChart);
		
		// Node OS/JVM
		container.add(clusterNodeUtilChar);
		container.add(clusterNodeMemUsedPerChar);
		container.add(clusterNodeTXChar);
		container.add(clusterNodeRXChar);
		
		// Nodes Tab
		clusterNodesTablePanel = new TablePanel(NodeInfoRowItem.class, 0, true, false);
		
		clusterNodesTablePanel.setColumWidth(0, 50);
		clusterNodesTablePanel.setColumWidth(1, 75);
		// clusterNodesTablePanel.setColumWidth(2, 60);
		clusterNodesTablePanel.setColumWidth(3, 75);
		clusterNodesTablePanel.setColumWidth(4, 75);
		clusterNodesTablePanel.setColumWidth(5, 75);
		clusterNodesTablePanel.setColumWidth(6, 75);
		clusterNodesTablePanel.setColumWidth(7, 75);
		clusterNodesTablePanel.setColumWidth(8, 75);
		clusterNodesTablePanel.setColumWidth(9, 75);
		clusterNodesTablePanel.setColumWidth(stateColumn, 75);
		
		clusterNodesTablePanel.addColumRenderer(
				new NodeControlButtonRenderer(clusterNodesTablePanel, stateColumn, IconManager.getIcon("startSimIcon"), IconManager
						.getIcon("pauseSimIcon"), IconManager.getIcon("stopIcon")), stateColumn);
		
		// this.add(clusterNodesTablePanel, BorderLayout.CENTER);
		// splitPane.setRightComponent(clusterNodesTablePanel);
		nodeStatusTabPanel.addTab(clusterNodesTablePanel, "Information");
		nodeStatusTabPanel.addTab(scrollPane, "Activity");
		
		this.add(nodeStatusTabPanel);
		
		// Register on the event bus
		JComputeEventBus.register(this);
	}
	
	@Subscribe
	public void ControlNodeEvent(NodeAdded e)
	{
		// Assuming Starting State
		clusterNodesTablePanel.addRow(new NodeInfoRowItem(e.getNodeConfiguration(), NodeManagerState.RUNNING.ordinal()));
	}
	
	@Subscribe
	public void NodeManagerStateChange(NodeManagerStateChange e)
	{
		System.out.println("NodeManagerStateChange " + e.getUid() + " state " + e.getState());
		clusterNodesTablePanel.updateCell(e.getUid(), stateColumn, e.getState().ordinal());
	}
	
	@Subscribe
	public void ControlNodeEvent(NodeRemoved e)
	{
		clusterNodesTablePanel.removeRow(e.getNodeConfiguration().getUid());
		
		clusterNodeUtilChar.removeStat("Node " + e.getNodeConfiguration().getUid());
		
		clusterNodeMemUsedPerChar.removeStat("Node " + e.getNodeConfiguration().getUid());
		
		clusterSimProChart.removeStat("Node " + e.getNodeConfiguration().getUid());
		
		clusterNodeActiveSims.removeStat("Node " + e.getNodeConfiguration().getUid());
		
		clusterNodeStatsPending.removeStat("Node " + e.getNodeConfiguration().getUid());
	}
	
	@Subscribe
	public void NodeStatsUpdateEvent(NodeStatsUpdate e)
	{
		clusterSimProChart.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), e.getStats().getSimulationsProcessed(), e.getNodeId());
		
		clusterNodeActiveSims.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), e.getStats().getSimulationsActive(), e.getNodeId());
		
		clusterNodeStatsPending.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), e.getStats().getStatisticsPendingFetch(),
				e.getNodeId());
		
		clusterNodeUtilChar.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), e.getStats().getCpuUsage(), e.getNodeId());
		
		clusterNodeMemUsedPerChar.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), e.getStats().getJvmMemoryUsedPercentage(),
				e.getNodeId());
		
		clusterNodeTXChar.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), (e.getStats().getBytesTX() / MEGABYTE), e.getNodeId());
		
		clusterNodeRXChar.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), (e.getStats().getBytesRX() / MEGABYTE), e.getNodeId());
		
	}
}
