package jCompute.Gui.Component.Swing;

import jCompute.Gui.Component.RowItem;
import jCompute.Gui.Component.TableCell.ColorConstants;

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
import javax.swing.table.TableCellRenderer;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import javax.swing.border.EtchedBorder;

public class TablePanel extends JPanel
{
	private static final long serialVersionUID = 7193787210494563482L;
	private JTable table;
	
	private JScrollPane scrollPane;
	private int indexColumn;
	
	private BasicEventList baseList;
	private SortedList sortedList;
	
	private EventList activeList;
	
	private Class rowClass;
	
	public TablePanel(Class rowClass, int indexColumn, boolean sortable, boolean rowSelection, boolean hScroll)
	{
		super();
		
		this.rowClass = rowClass;
		
		this.indexColumn = indexColumn;
		
		setLayout(new BorderLayout(0, 0));
		
		this.setBorder(null);
		
		setUpTable(sortable, rowSelection, hScroll);
	}
	
	public TablePanel(Class rowClass, int indexColumn, boolean sortable, boolean rowSelection)
	{
		this(rowClass, indexColumn, sortable, rowSelection, false);
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public TablePanel(Class rowClass, int indexColumn, String title, boolean sortable, boolean rowSelection)
	{
		this(rowClass, indexColumn, sortable, rowSelection);
		
		// Table Title Label
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		titlePanel.setBackground(ColorConstants.LightBlue);
		JLabel lblTitle = new JLabel(title);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		
		titlePanel.add(lblTitle, BorderLayout.CENTER);
		
		this.add(titlePanel, BorderLayout.NORTH);
	}
	
	@SuppressWarnings("unchecked")
	private void setUpTable(boolean sortable, boolean rowSelection, boolean hScroll)
	{
		RowItem rowItem;
		
		try
		{
			// Cast the row class to a a RowItem and use the RowItem api for
			// getting the field and their names
			rowItem = (RowItem) rowClass.newInstance();
			
			// Create a new table format via reflection of the rowClass
			TableFormat tf = GlazedLists.tableFormat(rowClass, rowItem.getFieldList(), rowItem.getFieldNames(), rowItem.getEditableCells());
			
			baseList = new BasicEventList();
			
			if(sortable)
			{
				sortedList = new SortedList(baseList);
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
	public void addRow(Object row)
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		activeList.add(row);
		
		activeList.getReadWriteLock().writeLock().unlock();
	}
	
	/**
	 * Updates a row
	 * @param rowKey
	 */
	public void updateRow(final Comparable rowKey, final Object row)
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey, indexColumn);
		
		if(index != -1)
		{
			activeList.set(index, row);
		}
		
		activeList.getReadWriteLock().writeLock().unlock();
	}
	
	private int findRow(Comparable rowKey, int column)
	{
		int index = -1;
		RowItem row;
		Comparable field;
		
		for(int i = 0; i < activeList.size(); i++)
		{
			row = (RowItem) activeList.get(i);
			
			field = (Comparable) row.getFieldValue(column);
			
			if(field.compareTo(rowKey) == 0)
			{
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	/**
	 * Removes a row
	 * @param rowKey
	 */
	public void removeRow(final Comparable rowKey)
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey, indexColumn);
		
		if(index != -1)
		{
			activeList.remove(index);
		}
		
		activeList.getReadWriteLock().writeLock().unlock();
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
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Object value;
		
		activeList.getReadWriteLock().writeLock().lock();
		
		RowItem row = (RowItem) activeList.get(rowIndex);
		
		value = row.getFieldValue(columnIndex);
		
		activeList.getReadWriteLock().writeLock().unlock();
		
		return value;
	}
	
	/**
	 * Returns the number of rows in the table
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
	
	public void updateCell(final Comparable rowKey, final int column, final Object columnValue)
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey, indexColumn);
		
		if(index != -1)
		{
			RowItem row = (RowItem) activeList.get(index);
			
			row.setFieldValue(column, columnValue);
			
			activeList.set(index, row);
		}
		
		activeList.getReadWriteLock().writeLock().unlock();
	}
	
	public void updateCells(final Comparable rowKey, final int columns[], final Object columnValues[])
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey, indexColumn);
		
		if(index != -1)
		{
			RowItem row = (RowItem) activeList.get(index);
			
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
}