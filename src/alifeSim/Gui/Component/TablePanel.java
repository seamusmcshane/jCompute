package alifeSim.Gui.Component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.sun.glass.ui.Cursor;

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
	public void addRow(String rowKey,String columnValues[])
	{
		model.addRow(rowKey,columnValues);
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
		return (String)model.getValueAt(row, column);
	}
	
	
	/**
	 * Returns the number of rows in the table
	 * @return
	 */
	public int getRowsCount()
	{
		return model.getRowCount();
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
		private List<TableRow> tableRows;

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
				        
        public void removeRow(String rowKey)
		{
  
        	Iterator<TableRow> itr = tableRows.iterator();
        	
        	int row = 0;
        	
        	while(itr.hasNext())
        	{
        		TableRow current = itr.next();
        		
        		if(current.getColumn(0).equals(rowKey))
        		{
        			
        			//current.updateColumns(rowKey, columnValueList);
        			
        			tableRows.remove(current);
        			
                	fireTableRowsDeleted(row, row);
                	
        			break;
        		}
        		row++;
        	}
			
		}

		public void addRow(String rowKey,String columnList[])
        {
        	tableRows.add(new TableRow(rowKey,columnList));
        	
        	// Redraw the last row
        	fireTableRowsInserted(tableRows.size() - 1, tableRows.size() - 1);
        }
        
        public void updateRow(String rowKey,String columnValueList[])
        {
        	Iterator<TableRow> itr = tableRows.iterator();
        	
        	int row = 0;
        	
        	while(itr.hasNext())
        	{
        		TableRow current = itr.next();
        		
        		if(current.getColumn(0).equals(rowKey))
        		{
        			
        			current.updateColumns(rowKey, columnValueList);
        			
                	// Redraw the row...
                	fireTableRowsUpdated(row,row);
        			
        			break;
        		}
        		
        		row++;
        	}

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
			return tableRows.get(row).getColumn(col);
		}
		
		public void clearData()
		{
			this.tableRows = new ArrayList<TableRow>();
		}
		
	}	
	
	/**
	 *  Private Row Class for Table Model
	 * @author Seamus McShane
	 *
	 */
	private class TableRow
	{
		private List<String> rowColumns;
		
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
	}

	public void clearSelection()
	{
		table.clearSelection();		
	}
		
}