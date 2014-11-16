package jCompute.Gui.Cluster;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import jCompute.IconManager;
import jCompute.JComputeEventBus;
import jCompute.Batch.Batch;
import jCompute.Batch.Batch.BatchPriority;
import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Batch.BatchManager.Event.BatchAddedEvent;
import jCompute.Batch.BatchManager.Event.BatchFinishedEvent;
import jCompute.Batch.BatchManager.Event.BatchPositionEvent;
import jCompute.Batch.BatchManager.Event.BatchProgressEvent;
import jCompute.Gui.Cluster.TableRowItems.BatchCompletedRowItem;
import jCompute.Gui.Cluster.TableRowItems.BatchQueueRowItem;
import jCompute.Gui.Cluster.TableRowItems.SimpleInfoRowItem;
import jCompute.Gui.Component.TablePanel;
import jCompute.Gui.Component.TableCell.BooleanIconRenderer;
import jCompute.Gui.Component.TableCell.EmptyCellColorRenderer;
import jCompute.Gui.Component.TableCell.HeaderRowRenderer;
import jCompute.Gui.Component.TableCell.PriorityIconRenderer;
import jCompute.Gui.Component.TableCell.ProgressBarTableCellRenderer;

import javax.swing.JPanel;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class BatchTab extends JPanel
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(BatchTab.class);
	private static final long serialVersionUID = 662715907114338723L;

	// Left Split
	private JPanel batchQueuedAndCompletePanel;
	private TablePanel batchQueuedTable;
	private TablePanel batchCompletedTable;

	// BatchInfo selection
	private int queuedSelectedBatchRowIndex = -1;
	private int completedSelectedBatchRowIndex = -1;
	// 0 = none,1 queued,2 completed
	private int queuedOrCompleted = 0;

	// Right Split
	private TablePanel batchInfo;
	private int rightPanelsMinWidth;

	// Queue Table Positions
	private int positionColumn = 0;
	private int idColumn = 1;
	private int nameColumn = 2;
	private int priColumn = 3;
	private int statusColumn = 4;
	private int progressColumn = 5;
	private int estimatedFinishColumn = 6;
	private int batchQueueIndexColumn = idColumn;

	private int numericColumnWidth = 60;
	private int iconColumnWidth = 50;
	private int dateColumnWidth = 220;
	private int timeColumnWidth = 100;
	private int progressColumnWidth = 80;

	private BatchManager batchManager;

	public BatchTab(BatchManager batchManager, int rightPanelsMinWidth)
	{
		// Batch Manager
		this.batchManager = batchManager;

		// Min Width of rightPanel
		this.rightPanelsMinWidth = rightPanelsMinWidth;

		// Panel Layout
		this.setLayout(new BorderLayout());

		createBatchQueuedAndCompletePanel();

		this.add(batchQueuedAndCompletePanel, BorderLayout.CENTER);

		createBatchInfoPanel();

		this.add(batchInfo, BorderLayout.EAST);

		// Register on the event bus
		JComputeEventBus.register(this);

		registerTableMouseListeners();
	}

	private void createBatchInfoPanel()
	{
		batchInfo = new TablePanel(SimpleInfoRowItem.class, 0, "Batch Info", false, false);
		batchInfo.setColumWidth(0, 125);
		batchInfo.setMinimumSize(new Dimension(rightPanelsMinWidth, 150));
		batchInfo.setPreferredSize(new Dimension(rightPanelsMinWidth, 150));
		batchInfo.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());

		batchInfo.addColumRenderer(new HeaderRowRenderer(batchInfo.getJTable()), 0);
		batchInfo.setVisible(false);
	}

	private void createBatchQueuedAndCompletePanel()
	{
		// Left Tables (Queue and Completed)
		batchQueuedAndCompletePanel = new JPanel();

		// Top Queue Batches Table
		GridBagLayout gbl_batchQueuedCompletePanel = new GridBagLayout();
		gbl_batchQueuedCompletePanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_batchQueuedCompletePanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_batchQueuedCompletePanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_batchQueuedCompletePanel.rowWeights = new double[]
		{
				1.0, 1.0
		};
		batchQueuedAndCompletePanel.setLayout(gbl_batchQueuedCompletePanel);

		batchQueuedTable = new TablePanel(BatchQueueRowItem.class, batchQueueIndexColumn, "Queued", true, true);

		// Batch Priority
		batchQueuedTable.addColumRenderer(
				new PriorityIconRenderer(IconManager.getIcon("highPriorityIcon"), IconManager
						.getIcon("standardPriorityIcon")), priColumn);

		// Batch State
		batchQueuedTable.addColumRenderer(
				new BooleanIconRenderer(IconManager.getIcon("startSimIcon"), IconManager.getIcon("pausedSimIcon")),
				statusColumn);

		// Progress Column uses a progress bar for display
		batchQueuedTable.addColumRenderer(new ProgressBarTableCellRenderer(), progressColumn);

		GridBagConstraints gbc_batchQueuedTable = new GridBagConstraints();
		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;

		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;

		batchQueuedTable.setColumWidth(positionColumn, numericColumnWidth);
		batchQueuedTable.setColumWidth(idColumn, numericColumnWidth);

		// batchQueuedTable.setColumWidth(nameColumn, 175);
		batchQueuedTable.setColumWidth(priColumn, iconColumnWidth);
		batchQueuedTable.setColumWidth(statusColumn, iconColumnWidth);
		batchQueuedTable.setColumWidth(progressColumn, progressColumnWidth);
		batchQueuedTable.setColumWidth(estimatedFinishColumn, dateColumnWidth);
		// batchQueuedTable.setColumWidth(6, 40);
		// batchQueuedTable.setColumWidth(7, 40);
		// batchQueuedTable.setColumWidth(8, 60);

		batchQueuedAndCompletePanel.add(batchQueuedTable, gbc_batchQueuedTable);

		// Bottom Completed Batches
		batchCompletedTable = new TablePanel(BatchCompletedRowItem.class, 0, "Completed", true, true);

		GridBagConstraints gbc_batchCompleteTable = new GridBagConstraints();
		gbc_batchCompleteTable.gridx = 0;
		gbc_batchCompleteTable.gridy = 1;
		gbc_batchCompleteTable.fill = GridBagConstraints.BOTH;

		batchCompletedTable.setColumWidth(0, numericColumnWidth);
		batchCompletedTable.setColumWidth(2, timeColumnWidth);
		batchCompletedTable.setColumWidth(3, dateColumnWidth);
		// batchCompletedTable.setColumWidth(3, 50);

		batchQueuedAndCompletePanel.add(batchCompletedTable, gbc_batchCompleteTable);
	}

	private void clearQueuedSelection()
	{
		queuedSelectedBatchRowIndex = -1;

		batchQueuedTable.clearSelection();
	}

	private void clearCompletedSelection()
	{
		completedSelectedBatchRowIndex = -1;

		batchCompletedTable.clearSelection();
	}

	private void registerTableMouseListeners()
	{
		batchQueuedTable.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == 1)
				{
					JTable table = (JTable) e.getSource();
					Point p = e.getPoint();

					queuedOrCompleted = 1;

					if(queuedSelectedBatchRowIndex == table.rowAtPoint(p))
					{
						queuedOrCompleted = 0;

						clearQueuedSelection();
					}
					else
					{
						queuedSelectedBatchRowIndex = table.rowAtPoint(p);
					}

					// Clear any selection in the completed table
					clearCompletedSelection();
				}

				updateBatchInfo(queuedOrCompleted);

			}
		});

		batchCompletedTable.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == 1)
				{
					JTable table = (JTable) e.getSource();
					Point p = e.getPoint();

					queuedOrCompleted = 2;

					if(completedSelectedBatchRowIndex == table.rowAtPoint(p))
					{
						queuedOrCompleted = 0;

						clearCompletedSelection();
					}
					else
					{
						completedSelectedBatchRowIndex = table.rowAtPoint(p);

					}

					// Clear any selection in the queued table
					clearQueuedSelection();
				}

				updateBatchInfo(queuedOrCompleted);

			}
		});
	}

	private void updateBatchInfo(int srcTable)
	{
		int batchId = 0;

		// Should the data fetch be skipped
		boolean skipData = false;

		// find the table and batch the row relates to.
		if(srcTable > 0)
		{
			if(srcTable == 1)
			{
				if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
				{
					queuedSelectedBatchRowIndex = 0;
					batchQueuedTable.clearSelection();
					srcTable = 0;

					// invalid row selected
					skipData = true;
				}
				else
				{
					batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
				}
			}
			else
			{

				if(completedSelectedBatchRowIndex < 0 || batchCompletedTable.getRowsCount() == 0)
				{
					completedSelectedBatchRowIndex = 0;
					batchCompletedTable.clearSelection();
					srcTable = 0;

					// invalid row selected
					skipData = true;
				}
				else
				{
					batchId = (int) batchCompletedTable.getValueAt(completedSelectedBatchRowIndex, 0);
				}
			}
		}
		else
		{
			// No table selected
			skipData = true;
		}

		if(!skipData)
		{
			String info[] = batchManager.getBatchInfo(batchId);

			// Batch Info
			int batchInfoLength = info.length;

			if(batchInfo.getRowsCount() <= 0)
			{
				for(int i = 0; i < batchInfoLength; i += 2)
				{
					batchInfo.addRow(new SimpleInfoRowItem(info[i], info[i + 1]));
				}
			}
			else
			{
				for(int i = 0; i < batchInfoLength; i += 2)
				{
					batchInfo.updateRow(info[i], new SimpleInfoRowItem(info[i], info[i + 1]));

				}
			}

		}
		else
		{
			// Clear Batch info tables
			batchInfo.clearTable();
		}

		// Display the info pane based on if we have data to put in it.
		batchInfo.setVisible(!skipData);

	}

	public void startBatch()
	{
		if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
		{
			queuedSelectedBatchRowIndex = 0;

			// invalid row selected
			return;
		}

		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

		batchManager.setStatus(batchId, true);
	}

	public void removeBatch()
	{
		if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
		{
			queuedSelectedBatchRowIndex = 0;

			// invalid row selected
			return;
		}

		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

		clearQueuedSelection();

		batchQueuedTable.removeRow(batchId);
		batchManager.removeBatch(batchId);
	}

	public void pauseBatch()
	{
		if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
		{
			// invalid row selected
			return;
		}

		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

		batchManager.setStatus(batchId, false);
	}

	public void moveLast()
	{
		if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0
				|| queuedSelectedBatchRowIndex == batchQueuedTable.getRowsCount() - 1)
		{
			// invalid row selected
			return;
		}

		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
				+ " moveToEnd...");

		batchManager.moveToEnd(batchId);

		queuedSelectedBatchRowIndex = batchQueuedTable.getRowsCount() - 1;
		batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);

		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " MOVED...");
	}

	public void moveBackward()
	{
		if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0
				|| queuedSelectedBatchRowIndex == batchQueuedTable.getRowsCount() - 1)
		{
			// invalid row selected
			return;
		}

		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
				+ " moveToBack...");

		batchManager.moveBackward(batchId);

		queuedSelectedBatchRowIndex = queuedSelectedBatchRowIndex + 1;
		batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);

		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " MOVED...");
	}

	public void moveForward()
	{
		if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
		{
			// invalid row selected
			return;
		}

		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
				+ " moveToFront...");

		batchManager.moveForward(batchId);

		queuedSelectedBatchRowIndex = queuedSelectedBatchRowIndex - 1;
		batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);
	}

	public void moveFirst()
	{
		if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
		{
			queuedSelectedBatchRowIndex = 0;

			// invalid row selected
			return;
		}

		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId
				+ " moveToFront...");

		batchManager.moveToFront(batchId);

		queuedSelectedBatchRowIndex = 0;
		batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);

		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " MOVED...");
	}

	public void setBatchStandardPri()
	{
		if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
		{
			queuedSelectedBatchRowIndex = 0;

			// invalid row selected
			return;
		}

		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

		batchManager.setPriority(batchId, BatchPriority.STANDARD);
	}

	public void setBatchHighPri()
	{
		if(queuedSelectedBatchRowIndex < 0 || batchQueuedTable.getRowsCount() == 0)
		{
			queuedSelectedBatchRowIndex = 0;

			// invalid row selected
			return;
		}

		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);

		batchManager.setPriority(batchId, BatchPriority.HIGH);
	}

	@Subscribe
	public void batchQueuePositionChanged(BatchPositionEvent event)
	{
		Batch batch = event.getBatch();
		batchQueuedTable.updateRow(batch.getBatchId(), new BatchQueueRowItem(batch));

		log.debug("batchQueuePositionChanged " + batch.getBatchId() + " Pos" + batch.getPosition());
	}

	@Subscribe
	public void batchProgress(BatchProgressEvent event)
	{
		Batch batch = event.getBatch();

		batchQueuedTable.updateRow(batch.getBatchId(), new BatchQueueRowItem(batch));

		updateBatchInfo(queuedOrCompleted);
	}

	@Subscribe
	public void batchAdded(BatchAddedEvent event)
	{
		// add new row
		batchQueuedTable.addRow(new BatchQueueRowItem(event.getBatch()));
	}

	@Subscribe
	public void batchFinished(BatchFinishedEvent event)
	{
		Batch batch = event.getBatch();

		log.info("Batch Finished " + batch.getBatchId());

		// remove row
		batchQueuedTable.removeRow(batch.getBatchId());

		batchCompletedTable.addRow(new BatchCompletedRowItem(batch.getBatchId(), batch.getFileName(),
				jCompute.util.Text.longTimeToDHMS(batch.getRunTime()), batch.getFinished()));

		queuedSelectedBatchRowIndex = -1;

		if(queuedOrCompleted == 1)
		{
			queuedOrCompleted = 0;
		}

		batchQueuedTable.clearSelection();
	}
}