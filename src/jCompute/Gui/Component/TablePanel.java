package jCompute.Gui.Component;

import jCompute.Debug.DebugLogger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.util.concurrent.Semaphore;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

public class TablePanel extends JPanel
{
	private static final long serialVersionUID = 7193787210494563482L;
	private JLabel lblTitle;
	private JTable table;
	private JScrollPane scrollPane;

	private BasicEventList<TablePanelRow> eventList = new BasicEventList<TablePanelRow>();

	/**
	 * Creates a Panel with a table - needs Title and a List of column names
	 * (column titles)
	 * 
	 * @param title
	 * @param columnNames
	 */
	public TablePanel(String title, String columnNames[], boolean alternatingRowColors)
	{
		super();

		setLayout(new BorderLayout(0, 0));

		this.setBorder(null);

		// Title
		lblTitle = new JLabel(title);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblTitle, BorderLayout.NORTH);

		setUpTable(this, columnNames, alternatingRowColors);

	}

	public TablePanel(String columnNames[], boolean alternatingRowColors)
	{
		super();

		setLayout(new BorderLayout(0, 0));

		this.setBorder(null);

		setUpTable(this, columnNames, alternatingRowColors);

	}

	/**
	 * Creates the JTable with required values
	 * 
	 * @param panel
	 * @param colNames
	 */
	private void setUpTable(JPanel panel, String colNames[], boolean alternatingRowColors)
	{
		TablePanelTableFormat tableFormat = new TablePanelTableFormat(colNames);
		EventTableModel tableModel = new EventTableModel(eventList, tableFormat);		
		EventSelectionModel selectionModel = new EventSelectionModel(eventList);
		table = new JTable(tableModel);
		table.setSelectionModel(selectionModel);

		scrollPane = new JScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportBorder(null);
		scrollPane.setWheelScrollingEnabled(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		table.setBackground(Color.white);
		table.setBorder(null);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);

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
	 * 
	 * @param rowKey
	 * @param columnValues
	 */
	public void addRow(final String columnValueList[])
	{
		eventList.getReadWriteLock().writeLock().lock();
		 
		eventList.add(new TablePanelRow(columnValueList));
		
		eventList.getReadWriteLock().writeLock().unlock();
	}

	/**
	 * Updates a row
	 * 
	 * @param rowKey
	 */
	public void updateRow(final String rowKey, final String columnValueList[])
	{
		eventList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey);
		
		if(index!=-1)
		{
			TablePanelRow row = eventList.get(index);
			
			row.setValues(columnValueList);
			
			eventList.set(index, row);
		}
		
		eventList.getReadWriteLock().writeLock().unlock();
	}

	private int findRow(String rowKey)
	{
		int index = -1;
		TablePanelRow row;
		
		for(int i=0;i<eventList.size();i++)
		{
			row = eventList.get(i);
			if(row.getColumnValue(0).equals(rowKey))
			{
				index =i;
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
	public void removeRow(final String rowKey)
	{
		eventList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey);
		
		if(index!=-1)
		{
			TablePanelRow row = eventList.get(index);
			
			eventList.remove(row);
		}

		eventList.getReadWriteLock().writeLock().unlock();
	}

	/**
	 * Clears the data in the Table
	 */
	public void clearTable()
	{
		eventList.getReadWriteLock().writeLock().lock();

		eventList.clear();
		
		eventList.getReadWriteLock().writeLock().unlock();

	}

	public void addMouseListener(MouseAdapter adaptor)
	{
		table.addMouseListener(adaptor);
	}

	/**
	 * Gets a value at a specific coordinate in the table
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	public String getValueAt(int rowIndex, int column)
	{
		String value;
		
		eventList.getReadWriteLock().writeLock().lock();
		
		TablePanelRow row = eventList.get(rowIndex);
		
		value = row.getColumnValue(column);
				
		eventList.getReadWriteLock().writeLock().unlock();
		
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
		
		eventList.getReadWriteLock().writeLock().lock();

		size = eventList.size();
				
		eventList.getReadWriteLock().writeLock().unlock();

		return size;
	}

	public void clearSelection()
	{
		table.clearSelection();
	}

	public void updateCell(final String rowKey, final int column, final String columnValue)
	{
		eventList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey);
		
		if(index!=-1)
		{
			TablePanelRow row = eventList.get(index);
			
			row.setValueAt(column,columnValue);
			
			eventList.set(index, row);
		}

		
		eventList.getReadWriteLock().writeLock().unlock();
	}

	public void updateCells(final String rowKey, final int columns[], final String columnValues[])
	{
		eventList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey);
		
		if(index!=-1)
		{
			TablePanelRow row = eventList.get(index);
			
			row.setValuesInColumn(columns,columnValues);
			
			eventList.set(index, row);
		}

		
		eventList.getReadWriteLock().writeLock().unlock();
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

	private class TablePanelTableFormat implements TableFormat
	{
		private String[] columnNames;
		private int noColumns;
		
		public TablePanelTableFormat(String[] columnNames)
		{
			this.columnNames = columnNames;
			this.noColumns = columnNames.length;
		}
		
		public int getColumnCount()
		{
			return noColumns;
		}
		public String getColumnName(int column)
		{
			if(column < 0 || column > noColumns)
			{
				return null;
			}

			return columnNames[column];			
		}
		
		public Object getColumnValue(Object baseObject, int column)
		{
			TablePanelRow row = (TablePanelRow) baseObject;
			
			return row.getColumnValue(column);
		}
	}
	
	private class TablePanelRow
	{
		private String[] columnValues;
		private int noColumns;
		
		public TablePanelRow(String[] columnValues)
		{
			this.columnValues = columnValues;
			this.noColumns = columnValues.length;
		}

		public void setValuesInColumn(int[] columns, String[] columnValues)
		{
			int len = columns.length;
			int column = -1;
			
			for(int i=0;i<len;i++)
			{
				column = columns[i];
				
				setValueAt(column,columnValues[i]);
			}			
		}

		public void setValueAt(int column,String value)
		{			
			columnValues[column] = value;
		}
		
		public void setValues(String[] columnValues)
		{
			for(int i=0;i<noColumns;i++)
			{
				this.columnValues[i] = columnValues[i];
			}
		}
		
		public String getColumnValue(int column)
		{
			if(column < 0 || column > noColumns)
			{
				return null;
			}
			
			return columnValues[column];			
		}
	}
	
}