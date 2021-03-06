package jcompute.gui.interactive.tab;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JTable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import jcompute.JComputeEventBus;
import jcompute.gui.cluster.tablerowitems.SimulationListRowItem;
import jcompute.gui.component.swing.jpanel.TablePanel;
import jcompute.gui.component.tablecell.ProgressBarTableCellRenderer;
import jcompute.gui.interactive.GUITabManager;
import jcompute.simulation.SimulationState.SimState;
import jcompute.simulation.event.SimulationStatChangedEvent;
import jcompute.simulation.event.SimulationStateChangedEvent;
import jcompute.simulationmanager.event.SimulationsManagerEvent;
import jcompute.simulationmanager.event.SimulationsManagerEventType;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SimulationListTabPanel extends JPanel
{
	private static final long serialVersionUID = 76641721672552215L;
	
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(SimulationListTabPanel.class);

	// The table object
	private TablePanel<Integer, SimulationListRowItem> table;

	// Name for the Table / This Object
	private String name = "Simulations List";

	// References to the needed objects
	private GUITabManager tabManager;

	private int selectedRowIndex = -1;

	public SimulationListTabPanel(GUITabManager tabManager)
	{
		super();

		this.tabManager = tabManager;

		setLayout(new BorderLayout(0, 0));

		setMinimumSize(new Dimension(400, 600));

		setUpTable();

		// Register on the event bus
		JComputeEventBus.register(this);

	}

	/*
	 * SetsUp the Table with the correct
	 */
	private void setUpTable()
	{
		table = new TablePanel<Integer, SimulationListRowItem>(SimulationListRowItem.class, "Active Simulations", true, true);

		table.setColumWidth(0, 65);
		table.setColumWidth(1, 50);
		table.setColumWidth(2, 50);
		table.setColumWidth(3, 65);
		table.setColumWidth(4, 50);
		// table.setColumWidth(5,25);

		// Progress Column uses a progress bar for display
		table.addColumRenderer(new ProgressBarTableCellRenderer(table.getJTable()), 3);
		this.add(table);

		registerTableMouseListener();

	}

	/*
	 * Sets up the handler for the mouse clicks on the Table
	 */
	private void registerTableMouseListener()
	{
		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == 1)
				{
					JTable table = (JTable) e.getSource();
					Point p = e.getPoint();

					selectedRowIndex = table.rowAtPoint(p);

					if(e.getClickCount() == 2)
					{
						// Get the String "Simulation (int)" and remove "Simulation "
						int simId = (int) table.getValueAt(selectedRowIndex, 0);

						tabManager.displayTab(simId);

					}
				}
				else
				{
					selectedRowIndex = -1;

					table.clearSelection();
				}
			}
		});
	}

	public String getTabName()
	{
		return name;
	}

	/**
	 * SimulationsManagerEvent handler method
	 * @param e
	 */
	@Subscribe
	public void SimulationsManagerEvent(SimulationsManagerEvent e)
	{
		final SimulationsManagerEventType type = e.getEventType();
		final int simId = e.getSimId();

		if(type == SimulationsManagerEventType.AddedSim)
		{
			// Add a row
			table.addRow(new SimulationListRowItem(simId));

		}
		else if(type == SimulationsManagerEventType.RemovedSim)
		{
			// Remove the Row
			table.removeRow(simId);
		}
		else
		{
			log.error("Unhandled SimulationManagerEvent in Simulations List");
		}
	}

	@Subscribe
	public void SimulationStatChanged(SimulationStatChangedEvent e)
	{
		table.updateCells(e.getSimId(), new int[]
		{
			2, 3, 4, 5
		}, new Object[]
		{
			e.getStepNo(), e.getProgress(), e.getAsps(), e.getTime()
		});

	}

	@Subscribe
	public void SimulationStateChangedEvent(SimulationStateChangedEvent e)
	{
		table.updateCell(e.getSimId(), 1, e.getState());
		
		if(e.getState() == SimState.FINISHED)
		{
			// So the GUI shows the correct values at the end of processing
			table.updateCells(e.getSimId(), new int[]
			{
				2, 3, 5
			}, new Object[]
			{
				(int)e.getStepCount(), 100, e.getRunTime()
			});
		}
	}

}
