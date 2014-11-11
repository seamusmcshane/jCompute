package jCompute.Gui.Batch;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.Event.NodeAdded;
import jCompute.Cluster.Controller.Event.NodeRemoved;
import jCompute.Cluster.Controller.Event.NodeUpdated;
import jCompute.Cluster.Controller.Event.StatusChanged;
import jCompute.Gui.Batch.TableRowItems.ActiveSimulationRowItem;
import jCompute.Gui.Batch.TableRowItems.NodeInfoRowItem;
import jCompute.Gui.Batch.TableRowItems.SimpleInfoRowItem;
import jCompute.Gui.Component.SimpleTabPanel;
import jCompute.Gui.Component.TablePanel;
import jCompute.Gui.Component.TableCell.EmptyCellColorRenderer;
import jCompute.Gui.Component.TableCell.HeaderRowRenderer;
import jCompute.Gui.Component.TableCell.ProgressBarTableCellRenderer;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEventType;

import com.google.common.eventbus.Subscribe;

public class ClusterStatusTab extends JPanel
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ClusterStatusTab.class);
	private static final long serialVersionUID = 9077507540119398840L;

	// Left
	private TablePanel activeSimulationsListTable;

	// Right
	private SimpleTabPanel clusterInfoTabPanel;
	private TablePanel clusterStatusTablePanel;
	private TablePanel clusterNodesTablePanel;

	public ClusterStatusTab()
	{
		this.setLayout(new BorderLayout());

		createActiveSimulationsListTable();

		this.add(activeSimulationsListTable, BorderLayout.CENTER);

		createClusterInfoTabPanel();

		this.add(clusterInfoTabPanel, BorderLayout.EAST);

		// Register on the event bus
		JComputeEventBus.register(this);
	}

	public void createClusterInfoTabPanel()
	{
		// Tab Panel
		clusterInfoTabPanel = new SimpleTabPanel();
		clusterInfoTabPanel.setMinimumSize(new Dimension(300, 150));
		clusterInfoTabPanel.setPreferredSize(new Dimension(300, 150));

		// Info Tab
		clusterStatusTablePanel = new TablePanel(SimpleInfoRowItem.class, 0, false, false);
		clusterStatusTablePanel.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());
		clusterStatusTablePanel.addColumRenderer(new HeaderRowRenderer(clusterStatusTablePanel.getJTable()), 0);

		clusterStatusTablePanel.setColumWidth(0, 125);

		// Nodes Tab
		clusterNodesTablePanel = new TablePanel(NodeInfoRowItem.class, 0, true, false);

		clusterNodesTablePanel.setColumWidth(0, 50);
		clusterNodesTablePanel.setColumWidth(1, 75);
		// clusterNodesTablePanel.setColumWidth(2, 60);
		clusterNodesTablePanel.setColumWidth(3, 75);
		clusterNodesTablePanel.setColumWidth(4, 75);

		clusterInfoTabPanel.addTab(clusterStatusTablePanel, "Info");
		clusterInfoTabPanel.addTab(clusterNodesTablePanel, "Nodes");
		
		// Populate Fields
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Address",""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Port",""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("",""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Connecting Nodes",""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Active Nodes",""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("",""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Max Active Sims",""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Added Sims",""));		
	}

	public void createActiveSimulationsListTable()
	{
		activeSimulationsListTable = new TablePanel(ActiveSimulationRowItem.class, 0, "Cluster Activity", true, false);

		activeSimulationsListTable.setColumWidth(0, 80);

		activeSimulationsListTable.setColumWidth(1, 70);
		activeSimulationsListTable.setColumWidth(2, 80);
		// activeSimulationsListTable.setColumWidth(3, 65);
		activeSimulationsListTable.setColumWidth(4, 80);
		activeSimulationsListTable.setColumWidth(5, 110);
		// Progress Column uses a progress bar for display
		activeSimulationsListTable.addColumRenderer(new ProgressBarTableCellRenderer(), 3);
		activeSimulationsListTable.setMinimumSize(new Dimension(800, 200));
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
		SimulationsManagerEventType type = e.getEventType();
		int simId = e.getSimId();

		if(type == SimulationsManagerEventType.AddedSim)
		{
			log.debug("Add Row for " + "Simulation " + simId);

			// Add the row
			activeSimulationsListTable.addRow(new ActiveSimulationRowItem(simId));
		}
		else if(type == SimulationsManagerEventType.RemovedSim)
		{
			log.debug("Removing Row for " + "Simulation " + simId);

			// Remove the Row
			activeSimulationsListTable.removeRow(simId);
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
		activeSimulationsListTable.updateCells(e.getSimId(), new int[]
		{
				1
		}, new Object[]
		{
				e.getState()
		});
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
	}
	
	@Subscribe
	public void ControlNodeEvent(NodeUpdated e)
	{
		clusterNodesTablePanel.updateRow(e.getNodeConfiguration().getUid(),new NodeInfoRowItem(e.getNodeConfiguration()));
	}
	
	@Subscribe
	public void ControlNodeEvent(StatusChanged e)
	{
		clusterStatusTablePanel.updateRow("Address", new SimpleInfoRowItem("Address",e.getAddress()));
		clusterStatusTablePanel.updateRow("Port", new SimpleInfoRowItem("Port",e.getPort()));
		clusterStatusTablePanel.updateRow("Connecting Nodes", new SimpleInfoRowItem("Connecting Nodes",e.getConnectingNodes()));
		clusterStatusTablePanel.updateRow("Active Nodes", new SimpleInfoRowItem("Active Nodes",e.getActiveNodes()));
		clusterStatusTablePanel.updateRow("Max Active Sims", new SimpleInfoRowItem("Max Active Sims",e.getMaxActiveSims()));
		clusterStatusTablePanel.updateRow("Added Sims", new SimpleInfoRowItem("Added Sims",e.getAddedSims()));
	}
	
	
}
