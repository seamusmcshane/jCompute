package jCompute.Gui.Cluster;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.Event.StatusChanged;
import jCompute.Gui.Cluster.TableRowItems.SimpleInfoRowItem;
import jCompute.Gui.Cluster.TableRowItems.SimulationListRowItem;
import jCompute.Gui.Component.Swing.SimpleTabPanel;
import jCompute.Gui.Component.Swing.TablePanel;
import jCompute.Gui.Component.TableCell.EmptyCellColorRenderer;
import jCompute.Gui.Component.TableCell.HeaderRowRenderer;
import jCompute.Gui.Component.TableCell.ProgressBarTableCellRenderer;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEventType;

import com.google.common.eventbus.Subscribe;
import java.awt.GridLayout;

public class ClusterStatusTab extends JPanel
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ClusterStatusTab.class);
	private static final long serialVersionUID = 9077507540119398840L;
	
	// Left
	private JPanel simulationListsContainer;
	private TablePanel activeSimulationsListTable;
	private TablePanel finishedSimulationsListTable;
	
	// Right
	private SimpleTabPanel clusterInfoTabPanel;
	private TablePanel clusterStatusTablePanel;
	
	private int rightPanelsMinWidth;
	
	public ClusterStatusTab(int rightPanelsMinWidth)
	{
		// Min Width of rightPanel
		this.rightPanelsMinWidth = rightPanelsMinWidth;
		
		this.setLayout(new BorderLayout());
		
		simulationListsContainer = new JPanel(new GridLayout(2, 0, 0, 0));
		
		createSimulationsLists();
		
		createClusterInfoTabPanel();
		
		this.add(simulationListsContainer, BorderLayout.CENTER);
		this.add(clusterInfoTabPanel, BorderLayout.EAST);
		
		// Register on the event bus
		JComputeEventBus.register(this);
	}
	
	public void createClusterInfoTabPanel()
	{
		// Tab Panel
		clusterInfoTabPanel = new SimpleTabPanel();
		clusterInfoTabPanel.setMinimumSize(new Dimension(rightPanelsMinWidth, 150));
		clusterInfoTabPanel.setPreferredSize(new Dimension(rightPanelsMinWidth, 150));
		
		// Info Tab
		clusterStatusTablePanel = new TablePanel(SimpleInfoRowItem.class, 0, false, false);
		clusterStatusTablePanel.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());
		clusterStatusTablePanel.addColumRenderer(new HeaderRowRenderer(clusterStatusTablePanel.getJTable()), 0);
		
		clusterStatusTablePanel.setColumWidth(0, 125);
		
		clusterInfoTabPanel.addTab(clusterStatusTablePanel, "Info");
		
		// Populate Fields
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Address", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Port", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Connecting Nodes", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Active Nodes", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Max Active Sims", ""));
		clusterStatusTablePanel.addRow(new SimpleInfoRowItem("Added Sims", ""));
	}
	
	public void createSimulationsLists()
	{
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
			finishedSimulationsListTable
					.addRow(new SimulationListRowItem(e.getSimId(), e.getState(), (int) e.getStepCount(), 100, -1, e.getRunTime()));
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
}
