package jCompute.Gui.Cluster;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
import jCompute.Gui.Component.Swing.GlobalStatChartPanel;
import jCompute.Gui.Component.Swing.SimpleTabPanel;
import jCompute.Gui.Component.Swing.SimpleTabTabTitle;
import jCompute.Gui.Component.Swing.TablePanel;
import jCompute.Gui.Component.TableCell.ColorLabelRenderer;
import jCompute.Gui.Component.TableCell.EmptyCellColorRenderer;
import jCompute.Gui.Component.TableCell.HeaderRowRenderer;
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
		clusterStatusTablePanel.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterStatusTablePanel.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		
		// Populate Fields
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Address", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Port", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Connecting Nodes", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Active Nodes", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Max Active Sims", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Added Sims", ""));
		
		graphsJPanelContainer = new JPanel();
		GridBagLayout gbl_graphsJPanelContainer = new GridBagLayout();
		gbl_graphsJPanelContainer.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
		gbl_graphsJPanelContainer.columnWeights = new double[]{1.0};
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
		
		clusterNodeTXChar = new GlobalStatChartPanel("Network TX", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeTXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeTXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		clusterNodeRXChar = new GlobalStatChartPanel("Network RX", "Nodes", true, false, 60, true, legendMaxNodes);
		clusterNodeRXChar.setMaximumSize(new Dimension(1920, CHART_HEIGHT));
		clusterNodeRXChar.setPreferredSize(new Dimension(600, CHART_HEIGHT));
		
		
		// Cluster Info
		GridBagConstraints gbConstraints0 = new GridBagConstraints();
		gbConstraints0.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints0.gridx = 0;
		gbConstraints0.gridy = 0;
		graphsJPanelContainer.add(clusterStatusTablePanel,gbConstraints0);
		
		// Processing
		GridBagConstraints gbConstraints1 = new GridBagConstraints();
		gbConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints1.gridx = 0;
		gbConstraints1.gridy = 1;
		graphsJPanelContainer.add(clusterNodeActiveSims,gbConstraints1);

		GridBagConstraints gbConstraints2 = new GridBagConstraints();
		gbConstraints2.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints2.gridx = 0;
		gbConstraints2.gridy = 2;
		graphsJPanelContainer.add(clusterNodeStatsPending,gbConstraints2);

		GridBagConstraints gbConstraints3 = new GridBagConstraints();
		gbConstraints3.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints3.gridx = 0;
		gbConstraints3.gridy = 3;
		graphsJPanelContainer.add(clusterSimProChart,gbConstraints3);
		
		// Node OS/JVM
		GridBagConstraints gbConstraints4 = new GridBagConstraints();
		gbConstraints4.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints4.gridx = 0;
		gbConstraints4.gridy = 4;
		graphsJPanelContainer.add(clusterNodeUtilChar,gbConstraints4);
		
		GridBagConstraints gbConstraints5 = new GridBagConstraints();
		gbConstraints5.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints5.gridx = 0;
		gbConstraints5.gridy = 5;
		graphsJPanelContainer.add(clusterNodeMemUsedPerChar,gbConstraints5);

		GridBagConstraints gbConstraints6 = new GridBagConstraints();
		gbConstraints6.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints6.gridx = 0;
		gbConstraints6.gridy = 6;
		graphsJPanelContainer.add(clusterNodeTXChar,gbConstraints6);

		GridBagConstraints gbConstraints7 = new GridBagConstraints();
		gbConstraints7.fill = GridBagConstraints.HORIZONTAL;
		gbConstraints7.gridx = 0;
		gbConstraints7.gridy = 7;
		graphsJPanelContainer.add(clusterNodeRXChar,gbConstraints7);
		
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
				clusterNodeTXChar.addStat(nodeId, nid);
				clusterNodeRXChar.addStat(nodeId, nid);
				
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
				clusterNodeTXChar.removeStat(nodeId);
				clusterNodeRXChar.removeStat(nodeId);
				
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
		clusterNodeTXChar.statUpdate(nodeId, e.getSequenceNum(), (e.getStats().getBytesTX() / MEGABYTE));
		clusterNodeRXChar.statUpdate(nodeId, e.getSequenceNum(), (e.getStats().getBytesRX() / MEGABYTE));
	}
}
