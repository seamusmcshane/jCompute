package jCompute.Gui.Component.Swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellRenderer;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import jCompute.Gui.Component.RowItem;
import jCompute.Gui.Component.TableCell.ColorConstants;

public class TablePanel<IndexType, RowType extends RowItem<RowType, IndexType>> extends JPanel
{
	private static final long serialVersionUID = 7193787210494563482L;
	private JTable table;

	private JScrollPane scrollPane;

	private BasicEventList<RowType> baseList;
	private SortedList<RowType> sortedList;

	private EventList<RowType> activeList;

	private Class<RowType> rowClass;

	public TablePanel(Class<RowType> rowClass, boolean sortable, boolean rowSelection, boolean hScroll)
	{
		super();

		this.rowClass = rowClass;

		setLayout(new BorderLayout(0, 0));

		setBorder(null);

		setUpTable(sortable, rowSelection, hScroll);
	}

	public TablePanel(Class<RowType> rowClass, boolean sortable, boolean rowSelection)
	{
		this(rowClass, sortable, rowSelection, false);
	}

	/**
	 * @wbp.parser.constructor
	 */
	public TablePanel(Class<RowType> rowClass, String title, boolean sortable, boolean rowSelection)
	{
		this(rowClass, sortable, rowSelection);

		// Table Title Label
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		titlePanel.setBackground(ColorConstants.LightBlue);
		JLabel lblTitle = new JLabel(title);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

		titlePanel.add(lblTitle, BorderLayout.CENTER);

		this.add(titlePanel, BorderLayout.NORTH);
	}

	private void setUpTable(boolean sortable, boolean rowSelection, boolean hScroll)
	{
		RowType rowItem;

		try
		{
			// Cast the row class to a a RowItem and use the RowItem api for
			// getting the field and their names
			rowItem = rowClass.newInstance();

			// Create a new table format via reflection of the rowClass
			TableFormat<RowType> tf = GlazedLists.tableFormat(rowClass, rowItem.getFieldList(), rowItem.getFieldNames(), rowItem.getEditableCells());

			baseList = new BasicEventList<RowType>();

			if(sortable)
			{
				sortedList = new SortedList<RowType>(baseList);
				table = new JTable(GlazedListsSwing.eventTableModelWithThreadProxyList(sortedList, tf));
				TableComparatorChooser.install(table, sortedList, AbstractTableComparatorChooser.SINGLE_COLUMN);
				activeList = sortedList;
			}
			else
			{
				table = new JTable(GlazedListsSwing.eventTableModelWithThreadProxyList(baseList, tf));
				activeList = baseList;
			}

			scrollPane = new JScrollPane(table);

			if(hScroll == true)
			{
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			}
			else
			{
				table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			}
			scrollPane.setWheelScrollingEnabled(true);

			table.setBackground(Color.white);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setCellSelectionEnabled(false);
			table.setColumnSelectionAllowed(false);
			table.setRowSelectionAllowed(rowSelection);
			table.setRowHeight(20);

			table.setShowGrid(true);
			table.setGridColor(Color.LIGHT_GRAY);

			this.add(scrollPane, BorderLayout.CENTER);

		}
		catch(InstantiationException | IllegalAccessException e)
		{
			System.out.println("Error in Row Item Reflection : " + rowClass.toString());
			e.printStackTrace();
		}

	}

	/**
	 * Sets a TableCellRenderer to be used for a column
	 *
	 * @param renderer
	 * @param column
	 */
	public void addColumRenderer(TableCellRenderer renderer, int column)
	{
		table.getColumnModel().getColumn(column).setCellRenderer(renderer);
	}

	public void setDefaultRenderer(Class<?> classType, TableCellRenderer defaultRenderer)
	{
		table.setDefaultRenderer(classType, defaultRenderer);
	}

	public TableCellRenderer getTableHeaderRenderer()
	{
		return table.getTableHeader().getDefaultRenderer();
	}

	/**
	 * Appends a new row to the end of the table
	 */
	public void addRow(RowType row)
	{
		activeList.getReadWriteLock().writeLock().lock();

		activeList.add(row);

		activeList.getReadWriteLock().writeLock().unlock();
	}

	/**
	 * Updates a row
	 *
	 * @param rowKey
	 */
	public void updateRow(final IndexType rowKey, final RowType row)
	{
		activeList.getReadWriteLock().writeLock().lock();

		int index = findRow(rowKey);

		if(index != -1)
		{
			activeList.set(index, row);
		}

		activeList.getReadWriteLock().writeLock().unlock();
	}

	private int findRow(IndexType rowKey)
	{
		int index = -1;
		RowType row;

		for(int i = 0; i < activeList.size(); i++)
		{
			row = activeList.get(i);

			if(row.keyEquals(rowKey))
			{
				index = i;
				break;
			}
		}

		return index;
	}

	/**
	 * Removes a row
	 *
	 * @param rowKey
	 */
	public void removeRow(final IndexType rowKey)
	{
		activeList.getReadWriteLock().writeLock().lock();

		int index = findRow(rowKey);

		if(index != -1)
		{
			activeList.remove(index);
		}

		activeList.getReadWriteLock().writeLock().unlock();
	}

	public void removeFirstRow()
	{
		activeList.getReadWriteLock().writeLock().lock();

		if(activeList.size() > 0)
		{
			activeList.remove(0);
		}

		activeList.getReadWriteLock().writeLock().unlock();
	}

	public int getRows()
	{
		activeList.getReadWriteLock().writeLock().lock();

		int size = activeList.size();

		activeList.getReadWriteLock().writeLock().unlock();

		return size;
	}

	/**
	 * Clears the data in the Table
	 */
	public void clearTable()
	{
		activeList.getReadWriteLock().writeLock().lock();

		activeList.clear();

		activeList.getReadWriteLock().writeLock().unlock();

	}

	public void addMouseListener(MouseAdapter adaptor)
	{
		table.addMouseListener(adaptor);
	}

	/**
	 * Gets a value at a specific coordinate in the table
	 *
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Object value;

		activeList.getReadWriteLock().writeLock().lock();

		RowType row = activeList.get(rowIndex);

		value = row.getFieldValue(columnIndex);

		activeList.getReadWriteLock().writeLock().unlock();

		return value;
	}

	/**
	 * Returns the number of rows in the table
	 *
	 * @return
	 */
	public int getRowsCount()
	{
		int size = 0;

		activeList.getReadWriteLock().writeLock().lock();

		size = activeList.size();

		activeList.getReadWriteLock().writeLock().unlock();

		return size;
	}

	public void clearSelection()
	{
		table.clearSelection();
	}

	public void updateCell(final IndexType rowKey, final int column, final Object columnValue)
	{
		activeList.getReadWriteLock().writeLock().lock();

		int index = findRow(rowKey);

		if(index != -1)
		{
			RowType row = activeList.get(index);

			row.setFieldValue(column, columnValue);

			activeList.set(index, row);
		}

		activeList.getReadWriteLock().writeLock().unlock();
	}

	public void updateCells(final IndexType rowKey, final int columns[], final Object columnValues[])
	{
		activeList.getReadWriteLock().writeLock().lock();

		int index = findRow(rowKey);

		if(index != -1)
		{
			RowType row = activeList.get(index);

			for(int c = 0; c < columns.length; c++)
			{
				row.setFieldValue(columns[c], columnValues[c]);
			}

			activeList.set(index, row);
		}

		activeList.getReadWriteLock().writeLock().unlock();
	}

	public void setColumWidth(int column, int pref)
	{
		table.getColumnModel().getColumn(column).setPreferredWidth(pref);
		table.getColumnModel().getColumn(column).setMinWidth(pref);
		table.getColumnModel().getColumn(column).setMaxWidth(pref);
	}

	public JTable getJTable()
	{
		return table;
	}

	public void setSelection(int row, int column)
	{
		table.changeSelection(row, column, false, false);
	}

	public void scrollToBottom()
	{
		activeList.getReadWriteLock().writeLock().lock();

		int size = activeList.size();

		table.scrollRectToVisible(table.getCellRect(size - 1, 0, true));

		activeList.getReadWriteLock().writeLock().unlock();

	}
}