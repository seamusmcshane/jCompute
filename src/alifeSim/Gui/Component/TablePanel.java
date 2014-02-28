package alifeSim.Gui.Component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

public class TablePanel extends JPanel
{
	private static final long serialVersionUID = 7193787210494563482L;
	private JLabel lblTitle;		
	private JTable table;
	private TableModel model;
	
	/**
	 * Creates a Panel with a table - needs Title and a List of column names (column titles)
	 * @param title
	 * @param columnNames
	 */
	public TablePanel(String title,String columnNames[])
	{
		super();

		setLayout(new BorderLayout(0, 0));
		this.setMinimumSize(new Dimension(350,250));
		this.setBorder(null);
		this.setPreferredSize(new Dimension(350,250));
		
		// Title
		lblTitle = new JLabel(title);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblTitle, BorderLayout.NORTH);
		
		setUpTable(this,columnNames);	

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
	
	/**
	 * Appends a new row to the end of the table
	 * @param rowKey
	 * @param columnValues
	 */
	public void addRow(String rowKey,String columnValueList[])
	{
		model.addRow(rowKey,columnValueList);
	}
	
	
	/**
	 * Updates a row
	 * @param rowKey
	 */
	public void updateRow(String rowKey,String columnValueList[])
	{
		model.updateRow(rowKey,columnValueList);
	}
	
	/**
	 * Removes a row
	 * @param rowKey
	 */
	public void removeRow(String rowKey)
	{
		model.removeRow(rowKey);
	}
	
	/**
	 * Clears the data in the Table
	 */
	public void clearTable()
	{
		model.clearData();
	}
	
	/**
	 * Creates the JTable with required values
	 * @param panel
	 * @param colNames
	 */
	private void setUpTable(JPanel panel,String colNames[])
	{
		model = new TableModel(colNames);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportBorder(null);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable(model);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		table.setBackground(Color.white);
		
		table.setRowSelectionAllowed(false);		
		table.setBorder(null);
		scrollPane.setViewportView(table);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.setCellSelectionEnabled(false);	
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		
	}
	
	public void addMouseListener(MouseAdapter adaptor)
	{		
		table.addMouseListener(adaptor);
	}
	
	/**
	 * Gets a value at a specific coordinate in the table
	 * @param row
	 * @param column
	 * @return
	 */
	public String getValueAt(int row,int column)
	{		
		String value;

		value = (String) model.getValueAt(row, column);
				
		return value;
	}
	
	
	/**
	 * Returns the number of rows in the table
	 * @return
	 */
	public int getRowsCount()
	{
		int rowCount = model.getRowCount();
		
		return rowCount;
	}
	
	public void clearSelection()
	{
		table.clearSelection();			
	}

	public void updateCell(String rowKey, int column, String columnValue)
	{
		model.updateCell(rowKey,column, columnValue);
	}
	
	public void updateCells(String rowKey, int columns[], String columnValues[])
	{		
		model.updateCells(rowKey,columns, columnValues);
	}
	
	public void RedrawTable()
	{
		model.dataSync(); 
	}
	
	
	/** 
	 * Private Table Model Class
	 * @author Seamus McShane
	 *
	 */
	private class TableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = -3810467809045113741L;
		private List<String> columnNames = new ArrayList<String>();
		private HashMap<String,TableRow> tableRowIndex;
		private ArrayList<TableRow> tableRows;

        public TableModel(String names[]) 
        {
        	// Set the Column names
    		for(String colName: names)
    		{
    			columnNames.add(colName);
    		}
            
    		// Init the table rows
            clearData();
            
        }
		
        public void updateCells(String rowKey, int[] columns, String[] columnValues)
		{
    		TableRow row = tableRowIndex.get(rowKey);
    		
    		for(int i=0;i<columns.length;i++)
    		{
        		row.updateColumn(columns[i], columnValues[i]);	
    		}
		}

		public void updateCell(String rowKey, int column, String columnValue)
		{
    		TableRow row = tableRowIndex.get(rowKey);
    		
    		row.updateColumn(column, columnValue);			
		}

		public void dataSync()
        {
        	fireTableDataChanged();
        }
        
        public void removeRow(String rowKey)
		{
        	// Remove from Index
        	TableRow row = tableRowIndex.remove(rowKey);
        	        	
        	// Remove from the rows      	
        	Iterator<TableRow> itr = tableRows.iterator();
        	
        	while(itr.hasNext())
        	{
        		TableRow temp = itr.next();
        		
        		if(temp.getColumn(0).equals(rowKey))
        		{
        			tableRows.remove(temp);
        			
        			break;
        		}
        	}
		}

		public void addRow(String rowKey,String columnValueList[])
        {
			// New Object
			TableRow temp = new TableRow(rowKey,columnValueList);
			
			// Add it to index
			tableRowIndex.put(rowKey,temp);
			
			// Add it to rows
			tableRows.add(temp);
			
        }
        
        public void updateRow(String rowKey,String columnValueList[])
        {
        	// Use index to update row
        	tableRowIndex.put(rowKey,new TableRow(rowKey,columnValueList));
        }
        
		@Override
		public int getColumnCount()
		{
			return columnNames.size();
		}

		@Override
		public String getColumnName(int index) 
		{
			return columnNames.get(index);			
		}
		
		@Override
		public int getRowCount()
		{
			return tableRows.size();
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			// Use tableRows for direct lookup
			return tableRows.get(row).getColumn(col);
		}
		
		public void clearData()
		{
			tableRowIndex = new HashMap<String,TableRow>();
			
			tableRows = new ArrayList<TableRow>();
		}
		
	}	
		
	/**
	 *  Private Row Class for Table Model
	 * @author Seamus McShane
	 *
	 */
	private class TableRow
	{
		private ArrayList<String> rowColumns;
		
		public TableRow(String row,String values[])
		{
			super();
			
			updateColumns(row,values);	
		}
		
		public String getColumn(int index)
		{
			return rowColumns.get(index);
		}
		
		public void updateColumns(String row,String values[])
		{
			this.rowColumns = new ArrayList<>();
			
			rowColumns.add(row);
			
			for(String value : values)
			{
				rowColumns.add(value);
			}
		}
		
		public void updateColumn(int column, String value)
		{
			rowColumns.set(column, value);
		}
	}

	public void setColumWidth(int column, int pref)
	{
		table.getColumnModel().getColumn(column).setPreferredWidth(pref);		
	}

}