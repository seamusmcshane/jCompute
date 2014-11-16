package jCompute.Gui.Cluster;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
	private JScrollPane scrollPane;
	private JPanel container;
	private GlobalStatChartPanel clusterNodeUtilChar;
	private GlobalStatChartPanel clusterNodeMemChar;
	private GlobalStatChartPanel clusterSimProChart;

	// Left
	private TablePanel clusterNodesTablePanel;

	private int rightPanelsMinWidth;

	public NodeStatusTab(int rightPanelsMinWidth)
	{
		// Min Width of rightPanel
		this.rightPanelsMinWidth = rightPanelsMinWidth;

		this.setLayout(new BorderLayout());

		container = new JPanel();

		container.setLayout(new GridLayout(3, 1, 0, 0));

		scrollPane = new JScrollPane(container);

		scrollPane.setPreferredSize(new Dimension(600, 240));
		scrollPane.getVerticalScrollBar().setUnitIncrement(15);

		this.add(scrollPane, BorderLayout.NORTH);

		clusterNodeUtilChar = new GlobalStatChartPanel("Node CPU Utilisation", "Nodes", true, false, 60, true);
		clusterNodeUtilChar.setMaximumSize(new Dimension(1920, 200));
		clusterNodeUtilChar.setPreferredSize(new Dimension(600, 200));

		container.add(clusterNodeUtilChar);

		clusterNodeMemChar = new GlobalStatChartPanel("Node Mem Utilisation", "Nodes", true, false, 60, true);
		clusterNodeMemChar.setMaximumSize(new Dimension(1920, 200));
		clusterNodeMemChar.setPreferredSize(new Dimension(600, 200));

		container.add(clusterNodeMemChar);

		clusterSimProChart = new GlobalStatChartPanel("Simulations Processed", "Nodes", true, false, 60, true);
		clusterSimProChart.setMaximumSize(new Dimension(1920, 200));
		clusterSimProChart.setPreferredSize(new Dimension(600, 200));

		container.add(clusterSimProChart);

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

		clusterNodeUtilChar.removeStat("Node " + e.getNodeConfiguration().getUid());

		clusterNodeMemChar.removeStat("Node " + e.getNodeConfiguration().getUid());

		clusterSimProChart.removeStat("Node " + e.getNodeConfiguration().getUid());
	}

	@Subscribe
	public void NodeStatsUpdateEvent(NodeStatsUpdate e)
	{
		clusterNodeUtilChar.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), e.getStats().getCpuUsage(),
				e.getNodeId());
		clusterNodeMemChar.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), e.getStats().getFreeMemory(),
				e.getNodeId());
		clusterSimProChart.statUpdate("Node " + e.getNodeId(), e.getSequenceNum(), e.getStats()
				.getSimulationsProcessed(), e.getNodeId());
	}

}
