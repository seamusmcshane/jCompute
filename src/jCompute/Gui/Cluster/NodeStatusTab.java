package jCompute.Gui.Cluster;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.Event.NodeAdded;
import jCompute.Cluster.Controller.Event.NodeRemoved;
import jCompute.Cluster.Controller.Event.NodeStatsUpdate;
import jCompute.Gui.Cluster.TableRowItems.NodeInfoRowItem;
import jCompute.Gui.Component.GlobalStatChartPanel;
import jCompute.Gui.Component.TablePanel;

import java.awt.GridLayout;

import javax.swing.JLabel;

public class NodeStatusTab extends JPanel
{
	private GlobalStatChartPanel clusterChart;
	// Left
	private TablePanel clusterNodesTablePanel;

	private int rightPanelsMinWidth;

	public NodeStatusTab(int rightPanelsMinWidth)
	{
		// Min Width of rightPanel
		this.rightPanelsMinWidth = rightPanelsMinWidth;

		this.setLayout(new BorderLayout());

		clusterChart = new GlobalStatChartPanel("Cluster Utilisation", false, 60, true);
		clusterChart.setMaximumSize(new Dimension(1920, 200));
		clusterChart.setPreferredSize(new Dimension(600, 200));

		this.add(clusterChart, BorderLayout.NORTH);

		// Nodes Tab
		clusterNodesTablePanel = new TablePanel(NodeInfoRowItem.class, 0, true, false);

		clusterNodesTablePanel.setColumWidth(0, 50);
		clusterNodesTablePanel.setColumWidth(1, 75);
		// clusterNodesTablePanel.setColumWidth(2, 60);
		clusterNodesTablePanel.setColumWidth(3, 75);

		this.add(clusterNodesTablePanel, BorderLayout.CENTER);

		JPanel statusPanel = new JPanel();
		this.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new GridLayout(1, 0, 0, 0));
		JLabel lblNodes = new JLabel("Nodes");
		statusPanel.add(lblNodes);
		JLabel lblNodeNum = new JLabel("0");
		statusPanel.add(lblNodeNum);
		JLabel lblClusterUsage = new JLabel("Utilisation");
		statusPanel.add(lblClusterUsage);

		// Register on the event bus
		JComputeEventBus.register(this);
	}

	@Subscribe
	public void ControlNodeEvent(NodeAdded e)
	{
		clusterNodesTablePanel.addRow(new NodeInfoRowItem(e.getNodeConfiguration()));
	}

	@Subscribe
	public void ControlNodeEvent(NodeRemoved e)
	{
		clusterNodesTablePanel.removeRow(e.getNodeConfiguration().getUid());

		clusterChart.removeStat("Node " + e.getNodeConfiguration().getUid());
	}

	@Subscribe
	public void NodeStatsUpdateEvent(NodeStatsUpdate e)
	{
		System.out.println("Nid i" + e.getNodeId());
		System.out.println("Nid n" + e.getSequenceNum());
		System.out.println("Nid c" + e.getStats().getCpuUsage());
		System.out.println("Nid m" + e.getStats().getFreeMemory());
		System.out.println("Nid s" + e.getStats().getSimulationsProcessed());

		clusterChart.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), e.getStats().getCpuUsage(), e.getNodeId());
	}

}
